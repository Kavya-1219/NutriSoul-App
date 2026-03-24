package com.simats.nutrisoul

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf

object UserData {
    val name = mutableStateOf("k") // User's name
    val steps = mutableIntStateOf(0)
    val waterIntake = mutableIntStateOf(0)
    val automaticTracking = mutableStateOf(false)

    val calories = mutableIntStateOf(41)
    val protein = mutableIntStateOf(1)
    val carbs = mutableIntStateOf(10)
    val fats = mutableIntStateOf(0)
    const val dailyCalorieGoal = 1633

    var currentWeight = mutableIntStateOf(65)
    var targetWeight = mutableIntStateOf(60)
    var goal = mutableStateOf("Weight Loss") // Possible values: "Weight Loss", "Weight Gain", "Maintain Weight", "Gain Muscle"
}
