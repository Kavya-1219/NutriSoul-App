package com.simats.nutrisoul.data

import com.simats.nutrisoul.data.CustomFoodEntity
import com.simats.nutrisoul.data.IntakeEntity
import com.simats.nutrisoul.data.models.FoodItem
import com.simats.nutrisoul.data.models.FoodLog
import com.simats.nutrisoul.data.network.FoodSearchResponse
import com.google.firebase.Timestamp
import com.simats.nutrisoul.data.network.ProfileResponseDto
import com.simats.nutrisoul.data.User

fun FoodSearchResponse.toFoodItems(): List<FoodItem> {
    return foods.map { apiFoodItem ->
        val nutrients = apiFoodItem.foodNutrients.associate { it.nutrientName to it.value }
        FoodItem(
            name = apiFoodItem.description,
            caloriesPer100g = nutrients["Energy"] ?: 0.0,
            proteinPer100g = nutrients["Protein"] ?: 0.0,
            carbsPer100g = nutrients["Carbohydrate, by difference"] ?: 0.0,
            fatsPer100g = nutrients["Total lipid (fat)"] ?: 0.0,
            servingQuantity = 100.0,
            servingUnit = "g"
        )
    }
}

fun FoodLog.toEntity(): IntakeEntity {
    return IntakeEntity(
        id = id,
        name = name,
        calories = calories,
        protein = protein,
        carbs = carbs,
        fats = fats,
        mealType = mealType,
        date = date.toString(),
        quantity = quantity
    )
}

fun ProfileResponseDto.toUser(): User {
    return User(
        email = (email ?: "").lowercase(),
        name = name ?: "",
        age = age,
        gender = gender ?: "",
        height = height,
        weight = weight,
        activityLevel = activityLevel ?: "",
        goals = goals ?: emptyList(),
        targetWeight = targetWeight,
        currentWeight = currentWeight,
        targetWeeks = targetWeeks,
        mealsPerDay = mealsPerDay,
        healthConditions = healthConditions ?: emptyList(),
        todaysCalories = todaysCalories,
        todaysWaterIntake = todaysWaterIntake,
        todaysSteps = todaysSteps,
        bmr = bmr,
        lastLogin = Timestamp.now(),
        allergies = allergies ?: emptyList(),
        foodAllergies = foodAllergies ?: emptyList(),
        cholesterolLevel = cholesterolLevel ?: "",
        password = "", // Do not store password
        dietaryRestrictions = dietaryRestrictions ?: emptyList(),
        otherAllergies = otherAllergies ?: "",
        dislikes = dislikes ?: emptyList(),
        diabetesType = diabetesType ?: "",
        diastolic = diastolic,
        systolic = systolic,
        thyroidCondition = thyroidCondition ?: "",
        targetCalories = targetCalories,
        dietType = dietType ?: "",
        foodDislikes = foodDislikes ?: "",
        darkMode = darkMode,
        profilePictureUrl = profilePictureUrl
    )
}

fun FoodItem.toEntity(): CustomFoodEntity {
    return CustomFoodEntity(
        name = name,
        calories = caloriesPer100g,
        protein = proteinPer100g,
        carbs = carbsPer100g,
        fats = fatsPer100g
    )
}
