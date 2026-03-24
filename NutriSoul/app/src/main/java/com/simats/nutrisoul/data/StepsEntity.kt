package com.simats.nutrisoul.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "daily_steps")
data class StepsEntity(
    @PrimaryKey val date: LocalDate,
    val steps: Long = 0L,
    val manualSteps: Long = 0L,
    val goal: Int = 10000,
    val lastSyncedAt: Long = System.currentTimeMillis()
)
