package com.simats.nutrisoul.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_intake")
data class IntakeEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val userEmail: String = "", // Associated user

    val name: String,

    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fats: Double,

    val quantity: Double,     // grams or servings
    val unit: String = "g",

    val mealType: String,     // breakfast, lunch, dinner, snack

    val date: String,         // e.g. "2026-03-05"

    val timestamp: Long = System.currentTimeMillis()
)
