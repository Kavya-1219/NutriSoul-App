package com.simats.nutrisoul.data.network

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface NutritionApiService {

    @GET("foods/search")
    suspend fun searchFoods(
        @Query("query") query: String,
        @Query("api_key") apiKey: String,
        @Query("pageSize") pageSize: Int = 20
    ): Response<FoodSearchResponse>
}
