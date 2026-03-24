package com.simats.nutrisoul.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "custom_foods",
    indices = [Index(value = ["name"], unique = true)]
)
data class CustomFoodEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,

    // always per 100g or per 1 serving
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fats: Double
)
