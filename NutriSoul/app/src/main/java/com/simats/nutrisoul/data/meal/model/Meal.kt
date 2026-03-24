package com.simats.nutrisoul.data.meal.model

data class Meal(
    val id: Int = 0,
    val mealType: String, // breakfast/lunch/snack/dinner
    val title: String,
    val calories: Int,
    val protein: Int,
    val carbs: Int,
    val fats: Int,
    val items: List<MealItem>,
    val isEaten: Boolean = false
)
