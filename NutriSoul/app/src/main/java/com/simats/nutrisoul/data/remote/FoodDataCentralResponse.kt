package com.simats.nutrisoul.data.remote

import com.google.gson.annotations.SerializedName

data class FoodDataCentralResponse(
    @SerializedName("foods") val foods: List<Food>
)
