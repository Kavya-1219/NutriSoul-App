package com.simats.nutrisoul.data

import com.simats.nutrisoul.data.models.toFoodItem
import com.simats.nutrisoul.data.models.DailyTotals
import com.simats.nutrisoul.data.models.FoodItem
import com.simats.nutrisoul.data.models.FoodLog
import com.simats.nutrisoul.data.models.toIntakeEntity
import com.simats.nutrisoul.data.models.toCustomFoodEntity
import com.simats.nutrisoul.data.network.NutritionApiService
import com.simats.nutrisoul.data.network.NutriSoulApiService
import com.simats.nutrisoul.data.network.ProfileResponseDto
import com.simats.nutrisoul.data.network.FoodScanResponse
import com.simats.nutrisoul.data.network.LogFoodRequest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import kotlinx.coroutines.Dispatchers
import java.time.LocalDate
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class FoodRepository @Inject constructor(
    private val foodDao: FoodDao,
    private val userDao: UserDao,
    private val intakeDao: IntakeDao,
    private val customFoodDao: CustomFoodDao,
    private val foodLogDao: FoodLogDao,
    private val nutritionApiService: NutritionApiService,
    private val nutriSoulApiService: NutriSoulApiService
) {

    fun observeTodayTotals(email: String): Flow<DailyTotals> {
        val safeEmail = email.trim().lowercase()
        val today = LocalDate.now().toString() // "yyyy-MM-dd"
        return intakeDao.observeTotalsForDate(today, safeEmail)
    }

    fun observeLogsBetween(email: String, startDate: LocalDate, endDate: LocalDate): Flow<List<IntakeEntity>> {
        val safeEmail = email.trim().lowercase()
        return intakeDao.getLogsBetween(safeEmail, startDate.toString(), endDate.toString())
    }

    fun searchFoods(query: String): Flow<List<FoodItem>> {
        return flow {
            try {
                val results = nutriSoulApiService.searchFoods(query)
                emit(results.map { it.toFoodItem() })
            } catch (e: Exception) {
                emit(emptyList<FoodItem>())
            }
        }.flowOn(Dispatchers.IO)
    }

    suspend fun getTodayMacros(email: String): Flow<DailyTotals?> = flow {
        try {
            val response = nutriSoulApiService.getTodayMacros()
            val totals = DailyTotals(
                calories = response.calories,
                protein = response.protein,
                carbs = response.carbs,
                fats = response.fats
            )
            
            // Sync with local UserProfile
            val safeEmail = email.trim().lowercase()
            val user = userDao.getUserByEmail(safeEmail).firstOrNull()
            if (user != null) {
                userDao.updateUser(user.copy(
                    todaysCalories = response.calories.toInt()
                ))
            }

            // Also update IntakeEntity if we want to show it in History/Totals specifically
            // For now, updating UserProfile is enough for Home screen progress bars
            
            emit(totals)
        } catch (e: Exception) {
            emit(null)
        }
    }.flowOn(Dispatchers.IO)

    suspend fun scanFood(image: okhttp3.MultipartBody.Part, text: String?): Flow<FoodScanResponse?> = flow {
        try {
            val mediaType = "text/plain".toMediaTypeOrNull()
            val textBody = text?.let { okhttp3.RequestBody.create(mediaType, it) }
            val response = nutriSoulApiService.scanFood(image, textBody)
            emit(response)
        } catch (e: Exception) {
            println("FoodRepository: Scan failed: ${e.message}")
            e.printStackTrace()
            emit(null)
        }
    }.flowOn(Dispatchers.IO)

    suspend fun addFoodToDailyIntake(email: String, foodLog: FoodLog, mealType: String, factor: Double) {
        val safeEmail = email.trim().lowercase()
        
        try {
            android.util.Log.d("REQUEST", "name=${foodLog.name}, calories=${foodLog.calories}, qty=${foodLog.quantity}, factor=$factor")
            val response = nutriSoulApiService.logFood(
                LogFoodRequest(
                    foodName = foodLog.name,
                    calories = foodLog.calories,
                    protein = foodLog.protein,
                    carbs = foodLog.carbs,
                    fats = foodLog.fats,
                    quantity = foodLog.quantity,
                    mealType = mealType,
                    date = LocalDate.now().toString()
                )
            )
            android.util.Log.d("API", "SUCCESS: $response")
            
            // Save locally ONLY after successful backend sync
            val entity = foodLog.toIntakeEntity().copy(
                userEmail = safeEmail, 
                date = LocalDate.now().toString(),
                id = 0 // Auto-increment from Room
            )
            intakeDao.insert(entity)
            android.util.Log.d("FOOD_SYNC", "Local Room sync successful for daily intake: ${foodLog.name}")

            // ALSO save to history (FoodLogEntity) to ensure consistency
            val historyEntity = FoodLogEntity(
                userEmail = safeEmail,
                name = foodLog.name,
                caloriesPerUnit = (foodLog.calories / factor).toFloat(),
                proteinPerUnit = (foodLog.protein / factor).toFloat(),
                carbsPerUnit = (foodLog.carbs / factor).toFloat(),
                fatsPerUnit = (foodLog.fats / factor).toFloat(),
                quantity = factor.toFloat(),
                unit = foodLog.unit,
                timestampMillis = System.currentTimeMillis()
            )
            foodLogDao.insert(historyEntity)
            android.util.Log.d("FOOD_SYNC", "Local Room sync successful for history: ${foodLog.name}")
            
        } catch (e: retrofit2.HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            android.util.Log.e("API", "HTTP ERROR ${e.code()} → $errorBody")
            throw e
        } catch (e: Exception) {
            android.util.Log.e("API", "NETWORK ERROR → ${e.message}")
            throw e 
        }
    }

    fun getHomeData(): Flow<ProfileResponseDto?> = flow {
        try {
            val response = nutriSoulApiService.getHomeData()
            emit(response)
        } catch (e: Exception) {
            emit(null)
        }
    }.flowOn(Dispatchers.IO)

    suspend fun saveCustomFood(foodItem: FoodItem) {
        customFoodDao.insert(foodItem.toCustomFoodEntity())
    }
}
