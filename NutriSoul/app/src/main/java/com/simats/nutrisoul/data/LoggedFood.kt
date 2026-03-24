package com.simats.nutrisoul.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "logged_foods")
data class LoggedFood(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val quantity: Double,
    val unit: String,
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val timestamp: Date,
    val mealType: String // "Breakfast", "Lunch", "Dinner", "Snack"
)
