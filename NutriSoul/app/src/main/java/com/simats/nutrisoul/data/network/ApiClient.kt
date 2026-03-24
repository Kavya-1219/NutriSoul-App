package com.simats.nutrisoul.data.network

import com.google.gson.GsonBuilder
import com.simats.nutrisoul.data.SessionManager
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

    /**
     * TO FIX CONNECTION ERRORS:
     * Your device (192.168.137.83) is connected to your PC's Hotspot.
     * On this network, your PC's IP is: 172.23.50.3
     * Run backend with: python manage.py runserver 0.0.0.0:8000
     */
    private const val YOUR_PC_IP = "172.23.50.3"
    private const val BASE_URL = "http://$YOUR_PC_IP:8000/api/"
    // private const val BASE_URL = "http://$YOUR_PC_IP:8000/api/"

    fun create(sessionManager: SessionManager): UserApi {
        val client = okhttp3.OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(sessionManager))
            .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
            .build()
            .create(UserApi::class.java)
    }

    fun createNutriSoulApi(sessionManager: SessionManager): NutriSoulApiService {
        val client = okhttp3.OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(sessionManager))
            .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
            .build()
            .create(NutriSoulApiService::class.java)
    }

    val apiService: NutritionApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.nal.usda.gov/fdc/v1/")
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
            .build()
            .create(NutritionApiService::class.java)
    }
}
