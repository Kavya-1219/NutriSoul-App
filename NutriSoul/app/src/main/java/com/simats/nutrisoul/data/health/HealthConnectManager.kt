package com.simats.nutrisoul.data.health

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.time.TimeRangeFilter
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HealthConnectManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val _healthConnectClient by lazy { HealthConnectClient.getOrCreate(context) }

    val availabilityStatus: Int
        get() = HealthConnectClient.getSdkStatus(context)

    fun getHealthConnectClient(): HealthConnectClient? {
        return if (availabilityStatus == HealthConnectClient.SDK_AVAILABLE) {
            _healthConnectClient
        } else {
            null
        }
    }

    fun isProviderAvailable(): Boolean {
        return availabilityStatus == HealthConnectClient.SDK_AVAILABLE
    }

    suspend fun hasPermissions(permissions: Set<String>): Boolean {
        return _healthConnectClient.permissionController.getGrantedPermissions().containsAll(permissions)
    }

    fun requestPermissionsActivityContract(): ActivityResultContract<Set<String>, Set<String>> {
        return PermissionController.createRequestPermissionResultContract()
    }
    
    fun installHealthConnect() {
        val intent = Intent("androidx.health.connect.action.HEALTH_CONNECT_SETTINGS")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    suspend fun readTodaySteps(): Long {
        val startOfDay = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()
        val now = Instant.now()

        if (!hasPermissions(PERMISSIONS)) return 0L

        return try {
            val response = _healthConnectClient.aggregate(
                AggregateRequest(
                    metrics = setOf(StepsRecord.COUNT_TOTAL),
                    timeRangeFilter = TimeRangeFilter.between(startOfDay, now)
                )
            )
            response[StepsRecord.COUNT_TOTAL] ?: 0L
        } catch (e: Exception) {
            0L
        }
    }

    suspend fun readLast7DaysSteps(): List<Long> {
        val today = LocalDate.now()
        val sevenDaysAgo = today.minus(6, ChronoUnit.DAYS)
        val dailySteps = mutableListOf<Long>()

        for (i in 0..6) {
            val date = sevenDaysAgo.plus(i.toLong(), ChronoUnit.DAYS)
            val startOfDay = date.atStartOfDay(ZoneId.systemDefault()).toInstant()
            val endOfDay = date.plus(1, ChronoUnit.DAYS).atStartOfDay(ZoneId.systemDefault()).toInstant()

            try {
                val response = _healthConnectClient.aggregate(
                    AggregateRequest(
                        metrics = setOf(StepsRecord.COUNT_TOTAL),
                        timeRangeFilter = TimeRangeFilter.between(startOfDay, endOfDay)
                    )
                )
                dailySteps.add(response[StepsRecord.COUNT_TOTAL] ?: 0L)
            } catch (e: Exception) {
                dailySteps.add(0L)
            }
        }
        return dailySteps
    }
    
    companion object {
        val PERMISSIONS = setOf(
            HealthPermission.getReadPermission(StepsRecord::class)
        )
    }
}