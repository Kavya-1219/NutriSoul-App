package com.simats.nutrisoul.data.models

import com.simats.nutrisoul.data.network.ApiFoodItem

fun ApiFoodItem.toFoodItem(): FoodItem {
    return FoodItem(
        name = description,
        caloriesPer100g = foodNutrients.find { it.nutrientName == "Energy" }?.value?.toDouble() ?: 0.0,
        proteinPer100g = foodNutrients.find { it.nutrientName == "Protein" }?.value?.toDouble() ?: 0.0,
        carbsPer100g = foodNutrients.find { it.nutrientName == "Carbohydrate, by difference" }?.value?.toDouble() ?: 0.0,
        fatsPer100g = foodNutrients.find { it.nutrientName == "Total lipid (fat)" }?.value?.toDouble() ?: 0.0
    )
}
