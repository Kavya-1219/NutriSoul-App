package com.simats.nutrisoul.data.meal.model

data class MealPlan(
    val targetCalories: Int,
    val meals: List<Meal>
)

fun MealPlan.totalCalories(): Int = meals.sumOf { it.calories }
fun MealPlan.totalProtein(): Int = meals.sumOf { it.protein }
fun MealPlan.totalCarbs(): Int = meals.sumOf { it.carbs }
fun MealPlan.totalFats(): Int = meals.sumOf { it.fats }
