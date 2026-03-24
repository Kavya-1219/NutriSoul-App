package com.simats.nutrisoul.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.simats.nutrisoul.SleepQuality
import java.time.LocalDate
import java.time.LocalTime

@Entity(tableName = "sleep_logs")
data class SleepLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: LocalDate,
    val bedtime: LocalTime,
    val wakeTime: LocalTime,
    val duration: String,
    val durationMinutes: Int,
    val quality: SleepQuality
)

@Entity(tableName = "sleep_schedule")
data class SleepScheduleEntity(
    @PrimaryKey val id: Int = 1, // Single record
    val bedtime: LocalTime,
    val wakeTime: LocalTime,
    val reminderEnabled: Boolean
)
