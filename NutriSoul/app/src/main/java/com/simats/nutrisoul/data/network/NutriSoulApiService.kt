package com.simats.nutrisoul.data.network

import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

data class BackendFoodItem(
    val id: Int? = null,
    val name: String,
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fats: Double,
    @SerializedName("servingQuantity") val servingQuantity: Double = 100.0,
    @SerializedName("servingUnit") val servingUnit: String = "g"
)

data class LogFoodRequest(
    @SerializedName("food_name") val foodName: String,
    @SerializedName("calories") val calories: Double,
    @SerializedName("protein") val protein: Double,
    @SerializedName("carbs") val carbs: Double,
    @SerializedName("fats") val fats: Double,
    @SerializedName("quantity") val quantity: Double,
    @SerializedName("meal_type") val mealType: String,
    @SerializedName("date") val date: String // "YYYY-MM-DD"
)

data class LogFoodResponse(
    val id: Int,
    @SerializedName("food_name") val foodName: String,
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fats: Double,
    val quantity: Double,
    @SerializedName("meal_type") val mealType: String,
    val date: String,
    @SerializedName("created_at") val createdAt: String
)

data class TodayMacrosResponse(
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fats: Double
)

data class FoodScanResponse(
    @SerializedName("detected_items") val detectedItems: List<DetectedFoodItem>,
    val message: String
)

data class DetectedFoodItem(
    val name: String,
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fats: Double,
    val fiber: Double = 0.0,
    val sugar: Double = 0.0,
    @SerializedName("saturatedFat") val saturatedFat: Double = 0.0,
    @SerializedName("vitaminA") val vitaminA: Double = 0.0,
    @SerializedName("vitaminC") val vitaminC: Double = 0.0,
    @SerializedName("vitaminD") val vitaminD: Double = 0.0,
    @SerializedName("vitaminB12") val vitaminB12: Double = 0.0,
    val calcium: Double = 0.0,
    val iron: Double = 0.0,
    val magnesium: Double = 0.0,
    val potassium: Double = 0.0,
    val sodium: Double = 0.0,
    val zinc: Double = 0.0,
    @SerializedName("healthier_alternative") val healthierAlternative: String = "",
    @SerializedName("pro_tip") val proTip: String = "",
    @SerializedName("servingQuantity") val servingQuantity: Double = 100.0,
    @SerializedName("servingUnit") val servingUnit: String = "g",
    val confidence: Double = 1.0,
    val source: String = ""
)

data class DailyMealPlanResponse(
    @SerializedName("targetCalories") val targetCalories: Int,
    val meals: List<DailyMealEntryDto>
)

data class DailyMealEntryDto(
    @SerializedName("mealType") val mealType: String,
    val title: String,
    val calories: Int,
    val protein: Int,
    val carbs: Int,
    val fats: Int,
    @SerializedName("isEaten") val isEaten: Boolean = false,
    val items: List<MealTemplateItemDto>
)

data class MealTemplateItemDto(
    val name: String,
    val quantity: String,
    val calories: Int,
    val protein: Int,
    val carbs: Int,
    val fats: Int
)

data class SwapMealRequest(
    @SerializedName("meal_type") val mealType: String,
    @SerializedName("meal_template_id") val mealTemplateId: Int
)

data class MealTemplateDto(
    val id: Int,
    @SerializedName("mealType") val mealType: String,
    val title: String,
    val calories: Int,
    val protein: Int,
    val carbs: Int,
    val fats: Int,
    val items: List<MealTemplateItemDto>
)

data class AiTipDto(
    val title: String,
    val description: String,
    val icon: String,
    @SerializedName("icon_bg") val iconBg: String,
    @SerializedName("icon_tint") val iconTint: String,
    @SerializedName("card_bg") val cardBg: String,
    @SerializedName("border_color") val borderColor: String,
    val category: String? = null
)

data class ChatRequest(val message: String)
data class ChatResponse(val response: String)

data class NutritionInsightsResponse(
    val hasData: Boolean,
    val weeklyConsistency: Float,
    val consistencyPercent: Int,
    val daysLogged: Int,
    val totalDays: Int,
    val averageCalories: Int,
    val targetCalories: Int,
    val averageProtein: Double,
    val averageCarbs: Double,
    val averageFats: Double,
    val proteinPercentage: Int,
    val carbsPercentage: Int,
    val fatsPercentage: Int,
    val calorieStatus: CalorieStatusDto
)

data class CalorieStatusDto(
    val label: String,
    val tone: String,
    val emoji: String
)

data class MarkEatenRequest(
    @SerializedName("meal_type") val mealType: String,
    @SerializedName("is_eaten") val isEaten: Boolean,
    val date: String? = null
)

data class MarkEatenResponse(
    val message: String,
    @SerializedName("is_eaten") val isEaten: Boolean,
    val calories: Int = 0,
    val protein: Int = 0,
    val carbs: Int = 0,
    val fats: Int = 0,
    val name: String = "",
    @SerializedName("meal_type") val mealType: String = ""
)

data class FoodHistoryResponse(
    val results: List<FoodLogDto>
)

data class FoodLogDto(
    val id: Int,
    val name: String,
    @SerializedName("calories_per_unit") val caloriesPerUnit: Double,
    @SerializedName("protein_per_unit") val proteinPerUnit: Double,
    @SerializedName("carbs_per_unit") val carbsPerUnit: Double,
    @SerializedName("fats_per_unit") val fatsPerUnit: Double,
    val quantity: Double,
    val unit: String,
    @SerializedName("timestamp_millis") val timestampMillis: Long
)

data class DailyStepsDto(
    val date: String,
    @SerializedName("auto_steps") val autoSteps: Int,
    @SerializedName("manual_steps") val manualSteps: Int,
    @SerializedName("goal_steps") val goalSteps: Int,
    @SerializedName("total_steps") val totalSteps: Int,
    @SerializedName("updated_at") val updatedAt: String?
)

data class WeeklyStepsResponse(
    val start: String,
    val days: List<DailyStepsDto>,
    @SerializedName("avg_7_day") val avg7Day: Float
)

data class ManualStepsRequest(
    @SerializedName("delta_steps") val deltaSteps: Int,
    val date: String? = null
)

interface NutriSoulApiService {
    @GET("foods/search/")
    suspend fun searchFoods(@Query("query") query: String): List<BackendFoodItem>

    @POST("log-food/")
    suspend fun logFood(@Body log: LogFoodRequest): LogFoodResponse

    @GET("today-macros/")
    suspend fun getTodayMacros(@Query("date") date: String? = null): TodayMacrosResponse

    @Multipart
    @POST("food-scan/")
    suspend fun scanFood(
        @Part image: MultipartBody.Part,
        @Part("text") text: RequestBody? = null
    ): FoodScanResponse

    @GET("meal-plan/today/")
    suspend fun getTodayMealPlan(@Query("date") date: String? = null): DailyMealPlanResponse

    @POST("meal-plan/today/")
    suspend fun refreshMealPlan(): DailyMealPlanResponse

    @GET("meal-plan/alternatives/")
    suspend fun getMealAlternatives(@Query("meal_type") mealType: String): List<MealTemplateDto>

    @POST("meal-plan/swap/")
    suspend fun swapMeal(@Body request: SwapMealRequest): DailyMealPlanResponse

    @GET("ai-tips/")
    suspend fun getAiTips(): List<AiTipDto>

    @POST("ai-assistant/")
    suspend fun getAiAssistantResponse(@Body request: ChatRequest): ChatResponse

    @GET("nutrition-insights/")
    suspend fun getNutritionInsights(): NutritionInsightsResponse

    @POST("meal-plan/mark-eaten/")
    suspend fun markMealEaten(@Body request: MarkEatenRequest): MarkEatenResponse

    @GET("home/")
    suspend fun getHomeData(): ProfileResponseDto

    @GET("food-history/")
    suspend fun getFoodHistory(@Query("days") days: Int = 14): FoodHistoryResponse

    @GET("steps/today/")
    suspend fun getTodaySteps(): DailyStepsDto

    @PUT("steps/today/")
    suspend fun updateTodaySteps(@Body steps: Map<String, Int>): DailyStepsDto

    @GET("steps/weekly/")
    suspend fun getWeeklySteps(@Query("start") start: String? = null): WeeklyStepsResponse

    @POST("steps/manual-log/")
    suspend fun logManualSteps(@Body request: ManualStepsRequest): Map<String, Any>
}
