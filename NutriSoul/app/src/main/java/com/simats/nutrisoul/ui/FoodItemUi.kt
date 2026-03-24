package com.simats.nutrisoul.ui

data class FoodItemUi(
    val id: Long,
    val name: String,
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fats: Double,
    val fiber: Double = 0.0,
    val sugar: Double = 0.0,
    val saturatedFat: Double = 0.0,
    val vitaminA: Double = 0.0,
    val vitaminC: Double = 0.0,
    val vitaminD: Double = 0.0,
    val vitaminB12: Double = 0.0,
    val calcium: Double = 0.0,
    val iron: Double = 0.0,
    val magnesium: Double = 0.0,
    val potassium: Double = 0.0,
    val sodium: Double = 0.0,
    val zinc: Double = 0.0,
    val healthAlternative: String = "",
    val proTip: String = "",
    val servingQuantity: Double,
    val servingUnit: String,
    val confidence: Double = 1.0,
    val source: String = ""
)
