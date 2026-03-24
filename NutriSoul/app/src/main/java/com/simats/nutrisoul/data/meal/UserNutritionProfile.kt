package com.simats.nutrisoul.data.meal

data class UserNutritionProfile(
    val goal: String,                 // lose_weight / gain_weight / maintain / gain_muscle
    val dietType: String,              // vegetarian / nonveg / vegan
    val allergies: List<String>,
    val healthConditions: List<String>,
    val targetCalories: Int
)
