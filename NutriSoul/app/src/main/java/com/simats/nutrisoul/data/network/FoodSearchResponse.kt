package com.simats.nutrisoul.data.network

import com.google.gson.annotations.SerializedName

data class FoodSearchResponse(
    @SerializedName("foods") val foods: List<ApiFoodItem>
)

data class ApiFoodItem(
    @SerializedName("fdcId") val fdcId: Long,
    @SerializedName("description") val description: String,
    @SerializedName("servingSize") val servingSize: Double?,
    @SerializedName("servingSizeUnit") val servingSizeUnit: String?,
    @SerializedName("foodNutrients") val foodNutrients: List<ApiFoodNutrient>
)

data class ApiFoodNutrient(
    @SerializedName("nutrientName") val nutrientName: String,
    @SerializedName("unitName") val unitName: String,
    @SerializedName("value") val value: Double
)
