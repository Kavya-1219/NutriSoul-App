package com.simats.nutrisoul.data

import com.simats.nutrisoul.data.network.NutriSoulApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FoodLogRepository @Inject constructor(
    private val dao: FoodLogDao,
    private val nutriSoulApiService: NutriSoulApiService
) {

    fun observeLast7Days(email: String, startMillis: Long, endMillis: Long): Flow<List<FoodLogEntity>> {
        val safeEmail = email.trim().lowercase()
        return dao.observeLogsBetween(safeEmail, startMillis, endMillis)
    }

    suspend fun addLog(log: FoodLogEntity) {
        try {
            // Sync with backend FIRST
            nutriSoulApiService.logFood(
                com.simats.nutrisoul.data.network.LogFoodRequest(
                    foodName = log.name,
                    calories = log.caloriesPerUnit.toDouble(),
                    protein = log.proteinPerUnit.toDouble(),
                    carbs = log.carbsPerUnit.toDouble(),
                    fats = log.fatsPerUnit.toDouble(),
                    quantity = log.quantity.toDouble(),
                    mealType = "Manual Entry", // "Generic" is invalid for backend
                    date = java.time.LocalDate.now().toString()
                )
            )
            // Save locally ONLY after success
            dao.insert(log)
            android.util.Log.d("SYNC", "Food log successfully synced to backend and Room")
        } catch (e: retrofit2.HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            android.util.Log.e("SYNC", "HTTP ${e.code()} → $errorBody")
            throw e
        } catch (e: Exception) {
            android.util.Log.e("SYNC", "Failed to sync manual food log: ${e.message}")
            throw e
        }
    }

    suspend fun refreshHistory(email: String) = withContext(Dispatchers.IO) {
        val safeEmail = email.trim().lowercase()
        try {
            val response = nutriSoulApiService.getFoodHistory()
            val entities = response.results.map { dto ->
                FoodLogEntity(
                    id = dto.id.toLong(),
                    userEmail = safeEmail,
                    name = dto.name,
                    caloriesPerUnit = dto.caloriesPerUnit.toFloat(),
                    proteinPerUnit = dto.proteinPerUnit.toFloat(),
                    carbsPerUnit = dto.carbsPerUnit.toFloat(),
                    fatsPerUnit = dto.fatsPerUnit.toFloat(),
                    quantity = dto.quantity.toFloat(),
                    unit = dto.unit,
                    timestampMillis = dto.timestampMillis
                )
            }
            dao.insertAll(entities)
        } catch (e: Exception) {
            // Log or handle sync error
        }
    }
}
