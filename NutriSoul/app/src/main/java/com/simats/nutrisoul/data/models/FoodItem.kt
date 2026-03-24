package com.simats.nutrisoul.data.models

import com.simats.nutrisoul.data.CustomFoodEntity

data class FoodItem(
    val id: Long = 0,
    val name: String,
    val caloriesPer100g: Double,
    val proteinPer100g: Double,
    val carbsPer100g: Double,
    val fatsPer100g: Double,
    val servingQuantity: Double = 100.0,
    val servingUnit: String = "g"
)

fun FoodItem.toCustomFoodEntity() = CustomFoodEntity(
    name = name,
    calories = caloriesPer100g,
    protein = proteinPer100g,
    carbs = carbsPer100g,
    fats = fatsPer100g
)

fun CustomFoodEntity.toFoodItem() = FoodItem(
    name = name,
    caloriesPer100g = calories,
    proteinPer100g = protein,
    carbsPer100g = carbs,
    fatsPer100g = fats
)

fun com.simats.nutrisoul.data.network.BackendFoodItem.toFoodItem() = FoodItem(
    name = name,
    caloriesPer100g = calories,
    proteinPer100g = protein,
    carbsPer100g = carbs,
    fatsPer100g = fats,
    servingQuantity = servingQuantity,
    servingUnit = servingUnit
)
