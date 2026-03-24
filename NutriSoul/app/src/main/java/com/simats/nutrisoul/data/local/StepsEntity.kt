package com.simats.nutrisoul.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "daily_steps")
data class StepsEntity(
    @PrimaryKey val date: LocalDate,
    val steps: Long,
    val goal: Int,
    val lastSyncedAt: Long = System.currentTimeMillis()
)
