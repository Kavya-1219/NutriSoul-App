package com.simats.nutrisoul.util

import com.simats.nutrisoul.data.models.UserProfile

object CalorieCalculator {
    // Harris-Benedict Equation for BMR
    private fun calculateBMR(profile: UserProfile): Double {
        return if (profile.gender.equals("Male", ignoreCase = true)) {
            88.362 + (13.397 * profile.weight) + (4.799 * profile.height) - (5.677 * profile.age)
        } else {
            447.593 + (9.247 * profile.weight) + (3.098 * profile.height) - (4.330 * profile.age)
        }
    }

    // TDEE calculation
    fun calculateTDEE(profile: UserProfile): Int {
        val bmr = calculateBMR(profile)
        val activityMultiplier = when (profile.activityLevel) {
            "Slightly Active" -> 1.375
            "Moderately Active" -> 1.55
            "Very Active" -> 1.725
            else -> 1.2 // Sedentary
        }
        val maintenanceCalories = bmr * activityMultiplier

        return when (profile.goal) {
            "Lose" -> (maintenanceCalories - 500).toInt()
            "Gain" -> (maintenanceCalories + 500).toInt()
            else -> maintenanceCalories.toInt() // Maintain
        }
    }

    // Simple macro split: 40% carbs, 30% protein, 30% fat
    fun calculateMacros(totalCalories: Int): Triple<Float, Float, Float> {
        val protein = (totalCalories * 0.30f) / 4f
        val carbs = (totalCalories * 0.40f) / 4f
        val fats = (totalCalories * 0.30f) / 9f
        return Triple(protein, carbs, fats)
    }
}
