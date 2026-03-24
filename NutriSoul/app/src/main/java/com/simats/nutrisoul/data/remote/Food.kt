package com.simats.nutrisoul.data.remote

import com.google.gson.annotations.SerializedName

data class Food(
    @SerializedName("fdcId") val fdcId: Int,
    @SerializedName("description") val description: String,
    @SerializedName("foodNutrients") val foodNutrients: List<FoodNutrient>
)
