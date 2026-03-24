package com.simats.nutrisoul.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.simats.nutrisoul.data.repository.StepsRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class StepsSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val stepsRepository: StepsRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            stepsRepository.syncSteps()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
    
    companion object {
        const val WORK_NAME = "StepsSyncWorker"
    }
}
