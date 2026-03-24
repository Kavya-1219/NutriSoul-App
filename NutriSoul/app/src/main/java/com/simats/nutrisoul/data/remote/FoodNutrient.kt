package com.simats.nutrisoul.data.remote

import com.google.gson.annotations.SerializedName

data class FoodNutrient(
    @SerializedName("nutrientName") val nutrientName: String,
    @SerializedName("unitName") val unitName: String,
    @SerializedName("value") val value: Double
)
