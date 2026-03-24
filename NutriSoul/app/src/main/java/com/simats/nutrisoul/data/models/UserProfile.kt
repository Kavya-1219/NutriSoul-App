package com.simats.nutrisoul.data.models

data class UserProfile(
    val age: Int = 30,
    val gender: String = "Male", // "Male" or "Female"
    val height: Double = 180.0, // in cm
    val weight: Double = 75.0, // in kg
    val activityLevel: String = "Slightly Active", // e.g., Sedentary, Slightly Active, etc.
    val goal: String = "Maintain" // e.g., Lose, Maintain, Gain
)
