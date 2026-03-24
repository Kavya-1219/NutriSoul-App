package com.simats.nutrisoul

import androidx.compose.ui.graphics.Color
import java.time.LocalDate
import java.time.LocalTime

data class SleepSchedule(
    val bedtime: LocalTime = LocalTime.of(22, 0),
    val wakeTime: LocalTime = LocalTime.of(6, 0)
)

data class SleepLog(
    val date: LocalDate,
    val bedtime: LocalTime,
    val wakeTime: LocalTime,
    val duration: String,
    val durationMinutes: Int,
    val quality: SleepQuality
)

enum class SleepQuality(val label: String, val emoji: String, val color: Color, val bgColor: Color, val message: String) {
    Good("Good", "😊", Color(0xFF2E7D32), Color(0xFFE8F5E9), "Great sleep! This helps control hunger hormones today."),
    Fair("Fair", "😐", Color(0xFFF57C00), Color(0xFFFFF3E0), "Decent rest. Try sleeping a bit earlier tonight for better digestion."),
    Poor("Poor", "😴", Color(0xFFD32F2F), Color(0xFFFFEBEE), "Not enough sleep. This can increase cravings tomorrow."),
    Over("Over", "😴", Color(0xFFC62828), Color(0xFFFFEBEE), "Too much sleep can affect energy levels. Try to stick to your schedule.")
}

data class StressAndSleepUiState(
    val sleepSchedule: SleepSchedule = SleepSchedule(),
    val sleepLogs: List<SleepLog> = emptyList(),
    val reminderEnabled: Boolean = false,
    val weeklyAverageHours: Float = 0f,
    val showSleepScheduleDialog: Boolean = false,
    val isBreathing: Boolean = false,
    val showWindDownDialog: Boolean = false,
    val showLogSleepDialog: Boolean = false
)

data class MindCareUiState(
    val sleepSchedule: SleepSchedule = SleepSchedule(),
    val sleepLogs: List<SleepLog> = emptyList(),
    val reminderEnabled: Boolean = false,
    val weeklyAverageHours: Float = 0f,
    val showSleepScheduleDialog: Boolean = false,
    val isBreathing: Boolean = false,
    val showWindDownDialog: Boolean = false,
    val showLogSleepDialog: Boolean = false
)
