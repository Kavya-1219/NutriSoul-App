package com.simats.nutrisoul.data.network

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.gson.Gson
import com.simats.nutrisoul.BuildConfig
import com.simats.nutrisoul.data.meal.UserNutritionProfile
import com.simats.nutrisoul.data.meal.model.Meal
import com.simats.nutrisoul.data.meal.model.MealItem
import com.simats.nutrisoul.data.meal.model.MealPlan
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiService @Inject constructor() {

    private val apiKey = BuildConfig.NUTRITION_API_KEY
    private val modelName = "gemini-2.0-flash"
    private val fallbackModelName = "gemini-1.5-flash"

    private val generativeModel by lazy {
        GenerativeModel(modelName = modelName, apiKey = apiKey)
    }
    
    private val fallbackModel by lazy {
        GenerativeModel(modelName = fallbackModelName, apiKey = apiKey)
    }

    suspend fun getChatResponse(message: String): String = withContext(Dispatchers.IO) {
        if (apiKey == "dummy_key" || apiKey.isBlank()) {
            return@withContext "API Key is missing or invalid. Please check your local.properties file."
        }
        try {
            // Attempt with primary model
            val response = generativeModel.generateContent(message)
            response.text ?: "I am sorry, I couldn't generate a response."
        } catch (e: Throwable) {
            val errorMsg = e.localizedMessage ?: ""
            Log.w("GeminiService", "Primary model ($modelName) failed: $errorMsg")
            
            // Fallback attempt
            if (errorMsg.contains("429") || errorMsg.contains("quota", ignoreCase = true)) {
                try {
                    Log.i("GeminiService", "Attempting fallback to $fallbackModelName")
                    val fallbackResponse = fallbackModel.generateContent(message)
                    return@withContext fallbackResponse.text ?: "I am sorry, I couldn't generate a response."
                } catch (fallbackEx: Throwable) {
                    Log.e("GeminiService", "Fallback also failed", fallbackEx)
                }
            }

            Log.e("GeminiService", "Chat Error", e)
            if (errorMsg.contains("429") || errorMsg.contains("quota", ignoreCase = true) || errorMsg.contains("limit", ignoreCase = true)) {
                "I've reached my message limit (15 req/min). Please wait a minute and try again! ⏳"
            } else {
                "Sorry, I encountered a connection issue ($errorMsg). Please check your internet or try again later."
            }
        }
    }

    suspend fun generateMealPlan(profile: UserNutritionProfile): MealPlan? = withContext(Dispatchers.IO) {
        if (apiKey == "dummy_key" || apiKey.isBlank()) return@withContext null

        val prompt = """
            Generate a daily meal plan for a user with the following profile:
            - Goal: ${profile.goal}
            - Diet Type: ${profile.dietType}
            - Allergies: ${profile.allergies.joinToString()}
            - Health Conditions: ${profile.healthConditions.joinToString()}
            - Target Calories: ${profile.targetCalories}

            Return ONLY a JSON object exactly in this format:
            {
              "targetCalories": ${profile.targetCalories},
              "meals": [
                {
                  "mealType": "breakfast",
                  "title": "Meal Title",
                  "calories": 400,
                  "protein": 20,
                  "carbs": 50,
                  "fats": 10,
                  "items": [
                    {"name": "Item Name", "quantity": "100g", "calories": 200, "protein": 10, "carbs": 25, "fats": 5}
                  ]
                },
                ... (include lunch, dinner, and snack)
              ]
            }
        """.trimIndent()

        try {
            val response = generativeModel.generateContent(prompt)
            val json = response.text?.substringAfter("{")?.substringBeforeLast("}")?.let { "{$it}" }
            if (json != null) {
                Gson().fromJson(json, MealPlan::class.java)
            } else null
        } catch (e: Exception) {
            Log.e("GeminiService", "Meal Gen Error", e)
            null
        }
    }
}
