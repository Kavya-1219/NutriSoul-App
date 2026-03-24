package com.simats.nutrisoul.data.network

import com.google.gson.annotations.SerializedName

data class NutritionResponse(
    @SerializedName("parsed") val parsed: List<ParsedFood>?,
    @SerializedName("hints") val hints: List<Hint> = emptyList()
)

data class ParsedFood(
    @SerializedName("food") val food: FoodInfo?
)

data class FoodInfo(
    @SerializedName("label") val label: String?,
    @SerializedName("nutrients") val nutrients: Nutrients?
)

data class Nutrients(
    @SerializedName("ENERC_KCAL") val calories: Float?,
    @SerializedName("PROCNT") val protein: Float?,
    @SerializedName("CHOCDF") val carbs: Float?,
    @SerializedName("FAT") val fat: Float?
)

data class Hint(
    @SerializedName("food") val food: ApiFood?
)

data class ApiFood(
    @SerializedName("foodId") val foodId: String,
    @SerializedName("label") val label: String?,
    @SerializedName("nutrients") val nutrients: Nutrients?
)
