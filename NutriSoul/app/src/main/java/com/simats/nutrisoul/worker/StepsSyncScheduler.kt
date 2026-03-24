package com.simats.nutrisoul.worker

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class StepsSyncScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val workManager = WorkManager.getInstance(context)

    fun scheduleSync() {
        val request = PeriodicWorkRequestBuilder<StepsSyncWorker>(15, TimeUnit.MINUTES)
            .build()
            
        workManager.enqueueUniquePeriodicWork(
            StepsSyncWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    fun cancelSync() {
        workManager.cancelUniqueWork(StepsSyncWorker.WORK_NAME)
    }
}
