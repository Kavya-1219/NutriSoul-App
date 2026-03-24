package com.simats.nutrisoul.ui.steps

data class StepsUiState(
    val todaySteps: Long = 0,
    val stepsGoal: Int = 10000,
    val weeklyAverage: Long = 0,
    val caloriesBurned: Int = 0,
    val distanceKm: Double = 0.0,
    val healthConnectStatus: HealthConnectStatus = HealthConnectStatus.NotInstalled,
    val hasPermissions: Boolean = false,

    // ✅ new
    val autoTrackingEnabled: Boolean = false,
    val manualLogs: List<ManualStepsLog> = emptyList()
)

data class ManualStepsLog(
    val id: String,
    val steps: Int,
    val timestamp: Long
)

enum class HealthConnectStatus {
    Installed, NotInstalled, NotAvailable
}

sealed class StepsScreenEvent {
    data class OnPermissionResult(val granted: Boolean) : StepsScreenEvent()
    object OnRequestPermissions : StepsScreenEvent()
    data class OnGoalSelected(val goal: Int) : StepsScreenEvent()
    object OnSyncSteps : StepsScreenEvent()
    data class OnToggleAutoTracking(val enabled: Boolean) : StepsScreenEvent()
    data class OnAddManualSteps(val steps: Int) : StepsScreenEvent()
    data class OnDeleteManualLog(val id: String) : StepsScreenEvent()
}
