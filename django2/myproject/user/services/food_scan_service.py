import json
import logging
import re
import requests

import google.generativeai as genai
from PIL import Image
from django.conf import settings

from ..models import FoodItem

logger = logging.getLogger(__name__)


class FoodScanService:
    BLACKLIST = {
        "textile", "wool", "toy", "fabric", "clothing", "pattern", "product",
        "person", "human", "furniture", "table", "bottle", "wrapper", "background",
        "plate", "dish", "bowl", "spoon", "fork", "knife", "utensil", "cutlery",
        "hand", "finger", "countertop", "floor", "wall", "window"
    }
    
    COMMON_FOODS = [
        "banana", "apple", "pizza", "burger", "rice", "salad", "cake", "milk", "egg",
        "bread", "chicken", "coffee", "tea", "orange", "sandwich", "pasta"
    ]

    # Static nutrition data (per 100g) — ultimate fallback when all AI/API calls fail
    STATIC_NUTRITION = {
        "apple": {"calories": 52, "protein": 0.3, "carbs": 14, "fats": 0.2, "fiber": 2.4},
        "banana": {"calories": 89, "protein": 1.1, "carbs": 23, "fats": 0.3, "fiber": 2.6},
        "orange": {"calories": 47, "protein": 0.9, "carbs": 12, "fats": 0.1, "fiber": 2.4},
        "rice": {"calories": 130, "protein": 2.7, "carbs": 28, "fats": 0.3, "fiber": 0.4},
        "bread": {"calories": 265, "protein": 9, "carbs": 49, "fats": 3.2, "fiber": 2.7},
        "egg": {"calories": 155, "protein": 13, "carbs": 1.1, "fats": 11, "fiber": 0},
        "chicken": {"calories": 165, "protein": 31, "carbs": 0, "fats": 3.6, "fiber": 0},
        "milk": {"calories": 42, "protein": 3.4, "carbs": 5, "fats": 1, "fiber": 0},
        "pizza": {"calories": 266, "protein": 11, "carbs": 33, "fats": 10, "fiber": 2.3},
        "burger": {"calories": 295, "protein": 17, "carbs": 24, "fats": 14, "fiber": 1.3},
        "salad": {"calories": 20, "protein": 1.5, "carbs": 3.5, "fats": 0.2, "fiber": 2},
        "pasta": {"calories": 131, "protein": 5, "carbs": 25, "fats": 1.1, "fiber": 1.8},
        "sandwich": {"calories": 250, "protein": 12, "carbs": 30, "fats": 9, "fiber": 2},
        "cake": {"calories": 257, "protein": 4.6, "carbs": 36, "fats": 11, "fiber": 0.4},
        "coffee": {"calories": 2, "protein": 0.3, "carbs": 0, "fats": 0, "fiber": 0},
        "tea": {"calories": 1, "protein": 0.1, "carbs": 0.3, "fats": 0, "fiber": 0},
        "roti": {"calories": 297, "protein": 9.8, "carbs": 50, "fats": 7, "fiber": 4},
        "dal": {"calories": 116, "protein": 9, "carbs": 20, "fats": 0.4, "fiber": 8},
        "paneer": {"calories": 265, "protein": 18, "carbs": 1.2, "fats": 21, "fiber": 0},
        "idli": {"calories": 58, "protein": 2, "carbs": 12, "fats": 0.2, "fiber": 0.5},
        "dosa": {"calories": 168, "protein": 4, "carbs": 27, "fats": 4, "fiber": 1},
        "biryani": {"calories": 200, "protein": 7, "carbs": 25, "fats": 8, "fiber": 1},
        "samosa": {"calories": 262, "protein": 4, "carbs": 28, "fats": 15, "fiber": 2},
        "noodles": {"calories": 138, "protein": 4.5, "carbs": 25, "fats": 2, "fiber": 1},
        "fruit": {"calories": 52, "protein": 0.5, "carbs": 14, "fats": 0.2, "fiber": 2},
        "vegetables": {"calories": 25, "protein": 1.5, "carbs": 5, "fats": 0.2, "fiber": 2.5},
        "curd": {"calories": 98, "protein": 11, "carbs": 3.4, "fats": 4.3, "fiber": 0},
        "yogurt": {"calories": 59, "protein": 10, "carbs": 3.6, "fats": 0.4, "fiber": 0},
        "chapati": {"calories": 297, "protein": 9.8, "carbs": 50, "fats": 7, "fiber": 4},
        "oats": {"calories": 68, "protein": 2.4, "carbs": 12, "fats": 1.4, "fiber": 1.7},
        "poha": {"calories": 244, "protein": 5, "carbs": 52, "fats": 1, "fiber": 2},
        "upma": {"calories": 155, "protein": 4, "carbs": 22, "fats": 5.5, "fiber": 1.5},
        "paratha": {"calories": 260, "protein": 6, "carbs": 36, "fats": 10, "fiber": 3},
        "fish": {"calories": 206, "protein": 22, "carbs": 0, "fats": 12, "fiber": 0},
        "mutton": {"calories": 294, "protein": 25, "carbs": 0, "fats": 21, "fiber": 0},
        "cheese": {"calories": 402, "protein": 25, "carbs": 1.3, "fats": 33, "fiber": 0},
        "chocolate": {"calories": 546, "protein": 5, "carbs": 60, "fats": 31, "fiber": 7},
        "juice": {"calories": 46, "protein": 0.5, "carbs": 11, "fats": 0.1, "fiber": 0.2},
        "soup": {"calories": 35, "protein": 1.5, "carbs": 5, "fats": 1, "fiber": 0.5},
        "water": {"calories": 0, "protein": 0, "carbs": 0, "fats": 0, "fiber": 0},
        "snack": {"calories": 250, "protein": 5, "carbs": 30, "fats": 12, "fiber": 2},
        "dessert": {"calories": 350, "protein": 4, "carbs": 50, "fats": 15, "fiber": 1},
        "meat": {"calories": 250, "protein": 25, "carbs": 0, "fats": 15, "fiber": 0},
        "meal": {"calories": 400, "protein": 20, "carbs": 40, "fats": 15, "fiber": 4},
        "drink": {"calories": 100, "protein": 0.5, "carbs": 25, "fats": 0, "fiber": 0},
        "beverage": {"calories": 100, "protein": 0.5, "carbs": 25, "fats": 0, "fiber": 0},
        "vegetable": {"calories": 25, "protein": 1.5, "carbs": 5, "fats": 0.2, "fiber": 2.5},
    }

    def __init__(self):
        self.api_key = getattr(settings, "GEMINI_API_KEY", None)
        self.model = None

        if self.api_key:
            try:
                genai.configure(api_key=self.api_key)
                # Using gemini-2.0-flash for best compatibility with newest API keys
                self.model = genai.GenerativeModel("gemini-2.0-flash")
                logger.info("Gemini AI initialized with model gemini-2.0-flash")
            except Exception as e:
                logger.error(f"Failed to initialize Gemini AI: {e}")
        else:
            logger.error("GEMINI_API_KEY is not configured in settings.py.")

        # Developer Mock Mode (Allows testing without hitting API quotas)
        self.mock_mode = getattr(settings, "GEMINI_MOCK_MODE", False)
        
        self.edamam_app_id = getattr(settings, "EDAMAM_APP_ID", None)
        self.edamam_api_key = getattr(settings, "EDAMAM_API_KEY", None)

    def scan_food(self, image_file, additional_text=""):
        # Try AI-powered scan first
        if self.model:
            try:
                img = Image.open(image_file).convert("RGB")
                
                if self.mock_mode:
                    logger.info("GEMINI_MOCK_MODE is ON. Returning simulated response.")
                    raw_text = '{"items": [{"name": "Pizza", "confidence": 0.92, "estimated_per_100g": {"calories": 266, "protein": 11, "carbs": 33, "fats": 10}}]}'
                else:
                    prompt = self._build_prompt(additional_text)
                    try:
                        response = self.model.generate_content([prompt, img])
                        raw_text = getattr(response, "text", "") or ""
                    except Exception as e:
                        error_msg = str(e).lower()
                        if "not found" in error_msg or "404" in error_msg:
                            logger.warning("Primary model failed, trying gemini-1.5-pro")
                            try:
                                fallback_model = genai.GenerativeModel("gemini-1.5-pro")
                                response = fallback_model.generate_content([prompt, img])
                                raw_text = getattr(response, "text", "") or ""
                            except Exception as e2:
                                logger.warning("Fallback model also failed: %s", e2)
                                raw_text = ""
                        else:
                            logger.warning("Gemini generate_content failed: %s", e)
                            raw_text = ""
                
                if raw_text:
                    logger.info("Raw Gemini response length: %d", len(raw_text))
                    cleaned_text = self._extract_json_text(raw_text)

                    if cleaned_text:
                        data = json.loads(cleaned_text)
                        detected_items = data.get("items", [])
                        
                        if isinstance(detected_items, list) and detected_items:
                            results = []
                            for item in detected_items:
                                mapped = self._map_item_to_result(item)
                                if mapped:
                                    results.append(mapped)

                            if results:
                                return results, "success"

            except Exception as e:
                logger.exception("Gemini scan failed: %s", e)
        else:
            logger.warning("AI model unavailable — using fallback detection.")

        # Fallback chain: best-effort → static data → generic food
        logger.info("Primary AI scan did not produce results. Starting fallback chain.")

        # 1. Try best-effort (filename + context + DB match)
        best_effort, msg = self._get_best_effort_result(image_file, additional_text)
        if best_effort:
            return best_effort, msg

        # 2. Try static nutrition data fallback
        static_result = self._static_fallback(image_file, additional_text)
        if static_result:
            return static_result, "success"

        # 3. Ultimate fallback: return a generic "Mixed Food" result so user always sees something
        logger.info("All fallbacks exhausted — returning generic food estimate.")
        
        fallback_name = "Food Item (estimate)"
        keywords = set()
        if additional_text:
            # We preserve order if we can, but a set is unordered, so let's just pick in order
            ordered_words = re.findall(r"[A-Za-z]+", additional_text.lower())
            for w in ordered_words:
                if w not in self.BLACKLIST and len(w) > 2:
                    fallback_name = f"{w.capitalize()} (estimate)"
                    break

        return [{
            "name": fallback_name,
            "calories": 200.0,
            "protein": 8.0,
            "carbs": 25.0,
            "fats": 7.0,
            "fiber": 2.0,
            "sugar": 3.0,
            "saturatedFat": 2.0,
            "vitaminA": 0.0, "vitaminC": 0.0, "vitaminD": 0.0, "vitaminB12": 0.0,
            "calcium": 0.0, "iron": 0.0, "magnesium": 0.0, "potassium": 0.0,
            "sodium": 0.0, "zinc": 0.0,
            "healthier_alternative": "Try logging manually for accurate tracking.",
            "pro_tip": "For best results, take a clear photo of your food in good lighting.",
            "servingQuantity": 100.0,
            "servingUnit": "g",
            "confidence": 0.3,
        }], "Low-confidence estimate. Please verify or edit the values."



    def _get_best_effort_result(self, image_file, additional_text=""):
        """Attempts to guess the food based on filename, user context, or lightweight cues."""
        logger.info("Starting best-effort fallback scan. Additional text: %s", additional_text)
        
        # Collect all potential keywords
        keywords = set()
        
        # 1. From user context (additional_text)
        if additional_text:
            context_words = re.findall(r"[A-Za-z]+", additional_text.lower())
            keywords.update(context_words)

        # 2. From filename
        filename = getattr(image_file, "name", "") or ""
        file_words = re.findall(r"[A-Za-z]+", filename.lower())
        keywords.update(file_words)

        logger.info("Best-effort keywords collected: %s", keywords)

        # Try to find the best match from COMMON_FOODS first
        best_match = None

        for word in keywords:
            if word in self.BLACKLIST:
                continue
            
            # Priority 1: Exact match in COMMON_FOODS
            if word in self.COMMON_FOODS:
                match = self._get_db_match(word, 0.65)
                if match:
                    logger.info("Fallback matching active. Found low-confidence result.")
                    return [match], "success"

            # Priority 2: Any DB match
            db_food = self._find_food_match(word)
            if db_food:
                candidate = self._get_db_match(word, 0.55)
                if candidate:
                    if not best_match:
                        best_match = candidate

        if best_match and isinstance(best_match, dict):
            best_match["confidence"] = 0.55
            logger.info("Fallback matching active. Returning DB candidate.")
            return [best_match], "success"

        return None, ""

    def _static_fallback(self, image_file, additional_text=""):
        """Matches keywords against STATIC_NUTRITION data when DB and API are unavailable."""
        keywords = set()
        if additional_text:
            keywords.update(re.findall(r"[A-Za-z]+", additional_text.lower()))
        filename = getattr(image_file, "name", "") or ""
        keywords.update(re.findall(r"[A-Za-z]+", filename.lower()))

        for word in keywords:
            if word in self.BLACKLIST:
                continue
            if word in self.STATIC_NUTRITION:
                data = self.STATIC_NUTRITION[word]
                logger.info("Static fallback matched: %s", word)
                return [{
                    "name": word.capitalize(),
                    "calories": float(data["calories"]),
                    "protein": float(data["protein"]),
                    "carbs": float(data["carbs"]),
                    "fats": float(data["fats"]),
                    "fiber": float(data.get("fiber", 0)),
                    "sugar": 0.0,
                    "saturatedFat": 0.0,
                    "vitaminA": 0.0, "vitaminC": 0.0, "vitaminD": 0.0, "vitaminB12": 0.0,
                    "calcium": 0.0, "iron": 0.0, "magnesium": 0.0, "potassium": 0.0,
                    "sodium": 0.0, "zinc": 0.0,
                    "healthier_alternative": "",
                    "pro_tip": f"Estimated values for {word}. Adjust serving size as needed.",
                    "servingQuantity": 100.0,
                    "servingUnit": "g",
                    "confidence": 0.5,
                }]
        return None


    def _build_prompt(self, additional_text=""):
        prompt = """
You are identifying foods from an image.

Return ONLY valid JSON.
Do not include markdown fences.
Do not include any explanation text before or after the JSON.

Return this exact structure:
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
      "healthier_alternative": "A short suggestion for a healthier version or alternative",
      "pro_tip": "A short personalized nutrition tip or fun fact about this food"
    }
  ]
}

Rules:
- Identify only food or drink items actually visible in the image.
- Common valid items include apple, banana, salad, cucumber, tomato, rice, cake, pizza, burger, chocolate, milk, tea, coffee, juice, noodles, fruit, vegetables.
- If it's a dish, identify the main components.
- Ignore non-food items like people, furniture, table, bottle, wrapper, textile, toy, fabric, background.
- If no food or drink is confidently visible, return: {"items": []}
- Confidence should be between 0.0 and 1.0.
- Provide reasonable approximate values for macros per 100g.
""".strip()

        if additional_text:
            prompt += f"\nUser context: {additional_text.strip()}"

        prompt += "\n\nAlso ensure the response format remains exactly as specified in the rules, focusing on identified food items."
        return prompt

    def _extract_json_text(self, text: str) -> str:
        if not text:
            return ""

        text = text.strip()

        # Handle explicit JSON code blocks
        match = re.search(r"```(?:json)?(.*?)```", text, re.DOTALL | re.IGNORECASE)
        if match:
            text = match.group(1).strip()

        # Extract first JSON object if extra text exists
        start = text.find("{")
        end = text.rfind("}")
        if start == -1 or end == -1 or end <= start:
            return ""

        return text[start:end + 1].strip()

    def _map_item_to_result(self, item: dict):
        if not isinstance(item, dict):
            return None

        name = (item.get("name") or "").strip()
        if not name:
            return None

        name_lower = name.lower()

        # Reject only obvious junk
        if name_lower in self.BLACKLIST:
            logger.info("Skipping blacklisted label: %s", name)
            return None

        confidence = item.get("confidence", 0.7)
        try:
            confidence = float(confidence)
        except Exception:
            confidence = 0.7

        # Filter out very low confidence overall, or low-confidence potential junk
        if confidence < 0.2:
            logger.info("Skipping low-confidence item: %s (conf=%s)", name, confidence)
            return None

        if confidence < 0.4 and name_lower in self.BLACKLIST:
            logger.info("Skipping low-confidence potential junk: %s (conf=%s)", name, confidence)
            return None

        logger.info("Attempting DB match for name: %s", name)
        db_match = self._get_db_match(name, confidence)
        if db_match:
            return db_match

        logger.info("No DB match. Attempting nutrition API fallback for: %s", name)
        api_match = self._get_api_match(name, confidence)
        if api_match:
            return api_match

        logger.info("No API match. Using AI estimate fallback for: %s", name)
        return self._get_ai_estimate(name, item, confidence)

    def _get_db_match(self, name: str, confidence: float):
        db_food = self._find_food_match(name)
        if db_food:
            logger.info("Matched DB food: input=%s db=%s", name, db_food.name)
            return {
                "name": db_food.name,
                "calories": float(db_food.calories_per_100g),
                "protein": float(db_food.protein_per_100g),
                "carbs": float(db_food.carbs_per_100g),
                "fats": float(db_food.fats_per_100g),
                "servingQuantity": float(db_food.serving_quantity),
                "servingUnit": db_food.serving_unit,
                "confidence": confidence,
            }
        return None

    def _get_api_match(self, name: str, confidence: float):
        edamam_data = self._fetch_edamam_nutrition(name)
        if edamam_data:
            logger.info("Matched Edamam food: input=%s", name)
            edamam_data["confidence"] = confidence
            return edamam_data
        return None

    def _get_ai_estimate(self, name: str, item: dict, confidence: float):
        est = item.get("estimated_per_100g") or {}
        return {
            "name": name,
            "calories": self._safe_float(est.get("calories"), 0.0),
            "protein": self._safe_float(est.get("protein"), 0.0),
            "carbs": self._safe_float(est.get("carbs"), 0.0),
            "fats": self._safe_float(est.get("fats"), 0.0),
            "fiber": self._safe_float(est.get("fiber"), 0.0),
            "sugar": self._safe_float(est.get("sugar"), 0.0),
            "saturatedFat": self._safe_float(est.get("saturatedFat"), 0.0),
            "vitaminA": self._safe_float(est.get("vitaminA"), 0.0),
            "vitaminC": self._safe_float(est.get("vitaminC"), 0.0),
            "vitaminD": self._safe_float(est.get("vitaminD"), 0.0),
            "vitaminB12": self._safe_float(est.get("vitaminB12"), 0.0),
            "calcium": self._safe_float(est.get("calcium"), 0.0),
            "iron": self._safe_float(est.get("iron"), 0.0),
            "magnesium": self._safe_float(est.get("magnesium"), 0.0),
            "potassium": self._safe_float(est.get("potassium"), 0.0),
            "sodium": self._safe_float(est.get("sodium"), 0.0),
            "zinc": self._safe_float(est.get("zinc"), 0.0),
            "healthier_alternative": item.get("healthier_alternative", ""),
            "pro_tip": item.get("pro_tip", ""),
            "servingQuantity": 100.0,
            "servingUnit": "g",
            "confidence": confidence,
        }

    def _find_food_match(self, name: str):
        name = name.strip()
        if not name:
            return None

        # 1. Exact case-insensitive match
        exact = FoodItem.objects.filter(name__iexact=name).first()
        if exact:
            logger.info("Found exact DB match: %s", exact.name)
            return exact

        # 2. Singular/plural normalization
        normalized = name.lower().strip()
        candidates = {normalized}
        if normalized.endswith("s"):
            candidates.add(normalized[:-1])
        else:
            candidates.add(normalized + "s")

        for candidate in candidates:
            match = FoodItem.objects.filter(name__iexact=candidate).first()
            if match:
                logger.info("Found normalized DB match: %s -> %s", candidate, match.name)
                return match

        # 3. Only use whole important words (min 4 chars), not broad icontains
        words = [w.lower() for w in re.findall(r"[A-Za-z]+", name) if len(w) >= 4]
        for word in words:
            if word in self.BLACKLIST:
                continue

            # Strict word match
            match = FoodItem.objects.filter(name__iexact=word).first()
            if match:
                logger.info("Found strict word DB match: %s -> %s", word, match.name)
                return match

        logger.info("No safe DB match found for: %s", name)
        return None

    def _safe_float(self, value, default=0.0):
        try:
            return float(value)
        except Exception:
            return default

    def _fetch_edamam_nutrition(self, query: str):
        if not self.edamam_app_id or not self.edamam_api_key or self.edamam_app_id == 'your_edamam_app_id':
            return None

        try:
            url = "https://api.edamam.com/api/food-database/v2/parser"
            params = {
                "app_id": self.edamam_app_id,
                "app_key": self.edamam_api_key,
                "ingr": query,
                "nutrition-type": "logging"
            }
            response = requests.get(url, params=params, timeout=5)
            response.raise_for_status()
            data = response.json()

            if "hints" in data and len(data["hints"]) > 0:
                food = data["hints"][0]["food"]
                nutrients = food.get("nutrients", {})
                
                return {
                    "name": food.get("label", query),
                    "calories": float(nutrients.get("ENERC_KCAL", 0.0)),
                    "protein": float(nutrients.get("PROCNT", 0.0)),
                    "carbs": float(nutrients.get("CHOCDF", 0.0)),
                    "fats": float(nutrients.get("FAT", 0.0)),
                    "fiber": float(nutrients.get("FIBTG", 0.0)),
                    "servingQuantity": 100.0,
                    "servingUnit": "g",
                }
        except Exception as e:
            logger.warning("Edamam API fallback failed: %s", e)
        
        return None

    def _fallback_from_filename(self, image_file):
        import os
        import re

        filename = getattr(image_file, "name", "") or ""
        base = os.path.splitext(os.path.basename(filename))[0].lower()
        logger.info("Filename fallback starting for base='%s'", base)

        # Extract words of 3+ chars
        words = [w for w in re.findall(r"[a-zA-Z]+", base) if len(w) >= 3]
        logger.info("Extracted filename keywords: %s", words)

        for word in words:
            if word in self.BLACKLIST:
                continue
                
            db_food = self._find_food_match(word)
            if db_food:
                logger.info("Filename fallback MATCH FOUND in DB: '%s' -> '%s'", word, db_food.name)
                return [{
                    "name": db_food.name,
                    "calories": float(db_food.calories_per_100g),
                    "protein": float(db_food.protein_per_100g),
                    "carbs": float(db_food.carbs_per_100g),
                    "fats": float(db_food.fats_per_100g),
                    "servingQuantity": float(db_food.serving_quantity),
                    "servingUnit": db_food.serving_unit,
                    "confidence": 0.6,
                }]

        logger.info("Filename fallback: No DB match found for keywords %s", words)
        return []
