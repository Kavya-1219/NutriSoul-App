package com.simats.nutrisoul.data.meal.model

data class Ingredient(
    val name: String,
    val quantity: String,
    val calories: Int,
    val protein: Int,
    val carbs: Int,
    val fats: Int
)
