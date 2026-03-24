package com.simats.nutrisoul.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.simats.nutrisoul.data.models.MealType

@Entity(tableName = "food_log")
data class FoodEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fats: Double,
    val servingSize: String,
    val servingUnit: String,
    val timestamp: Long,
    val mealType: MealType
)
