package com.simats.nutrisoul.data.repository

import com.simats.nutrisoul.data.datastore.UserPreferencesRepository
import com.simats.nutrisoul.data.health.HealthConnectManager
import com.simats.nutrisoul.data.StepsDao
import com.simats.nutrisoul.data.StepsEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import com.simats.nutrisoul.data.network.NutriSoulApiService
import com.simats.nutrisoul.data.network.ManualStepsRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class StepsRepository @Inject constructor(
    private val healthConnectManager: HealthConnectManager,
    private val stepsDao: StepsDao,
    private val userPreferences: UserPreferencesRepository,
    private val apiService: NutriSoulApiService
) {
    val todaySteps: Flow<StepsEntity?> = stepsDao.getStepsForDate(LocalDate.now())

    fun getWeeklySteps(): Flow<List<StepsEntity>> {
        val sevenDaysAgo = LocalDate.now().minus(6, ChronoUnit.DAYS)
        return stepsDao.getStepsFrom(sevenDaysAgo)
    }

    val stepsGoal: Flow<Int> = userPreferences.stepsGoal

    suspend fun setStepsGoal(goal: Int) {
        userPreferences.setStepsGoal(goal)
    }

    /**
     * Reads steps from Health Connect, saves to Room, and syncs to the backend.
     */
    suspend fun syncSteps() {
        val stepsCount = healthConnectManager.readTodaySteps()
        val goal = stepsGoal.first()

        // Sync with backend FIRST
        try {
            withContext(Dispatchers.IO) {
                // Cast to Int for API compatibility
                apiService.updateTodaySteps(mapOf(
                    "auto_steps" to stepsCount.toInt(),
                    "goal_steps" to goal
                ))
            }
            
            // Save locally ONLY after successful backend sync
            val entity = StepsEntity(
                date = LocalDate.now(),
                steps = stepsCount,
                goal = goal
            )
            stepsDao.upsert(entity)
            android.util.Log.d("SYNC", "Steps successfully synced to backend and local Room")
            
        } catch (e: Exception) {
            android.util.Log.e("SYNC", "Failed to sync steps to backend: ${e.message}")
            // Requirement check: Backend is the primary source of truth.
            // If sync fails, we don't update local state to prevent desync.
            throw e
        }
    }

    suspend fun fetchStepsFromBackend() {
        try {
            val response = withContext(Dispatchers.IO) {
                apiService.getTodaySteps()
            }
            val entity = StepsEntity(
                date = LocalDate.parse(response.date),
                steps = response.autoSteps.toLong(),
                manualSteps = response.manualSteps.toLong(),
                goal = response.goalSteps
            )
            stepsDao.upsert(entity)
        } catch (e: Exception) {
            // Handle error
        }
    }

    suspend fun logManualSteps(delta: Int) {
        try {
            withContext(Dispatchers.IO) {
                apiService.logManualSteps(ManualStepsRequest(deltaSteps = delta))
            }
            // Update local Room too
            val today = LocalDate.now()
            val existing = stepsDao.getStepsForDate(today).first()
            if (existing != null) {
                stepsDao.upsert(existing.copy(
                    manualSteps = (existing.manualSteps ?: 0L) + delta.toLong()
                ))
            } else {
                stepsDao.upsert(StepsEntity(
                    date = today,
                    manualSteps = delta.toLong(),
                    steps = 0L,
                    goal = stepsGoal.first()
                ))
            }
        } catch (e: Exception) {
            // Handle error
        }
    }
}
