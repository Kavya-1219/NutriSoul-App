package com.simats.nutrisoul

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class MindCareViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(MindCareUiState())
    val uiState: StateFlow<MindCareUiState> = _uiState.asStateFlow()

    fun init(context: Context, userEmail: String) {
        viewModelScope.launch {
            // remember last user for boot restore
            MindCarePrefs.setLastUser(context, userEmail)

            // Load persisted state
            val schedule = MindCarePrefs.loadSchedule(context, userEmail)
            val reminderEnabled = MindCarePrefs.loadReminderEnabled(context, userEmail)
            val logs = MindCarePrefs.loadLogs(context, userEmail)
            val weeklyAvg = calculateWeeklyAverageHours(logs)

            _uiState.update {
                it.copy(
                    sleepSchedule = schedule,
                    reminderEnabled = reminderEnabled,
                    sleepLogs = logs,
                    weeklyAverageHours = weeklyAvg,
                    showSleepScheduleDialog = false,
                    showLogSleepDialog = false,
                    showWindDownDialog = false,
                    isBreathing = false
                )
            }

            // If there is a pending bedtime popup request (from alarm/notification), show it once.
            val pendingWindDown = MindCarePrefs.consumePendingWindDown(context, userEmail)
            if (pendingWindDown) {
                _uiState.update { it.copy(showWindDownDialog = true) }
            }

            // Keep alarm in sync (if user enabled reminders)
            if (reminderEnabled) {
                scheduleBedtimeReminder(context, userEmail, schedule.bedtime)
            }
        }
    }

    fun onDismissScheduleDialog() {
        _uiState.update { it.copy(showSleepScheduleDialog = false) }
    }

    fun onSaveSchedule(bedtime: LocalTime, wakeTime: LocalTime, context: Context, userEmail: String) {
        val newSchedule = SleepSchedule(bedtime, wakeTime)

        _uiState.update {
            it.copy(
                sleepSchedule = newSchedule,
                showSleepScheduleDialog = false
            )
        }

        MindCarePrefs.saveSchedule(context, userEmail, newSchedule)

        // Keep alarm aligned if reminders enabled
        if (uiState.value.reminderEnabled) {
            scheduleBedtimeReminder(context, userEmail, bedtime)
        }
    }

    fun onEditScheduleClicked() {
        _uiState.update { it.copy(showSleepScheduleDialog = true) }
    }

    fun onLogTodaySleepClicked() {
        _uiState.update { it.copy(showLogSleepDialog = true) }
    }

    fun onDismissLogSleepDialog() {
        _uiState.update { it.copy(showLogSleepDialog = false) }
    }

    fun onLogSleep(bedtime: LocalTime, wakeTime: LocalTime, quality: SleepQuality) {
        val duration = if (wakeTime.isBefore(bedtime)) {
            Duration.between(bedtime, wakeTime).plusHours(24)
        } else {
            Duration.between(bedtime, wakeTime)
        }

        val hours = duration.toHours()
        val minutes = duration.toMinutes() % 60
        val durationString = "${hours}h ${minutes}m"

        val newLog = SleepLog(
            date = LocalDate.now(),
            bedtime = bedtime,
            wakeTime = wakeTime,
            duration = durationString,
            durationMinutes = duration.toMinutes().toInt(),
            quality = quality
        )

        _uiState.update { current ->
            val withoutToday = current.sleepLogs.filter { !it.date.isEqual(LocalDate.now()) }
            val updated = (listOf(newLog) + withoutToday).sortedByDescending { it.date }.take(7)
            current.copy(
                sleepLogs = updated,
                weeklyAverageHours = calculateWeeklyAverageHours(updated),
                showLogSleepDialog = false
            )
        }
    }

    fun persistAfterLog(context: Context, userEmail: String) {
        // Call this after onLogSleep from UI layer
        val logs = uiState.value.sleepLogs
        MindCarePrefs.saveLogs(context, userEmail, logs)
        _uiState.update { it.copy(weeklyAverageHours = calculateWeeklyAverageHours(logs)) }
    }

    fun onReminderToggled(enabled: Boolean, context: Context, userEmail: String) {
        _uiState.update { it.copy(reminderEnabled = enabled) }
        MindCarePrefs.saveReminderEnabled(context, userEmail, enabled)

        if (enabled) {
            scheduleBedtimeReminder(context, userEmail, uiState.value.sleepSchedule.bedtime)
        } else {
            cancelBedtimeReminder(context, userEmail)
        }
    }

    fun onStartBreathing() {
        _uiState.update { it.copy(isBreathing = true) }
    }

    fun onStopBreathing() {
        _uiState.update { it.copy(isBreathing = false) }
    }

    fun onShowWindDownDialog() {
        _uiState.update { it.copy(showWindDownDialog = true) }
    }

    fun onDismissWindDownDialog() {
        _uiState.update { it.copy(showWindDownDialog = false) }
    }

    fun onSnooze10Min(context: Context, userEmail: String) {
        // Prevent immediate re-trigger
        MindCarePrefs.saveSnoozeUntil(context, userEmail, System.currentTimeMillis() + 10 * 60 * 1000L)
        scheduleSnoozeReminder(context, userEmail, 10)
    }

    private fun calculateWeeklyAverageHours(logs: List<SleepLog>): Float {
        if (logs.isEmpty()) return 0f
        val last7 = logs.sortedByDescending { it.date }.take(7)
        val totalMinutes = last7.sumOf { it.durationMinutes }
        val avgMinutes = totalMinutes.toFloat() / last7.size.toFloat()
        val avgHours = avgMinutes / 60f
        return (kotlin.math.round(avgHours * 10f) / 10f)
    }
}
