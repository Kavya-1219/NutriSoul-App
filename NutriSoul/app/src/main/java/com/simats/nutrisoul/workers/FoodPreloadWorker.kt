package com.simats.nutrisoul.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.simats.nutrisoul.BuildConfig
import com.simats.nutrisoul.data.FoodRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

@HiltWorker
class FoodPreloadWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: FoodRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val foodItems = repository.searchFoods("apple").first()
            foodItems.forEach { repository.saveCustomFood(it) }
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
