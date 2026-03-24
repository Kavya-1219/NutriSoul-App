package com.simats.nutrisoul.data

import com.simats.nutrisoul.data.meal.model.Meal
import com.simats.nutrisoul.data.meal.model.MealItem
import com.simats.nutrisoul.data.meal.model.MealPlan
import com.simats.nutrisoul.data.network.NutriSoulApiService
import com.simats.nutrisoul.data.network.SwapMealRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MealRepository @Inject constructor(
    private val apiService: NutriSoulApiService
) {
    suspend fun getTodayMealPlan(date: String? = null): MealPlan = withContext(Dispatchers.IO) {
        val response = apiService.getTodayMealPlan(date)
        MealPlan(
            targetCalories = response.targetCalories,
            meals = response.meals?.map { it.toDomain() } ?: emptyList()
        )
    }

    suspend fun refreshMealPlan(): MealPlan = withContext(Dispatchers.IO) {
        val response = apiService.refreshMealPlan()
        MealPlan(
            targetCalories = response.targetCalories,
            meals = response.meals?.map { it.toDomain() } ?: emptyList()
        )
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

    suspend fun getAiTips(): List<com.simats.nutrisoul.data.network.AiTipDto> = withContext(Dispatchers.IO) {
        apiService.getAiTips()
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
