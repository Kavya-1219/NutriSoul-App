package com.simats.nutrisoul.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "food_logs")
data class FoodLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userEmail: String,
    val name: String,
    val caloriesPerUnit: Float,
    val proteinPerUnit: Float,
    val carbsPerUnit: Float,
    val fatsPerUnit: Float,
    val quantity: Float,
    val unit: String,
    val timestampMillis: Long
)
