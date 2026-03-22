import json
import logging
import re
from difflib import SequenceMatcher

from google import genai
from django.conf import settings
from django.db.models import Sum, F, FloatField
from django.utils import timezone

from ..models import UserProfile, FoodLog, FoodItem

logger = logging.getLogger(__name__)


class FoodScanService:
    DEBUG_HARDCODE_IDLI = False

    BLACKLIST = {
        "textile", "wool", "toy", "fabric", "clothing", "pattern", "product",
        "person", "human", "furniture", "table", "bottle", "wrapper", "background",
        "plate", "dish", "bowl", "spoon", "fork", "knife", "utensil", "cutlery",
        "hand", "finger", "countertop", "floor", "wall", "window"
    }

    ALIASES = {
        "idly": "idli",
        "chapathi": "chapati",
        "roti": "chapati",
        "plain dosa": "dosa",
        "masala dosa": "dosa",
        "veg sandwich": "sandwich",
        "vegetable sandwich": "sandwich",
        "veg burger": "burger",
        "vegetable burger": "burger",
        "dal tadka": "dal",
        "sambar rice": "sambar",
        "curd": "yogurt",
    }

    STATIC_NUTRITION = {
        # only used when AI identifies the name but DB has no row and AI gives no macros
        "banana": {"calories": 89, "protein": 1.1, "carbs": 23.0, "fats": 0.3, "fiber": 2.6},
        "apple": {"calories": 52, "protein": 0.3, "carbs": 14.0, "fats": 0.2, "fiber": 2.4},
        "burger": {"calories": 295, "protein": 17.0, "carbs": 24.0, "fats": 14.0, "fiber": 1.3},
        "sandwich": {"calories": 250, "protein": 12.0, "carbs": 30.0, "fats": 9.0, "fiber": 2.0},
        "pizza": {"calories": 266, "protein": 11.0, "carbs": 33.0, "fats": 10.0, "fiber": 2.3},
        "dosa": {"calories": 168, "protein": 4.0, "carbs": 27.0, "fats": 4.0, "fiber": 1.0},
        "idli": {"calories": 58, "protein": 2.0, "carbs": 12.0, "fats": 0.2, "fiber": 0.5},
        "chapati": {"calories": 297, "protein": 9.8, "carbs": 50.0, "fats": 7.0, "fiber": 4.0},
        "rice": {"calories": 130, "protein": 2.7, "carbs": 28.0, "fats": 0.3, "fiber": 0.4},
        "dal": {"calories": 116, "protein": 9.0, "carbs": 20.0, "fats": 0.4, "fiber": 8.0},
        "sambar": {"calories": 80, "protein": 3.5, "carbs": 12.0, "fats": 2.5, "fiber": 3.0},
        "bread": {"calories": 265, "protein": 9.0, "carbs": 49.0, "fats": 3.2, "fiber": 2.7},
        "yogurt": {"calories": 59, "protein": 10.0, "carbs": 3.6, "fats": 0.4, "fiber": 0.0},
    }

    SIMPLE_FOOD_NAMES = {
        "banana", "apple", "burger", "sandwich", "pizza",
        "dosa", "idli", "chapati", "rice", "dal", "sambar"
    }

    def __init__(self):
        self.api_key = getattr(settings, "GEMINI_API_KEY", None)

        # Best stable practical choice for scan
        self.model_name = "gemini-2.5-flash"

        # Optional stronger preview model for testing only
        self.preview_model_name = "gemini-3-flash-preview"

        # Safe fallback
        self.fallback_model_name = "gemini-2.0-flash"

        self.client = None

        if self.api_key:
            try:
                self.client = genai.Client(api_key=self.api_key)
            except Exception:
                logger.exception("SCAN-AI: Gemini client initialization failed")

    def scan_food(self, image_file, additional_text="", user=None):
        logger.info("SCAN-AI: Food scan started")

        if self.DEBUG_HARDCODE_IDLI:
            items = [
                self._normalized_result(
                    name="Idli",
                    calories=58, protein=2, carbs=12, fats=0.2, fiber=0.5,
                    confidence=0.95,
                    pro_tip="Debug result.",
                    healthier_alternative="",
                    source="Debug"
                )
            ]
            return self._personalize_results(items, user), "success"

        try:
            image_bytes = image_file.read()
            image_file.seek(0)
        except Exception:
            logger.exception("SCAN-AI: Failed reading image bytes")
            image_bytes = None

        candidates = self._try_ai_detection(image_bytes, additional_text)
        if not candidates:
            uncertain = [self._normalized_result(
                name="Uncertain result",
                calories=0, protein=0, carbs=0, fats=0, fiber=0,
                sugar=0, saturatedFat=0,
                vitaminA=0, vitaminC=0, vitaminD=0, vitaminB12=0,
                calcium=0, iron=0, magnesium=0, potassium=0, sodium=0, zinc=0,
                confidence=0.0,
                pro_tip="Could not identify food confidently. Retry with one food item in good lighting, or enter food manually.",
                healthier_alternative="Use Search / Enter Manually for accurate logging.",
                source="Uncertain"
            )]
            return uncertain, "Low-confidence"

        hydrated = []
        for candidate in candidates:
            item = self._hydrate_candidate(candidate)
            if item:
                hydrated.append(item)

        hydrated = self._dedupe_results(hydrated)
        if not hydrated:
            uncertain = [self._normalized_result(
                name="Uncertain result",
                calories=0, protein=0, carbs=0, fats=0, fiber=0,
                sugar=0, saturatedFat=0,
                vitaminA=0, vitaminC=0, vitaminD=0, vitaminB12=0,
                calcium=0, iron=0, magnesium=0, potassium=0, sodium=0, zinc=0,
                confidence=0.0,
                pro_tip="Could not identify food confidently. Retry with one food item in good lighting, or enter food manually.",
                healthier_alternative="Use Search / Enter Manually for accurate logging.",
                source="Uncertain"
            )]
            return uncertain, "Low-confidence"

        return self._personalize_results(hydrated, user), "success"

    def _try_ai_detection(self, image_bytes, additional_text=""):
        if not self.client or not image_bytes:
            logger.warning("SCAN-AI: Missing Gemini client or image bytes")
            return None

        prompt = self._build_prompt(additional_text)

        models_to_try = [
            self.model_name,          # gemini-2.5-flash
            self.preview_model_name,  # gemini-3-flash-preview
            self.fallback_model_name  # gemini-2.0-flash
        ]

        for model_name in models_to_try:
            try:
                logger.info("SCAN-AI: Trying model %s", model_name)

                response = self.client.models.generate_content(
                    model=model_name,
                    contents=[
                        {
                            "role": "user",
                            "parts": [
                                {"text": prompt},
                                {
                                    "inline_data": {
                                        "mime_type": "image/jpeg",
                                        "data": image_bytes
                                    }
                                }
                            ]
                        }
                    ]
                )

                raw_text = (response.text or "").strip()
                logger.info("SCAN-AI: Raw response length from %s = %d", model_name, len(raw_text))
                logger.info("SCAN-AI: Raw response preview from %s = %s", model_name, raw_text[:500])

                if not raw_text:
                    continue

                cleaned = self._extract_json_text(raw_text)
                if cleaned:
                    payload = json.loads(cleaned)
                    items = payload.get("items", [])
                    results = []
                    for item in items:
                        parsed = self._parse_ai_candidate(item)
                        if parsed:
                            results.append(parsed)

                    results = self._dedupe_results(results)
                    if results:
                        return results

                recovered = self._recover_candidates_from_text(raw_text)
                if recovered:
                    return recovered

            except Exception:
                logger.exception("SCAN-AI: Model %s failed", model_name)

        return None

    def _parse_ai_candidate(self, item):
        if not isinstance(item, dict):
            return None

        name = (item.get("name") or "").strip()
        if not name:
            return None

        lower = name.lower()
        if lower in self.BLACKLIST:
            return None

        confidence = self._safe_float(item.get("confidence"), 0.0)
        if confidence <= 0:
            confidence = 0.45

        est = item.get("estimated_per_100g") or {}

        return {
            "name": name,
            "confidence": confidence,
            "estimated_per_100g": est,
            "healthier_alternative": item.get("healthier_alternative", "") or "",
            "pro_tip": item.get("pro_tip", "") or ""
        }

    def _recover_candidates_from_text(self, raw_text):
        lower = raw_text.lower()
        names = set(self.SIMPLE_FOOD_NAMES)
        names.update(self.ALIASES.keys())
        names.update(self.ALIASES.values())
        try:
            names.update([n.lower() for n in FoodItem.objects.values_list("name", flat=True)[:500] if n])
        except Exception:
            pass

        found = []
        for name in sorted(names, key=len, reverse=True):
            if name in lower and name not in self.BLACKLIST:
                found.append({
                    "name": name,
                    "confidence": 0.40,
                    "estimated_per_100g": {},
                    "healthier_alternative": "",
                    "pro_tip": "Recovered from AI text output. Please confirm the food name."
                })
            if len(found) >= 3:
                break

        return self._dedupe_results(found) or None

    def _hydrate_candidate(self, candidate):
        raw_name = (candidate.get("name") or "").strip()
        if not raw_name:
            return None

        normalized_name = self.ALIASES.get(raw_name.lower(), raw_name.lower())
        confidence = self._safe_float(candidate.get("confidence"), 0.45)
        est = candidate.get("estimated_per_100g") or {}
        pro_tip = candidate.get("pro_tip", "") or ""
        healthier = candidate.get("healthier_alternative", "") or ""

        # 1. DB nutrition is preferred for logging accuracy
        db_match = self._find_food_match(normalized_name)
        if db_match:
            serving_qty = float(db_match.serving_quantity or 100.0)
            factor = serving_qty / 100.0

            return self._apply_scan_quality_guidance(
                self._normalized_result(
                    name=db_match.name,
                    calories=float(db_match.calories_per_100g) * factor,
                    protein=float(db_match.protein_per_100g) * factor,
                    carbs=float(db_match.carbs_per_100g) * factor,
                    fats=float(db_match.fats_per_100g) * factor,
                    fiber=self._safe_float(est.get("fiber"), 0.0) * factor,
                    sugar=self._safe_float(est.get("sugar"), 0.0) * factor,
                    saturatedFat=self._safe_float(est.get("saturatedFat"), 0.0) * factor,
                    vitaminA=self._safe_float(est.get("vitaminA"), 0.0) * factor,
                    vitaminC=self._safe_float(est.get("vitaminC"), 0.0) * factor,
                    vitaminD=self._safe_float(est.get("vitaminD"), 0.0) * factor,
                    vitaminB12=self._safe_float(est.get("vitaminB12"), 0.0) * factor,
                    calcium=self._safe_float(est.get("calcium"), 0.0) * factor,
                    iron=self._safe_float(est.get("iron"), 0.0) * factor,
                    magnesium=self._safe_float(est.get("magnesium"), 0.0) * factor,
                    potassium=self._safe_float(est.get("potassium"), 0.0) * factor,
                    sodium=self._safe_float(est.get("sodium"), 0.0) * factor,
                    zinc=self._safe_float(est.get("zinc"), 0.0) * factor,
                    servingQuantity=serving_qty,
                    servingUnit=db_match.serving_unit or "serving",
                    confidence=confidence,
                    healthier_alternative=healthier,
                    pro_tip=pro_tip,
                    source="Database"
                )
            )

        # 2. AI estimate if macros exist
        ai_calories = self._safe_float(est.get("calories"), None)
        if ai_calories is not None and ai_calories > 0:
            item = self._normalized_result(
                name=raw_name.title(),
                calories=ai_calories,
                protein=self._safe_float(est.get("protein"), 0.0),
                carbs=self._safe_float(est.get("carbs"), 0.0),
                fats=self._safe_float(est.get("fats"), 0.0),
                fiber=self._safe_float(est.get("fiber"), 0.0),
                sugar=self._safe_float(est.get("sugar"), 0.0),
                saturatedFat=self._safe_float(est.get("saturatedFat"), 0.0),
                vitaminA=self._safe_float(est.get("vitaminA"), 0.0),
                vitaminC=self._safe_float(est.get("vitaminC"), 0.0),
                vitaminD=self._safe_float(est.get("vitaminD"), 0.0),
                vitaminB12=self._safe_float(est.get("vitaminB12"), 0.0),
                calcium=self._safe_float(est.get("calcium"), 0.0),
                iron=self._safe_float(est.get("iron"), 0.0),
                magnesium=self._safe_float(est.get("magnesium"), 0.0),
                potassium=self._safe_float(est.get("potassium"), 0.0),
                sodium=self._safe_float(est.get("sodium"), 0.0),
                zinc=self._safe_float(est.get("zinc"), 0.0),
                confidence=confidence,
                healthier_alternative=healthier,
                pro_tip=pro_tip,
                source="AI Estimate"
            )
            return self._apply_scan_quality_guidance(item)

        # 3. Static only if name is identified but no DB/macros
        static = self.STATIC_NUTRITION.get(normalized_name)
        if static:
            item = self._normalized_result(
                name=raw_name.title(),
                calories=static["calories"],
                protein=static["protein"],
                carbs=static["carbs"],
                fats=static["fats"],
                fiber=static.get("fiber", 0.0),
                confidence=min(confidence, 0.55),
                healthier_alternative=healthier,
                pro_tip=pro_tip or "Using reference nutrition for the identified food. Please verify serving size.",
                source="Backup Reference"
            )
            return self._apply_scan_quality_guidance(item)

        return None

    def _apply_scan_quality_guidance(self, item):
        confidence = float(item.get("confidence", 0.0))
        source = item.get("source", "")

        base_tip = item.get("pro_tip", "") or ""

        if confidence >= 0.80:
            prefix = "High confidence. You can review and log this result."
        elif confidence >= 0.55:
            prefix = "Medium confidence. Please confirm the food name before logging."
        else:
            prefix = "Low confidence. Confirm the food name or use manual search for best accuracy."

        if source == "Database":
            suffix = " Nutrition is taken from your trusted food database."
        elif source == "AI Estimate":
            suffix = " Nutrition is estimated from the image."
        elif source == "Backup Reference":
            suffix = " Nutrition is from backup reference data."
        else:
            suffix = ""

        item["pro_tip"] = f"{prefix}{suffix} {base_tip}".strip()

        if confidence < 0.55:
            item["healthier_alternative"] = (
                item.get("healthier_alternative", "") or
                "Capture one food item at a time in good lighting, or use Search / Enter Manually."
            )

        return item

    def _find_food_match(self, query_name):
        query_name = (query_name or "").strip().lower()
        if not query_name:
            return None

        exact = FoodItem.objects.filter(name__iexact=query_name).first()
        if exact:
            return exact

        best_item = None
        best_score = 0.0
        for item in FoodItem.objects.all()[:500]:
            db_name = (item.name or "").strip().lower()
            score = self._similarity(query_name, db_name)
            if score > best_score:
                best_score = score
                best_item = item

        if best_item and best_score >= 0.78:
            return best_item
        return None

    def _similarity(self, a, b):
        seq_score = SequenceMatcher(None, a, b).ratio()
        a_tokens = set(a.split())
        b_tokens = set(b.split())
        token_score = len(a_tokens & b_tokens) / max(1, len(a_tokens | b_tokens))
        return max(seq_score, token_score)

    def _build_prompt(self, additional_text=""):
        prompt = """
You are a food recognition assistant for a nutrition app.

Task:
- Identify the visible food in the image.
- Return up to 3 likely candidates.
- Prefer specific simple food names when possible.

Focus especially on common simple foods:
banana, apple, burger, sandwich, pizza, dosa, idli, chapati, rice, dal.

Rules:
- Ignore plate, spoon, bowl, background, logos, watermark text, table, and hands.
- If one food item is clearly visible, return that as the top candidate.
- Never use generic names like "food" or "meal".
- Confidence must be between 0.0 and 1.0.

Return ONLY valid JSON:
{
  "items": [
    {
      "name": "food name",
      "confidence": 0.0,
      "estimated_per_100g": {
        "calories": 0.0,
        "protein": 0.0,
        "carbs": 0.0,
        "fats": 0.0,
        "fiber": 0.0,
        "sugar": 0.0,
        "saturatedFat": 0.0,
        "vitaminA": 0.0,
        "vitaminC": 0.0,
        "vitaminD": 0.0,
        "vitaminB12": 0.0,
        "calcium": 0.0,
        "iron": 0.0,
        "magnesium": 0.0,
        "potassium": 0.0,
        "sodium": 0.0,
        "zinc": 0.0
      },
      "healthier_alternative": "",
      "pro_tip": ""
    }
  ]
}
""".strip()

        if additional_text:
            prompt += f"\nUser context: {additional_text.strip()}"
        return prompt

    def _extract_json_text(self, text):
        if not text:
            return ""
        text = text.strip()
        match = re.search(r"```(?:json)?(.*?)```", text, re.DOTALL | re.IGNORECASE)
        if match:
            text = match.group(1).strip()
        start = text.find("{")
        end = text.rfind("}")
        if start == -1 or end == -1 or end <= start:
            return ""
        return text[start:end + 1].strip()

    def _dedupe_results(self, items):
        out = []
        seen = set()
        for item in items:
            key = (item.get("name") or "").strip().lower()
            if key and key not in seen:
                seen.add(key)
                out.append(item)
        return out

    def _normalized_result(
        self,
        name,
        calories,
        protein,
        carbs,
        fats,
        fiber=0,
        sugar=0,
        saturatedFat=0,
        vitaminA=0,
        vitaminC=0,
        vitaminD=0,
        vitaminB12=0,
        calcium=0,
        iron=0,
        magnesium=0,
        potassium=0,
        sodium=0,
        zinc=0,
        servingQuantity=100.0,
        servingUnit="g",
        confidence=0.0,
        healthier_alternative="",
        pro_tip="",
        source="Unknown"
    ):
        return {
            "name": str(name).strip(),
            "calories": round(float(calories), 1),
            "protein": round(float(protein), 1),
            "carbs": round(float(carbs), 1),
            "fats": round(float(fats), 1),
            "fiber": round(float(fiber), 1),
            "sugar": round(float(sugar), 1),
            "saturatedFat": round(float(saturatedFat), 1),
            "vitaminA": round(float(vitaminA), 1),
            "vitaminC": round(float(vitaminC), 1),
            "vitaminD": round(float(vitaminD), 1),
            "vitaminB12": round(float(vitaminB12), 1),
            "calcium": round(float(calcium), 1),
            "iron": round(float(iron), 1),
            "magnesium": round(float(magnesium), 1),
            "potassium": round(float(potassium), 1),
            "sodium": round(float(sodium), 1),
            "zinc": round(float(zinc), 1),
            "healthier_alternative": healthier_alternative or "",
            "pro_tip": pro_tip or "",
            "servingQuantity": round(float(servingQuantity), 1),
            "servingUnit": servingUnit or "g",
            "confidence": round(float(confidence), 2),
            "source": source,
        }

    def _safe_float(self, value, default=0.0):
        try:
            if value is None:
                return default
            return float(value)
        except Exception:
            return default

    def _personalize_results(self, items, user):
        if not user or not getattr(user, "is_authenticated", False):
            return items

        profile = UserProfile.objects.filter(user=user).first()
        if not profile:
            return items

        today = timezone.localdate()
        consumed = FoodLog.objects.filter(
            user=user,
            timestamp__date=today
        ).aggregate(
            total=Sum(F("calories_per_unit") * F("quantity"), output_field=FloatField())
        )["total"] or 0.0

        target_calories = 2000.0
        try:
            calc_target = profile.calculate_calorie_goal()
            if calc_target and calc_target > 0:
                target_calories = float(calc_target)
            elif profile.target_calories:
                target_calories = float(profile.target_calories)
        except Exception:
            if profile.target_calories:
                target_calories = float(profile.target_calories)

        remaining = max(0.0, target_calories - consumed)

        allergies = self._normalize_list(profile.food_allergies) + self._normalize_list(profile.allergies)
        dislikes = self._normalize_list(profile.dislikes) + self._normalize_text_list(profile.food_dislikes)
        conditions = self._normalize_list(profile.health_conditions)
        goal = (profile.goal or "").lower()
        diet_type = (profile.diet_type or "").lower()

        return [
            self._apply_personal_rules(item, remaining, allergies, dislikes, conditions, goal, diet_type)
            for item in items
        ]

    def _apply_personal_rules(self, item, remaining_calories, allergies, dislikes, conditions, goal, diet_type):
        name = (item.get("name") or "").lower()

        warnings = []
        additions = []
        reductions = []
        replacements = []

        if any(x in name for x in allergies):
            warnings.append("This may conflict with your allergy profile. Avoid this item.")
        if any(x in name for x in dislikes):
            reductions.append("You marked similar foods as disliked.")
        if diet_type == "vegetarian" and any(x in name for x in ["chicken", "fish", "mutton", "meat"]):
            warnings.append("This may not match your vegetarian preference.")
        if diet_type == "vegan" and any(x in name for x in ["milk", "paneer", "cheese", "curd", "yogurt", "egg"]):
            warnings.append("This may not match your vegan preference.")

        item_calories = float(item.get("calories", 0.0))
        fats = float(item.get("fats", 0.0))
        carbs = float(item.get("carbs", 0.0))
        protein = float(item.get("protein", 0.0))
        sugar = float(item.get("sugar", 0.0))
        sodium = float(item.get("sodium", 0.0))
        sat_fat = float(item.get("saturatedFat", 0.0))
        fiber = float(item.get("fiber", 0.0))

        if remaining_calories > 0 and item_calories > remaining_calories and item_calories > 0:
            reductions.append(f"This food may exceed your remaining calorie budget ({int(remaining_calories)} kcal left today).")
            replacements.append("Choose a smaller portion or a lighter alternative.")

        if "lose" in goal:
            if item_calories > 250:
                reductions.append("Reduce portion size to support your weight-loss goal.")
            if fats > 12:
                replacements.append("Replace with a grilled, less oily, or lower-fat option.")
            if fiber < 3 and item_calories > 0:
                additions.append("Add vegetables or salad to improve fullness and fiber.")

        if "gain" in goal or "muscle" in goal:
            if protein < 12 and item_calories > 0:
                additions.append("Add a higher-protein side like eggs, paneer, curd, lentils, or chicken.")

        lower_conditions = [c.lower() for c in conditions]
        if any("diabetes" in c for c in lower_conditions):
            if carbs > 25 or sugar > 8:
                reductions.append("High carb or sugar foods may need portion control for diabetes management.")
            if fiber < 3 and item_calories > 0:
                additions.append("Add fiber-rich foods to slow glucose absorption.")

        if any("blood pressure" in c for c in lower_conditions) or any("hypertension" in c for c in lower_conditions):
            if sodium > 300:
                reductions.append("This may be high in sodium. Reduce portion or choose a lower-salt option.")

        if any("cholesterol" in c for c in lower_conditions):
            if sat_fat > 4 or fats > 15:
                replacements.append("Replace with a lower saturated-fat version.")

        if any("pcos" in c for c in lower_conditions):
            if carbs > 30 and fiber < 3:
                reductions.append("Refined or high-carb foods may need moderation for PCOS.")
            if item_calories > 0:
                additions.append("Add protein and fiber to make this meal more balanced.")

        existing_tip = item.get("pro_tip", "") or ""
        existing_alt = item.get("healthier_alternative", "") or ""

        tip_parts = []
        if warnings:
            tip_parts.extend(warnings)
        if reductions:
            tip_parts.append("Reduce: " + " ".join(reductions))
        if additions:
            tip_parts.append("Add: " + " ".join(additions))

        alt_parts = []
        if replacements:
            alt_parts.append("Replace: " + " ".join(replacements))

        item["pro_tip"] = " ".join([p for p in [existing_tip] + tip_parts if p]).strip()[:500]
        item["healthier_alternative"] = " ".join([p for p in [existing_alt] + alt_parts if p]).strip()[:400]
        return item

    def _normalize_list(self, value):
        if isinstance(value, list):
            return [str(x).strip().lower() for x in value if str(x).strip()]
        if isinstance(value, str):
            return [x.strip().lower() for x in value.split(",") if x.strip()]
        return []

    def _normalize_text_list(self, value):
        if not value:
            return []
        return [x.strip().lower() for x in str(value).split(",") if x.strip()]
