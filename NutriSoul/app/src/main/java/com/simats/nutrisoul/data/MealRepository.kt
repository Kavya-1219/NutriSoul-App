package com.simats.nutrisoul.data

import com.google.gson.Gson
import com.simats.nutrisoul.data.meal.UserNutritionProfile
import com.simats.nutrisoul.data.meal.model.Meal
import com.simats.nutrisoul.data.meal.model.MealItem
import com.simats.nutrisoul.data.meal.model.MealPlan
import com.simats.nutrisoul.data.network.GeminiService
import com.simats.nutrisoul.data.network.NutriSoulApiService
import com.simats.nutrisoul.data.network.SwapMealRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MealRepository @Inject constructor(
    private val apiService: NutriSoulApiService,
    private val geminiService: GeminiService
) {
    suspend fun getTodayMealPlan(date: String? = null, profile: UserNutritionProfile? = null): MealPlan = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getTodayMealPlan(date)
            val backendMeals = response.meals?.map { it.toDomain() } ?: emptyList()
            val eatenStatusMap = backendMeals.associate { it.mealType to it.isEaten }

            val plan = MealPlan(
                targetCalories = response.targetCalories,
                meals = backendMeals.distinctBy { it.mealType }
            )
            
            // If backend returns generic "Custom" meals or very few meals, try Gemini as upgrade
            if ((plan.meals.size < 3 || plan.meals.any { it.title.contains("Custom", ignoreCase = true) }) && profile != null) {
                val aiPlan = geminiService.generateMealPlan(profile)
                if (aiPlan != null && aiPlan.meals.isNotEmpty()) {
                    // Senior Review: Preserve "eaten" status from backend when upgrading to AI plan
                    val patchedMeals = aiPlan.meals.map { aiMeal ->
                        aiMeal.copy(isEaten = eatenStatusMap[aiMeal.mealType] ?: false)
                    }
                    return@withContext aiPlan.copy(meals = patchedMeals)
                }
            }
            
            // If still no real meals, use MealPlanGenerator as final local fallback
            if (plan.meals.any { it.title.contains("Custom", ignoreCase = true) } && profile != null) {
                val localPlan = com.simats.nutrisoul.data.meal.MealPlanGenerator().generate(profile, date ?: "today")
                val patchedMeals = localPlan.meals.map { m -> m.copy(isEaten = eatenStatusMap[m.mealType] ?: false) }
                return@withContext localPlan.copy(meals = patchedMeals)
            }
            plan
        } catch (e: Exception) {
            if (profile != null) {
                val aiPlan = geminiService.generateMealPlan(profile)
                if (aiPlan != null) return@withContext aiPlan
            }
            throw e
        }
    }

    suspend fun refreshMealPlan(profile: UserNutritionProfile? = null): MealPlan = withContext(Dispatchers.IO) {
        // Try backend first
        try {
            val response = apiService.refreshMealPlan()
            val backendMeals = response.meals?.map { it.toDomain() } ?: emptyList()
            val eatenStatusMap = backendMeals.associate { it.mealType to it.isEaten }

            val plan = MealPlan(
                targetCalories = response.targetCalories,
                meals = backendMeals.distinctBy { it.mealType }
            )
            
            // If backend returns generic "Custom" meals, try Gemini as upgrade
            if ((plan.meals.isEmpty() || plan.meals.any { it.title.contains("Custom", ignoreCase = true) }) && profile != null) {
                val aiPlan = geminiService.generateMealPlan(profile)
                if (aiPlan != null && aiPlan.meals.isNotEmpty()) {
                    val patchedMeals = aiPlan.meals.map { m -> m.copy(isEaten = eatenStatusMap[m.mealType] ?: false) }
                    return@withContext aiPlan.copy(meals = patchedMeals)
                }
            }

            // Local fallback
            if (plan.meals.any { it.title.contains("Custom", ignoreCase = true) } && profile != null) {
                val localPlan = com.simats.nutrisoul.data.meal.MealPlanGenerator().generate(profile, "refresh-${System.currentTimeMillis()}")
                val patchedMeals = localPlan.meals.map { m -> m.copy(isEaten = eatenStatusMap[m.mealType] ?: false) }
                return@withContext localPlan.copy(meals = patchedMeals)
            }
            return@withContext plan
        } catch (e: Exception) {
            // Fallback to Gemini if backend fails
            if (profile != null) {
                val aiPlan = geminiService.generateMealPlan(profile)
                if (aiPlan != null) return@withContext aiPlan
                
                // Final fallback to generator
                return@withContext com.simats.nutrisoul.data.meal.MealPlanGenerator().generate(profile, "fallback-${System.currentTimeMillis()}")
            }
            throw e
        }
    }

    suspend fun getMealAlternatives(mealType: String): List<Meal> = withContext(Dispatchers.IO) {
        val response = apiService.getMealAlternatives(mealType)
        response?.map { it.toDomain() } ?: emptyList()
    }

    suspend fun swapMeal(mealType: String, templateId: Int): MealPlan = withContext(Dispatchers.IO) {
        val response = apiService.swapMeal(SwapMealRequest(mealType, templateId))
        MealPlan(
            targetCalories = response.targetCalories,
            meals = response.meals?.map { it.toDomain() } ?: emptyList()
        )
    }

    suspend fun getAiTips(profile: UserNutritionProfile? = null): List<com.simats.nutrisoul.data.network.AiTipDto> = withContext(Dispatchers.IO) {
        try {
            val tips = apiService.getAiTips()
            if (tips.isNotEmpty()) return@withContext tips
            
            // If backend tips are empty, generate one via Gemini
            if (profile != null) {
                val aiTip = generateGeminiTip(profile)
                if (aiTip != null) return@withContext listOf(aiTip)
            }
            emptyList()
        } catch (e: Exception) {
            if (profile != null) {
                val aiTip = generateGeminiTip(profile)
                if (aiTip != null) return@withContext listOf(aiTip)
            }
            emptyList()
        }
    }

    private suspend fun generateGeminiTip(profile: UserNutritionProfile): com.simats.nutrisoul.data.network.AiTipDto? {
        val prompt = "Based on this nutrition profile: Goal: ${profile.goal}, Diet: ${profile.dietType}, Target: ${profile.targetCalories} kcal. Give ONE short, practical nutrition tip. Return ONLY a JSON object: {\"title\": \"Tip Title\", \"description\": \"Tip content\", \"icon\": \"star\", \"icon_bg\": \"#E8F5E9\", \"icon_tint\": \"#2ECC71\", \"card_bg\": \"#F1F8E9\", \"border_color\": \"#C5E1A5\"}"
        val response = geminiService.getChatResponse(prompt)
        return try {
            val json = response.substringAfter("{").substringBeforeLast("}").let { "{$it}" }
            Gson().fromJson(json, com.simats.nutrisoul.data.network.AiTipDto::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun markMealEaten(mealType: String, isEaten: Boolean, date: String? = null): com.simats.nutrisoul.data.network.MarkEatenResponse = withContext(Dispatchers.IO) {
        apiService.markMealEaten(com.simats.nutrisoul.data.network.MarkEatenRequest(mealType, isEaten, date))
    }

    private fun com.simats.nutrisoul.data.network.DailyMealEntryDto.toDomain(): Meal {
        return Meal(
            id = 0, // Entries don't expose template ID directly yet
            mealType = mealType,
            title = title,
            calories = calories,
            protein = protein,
            carbs = carbs,
            fats = fats,
            isEaten = isEaten,
            items = items?.map { it.toDomain() } ?: emptyList()
        )
    }

    private fun com.simats.nutrisoul.data.network.MealTemplateItemDto.toDomain(): MealItem {
        return MealItem(
            name = name,
            quantity = quantity,
            calories = calories,
            protein = protein,
            carbs = carbs,
            fats = fats
        )
    }

    private fun com.simats.nutrisoul.data.network.MealTemplateDto.toDomain(): Meal {
        return Meal(
            id = id,
            mealType = mealType,
            title = title,
            calories = calories,
            protein = protein,
            carbs = carbs,
            fats = fats,
            items = items?.map { it.toDomain() } ?: emptyList()
        )
    }
}
