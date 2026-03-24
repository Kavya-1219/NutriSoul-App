package com.simats.nutrisoul.data.network

import com.google.gson.annotations.SerializedName

data class UpdateProfileRequestDto(
    val name: String? = null,
    val age: Int? = null,
    val gender: String? = null,
    val height: Float? = null,
    val weight: Float? = null,
    val goals: List<String>? = null, // Multi-select support
    @SerializedName("activityLevel") val activityLevel: String? = null,
    @SerializedName("targetWeight") val targetWeight: Float? = null,
    @SerializedName("currentWeight") val currentWeight: Float? = null,
    @SerializedName("targetWeeks") val targetWeeks: Int? = null,
    @SerializedName("mealsPerDay") val mealsPerDay: Int? = null,
    @SerializedName("healthConditions") val healthConditions: List<String>? = null,
    @SerializedName("foodAllergies") val foodAllergies: List<String>? = null,
    @SerializedName("dietaryRestrictions") val dietaryRestrictions: List<String>? = null,
    @SerializedName("targetCalories") val targetCalories: Int? = null,
    @SerializedName("darkMode") val darkMode: Boolean? = null,
    @SerializedName("todaysWaterIntake") val todaysWaterIntake: Int? = null,
    @SerializedName("todaysSteps") val todaysSteps: Int? = null,
    @SerializedName("todaysCalories") val todaysCalories: Int? = null,
    val systolic: Int? = null,
    val diastolic: Int? = null,
    @SerializedName("thyroidCondition") val thyroidCondition: String? = null,
    @SerializedName("diabetesType") val diabetesType: String? = null,
    @SerializedName("cholesterolLevel") val cholesterolLevel: String? = null,
    @SerializedName("otherAllergies") val otherAllergies: String? = null,
    val dislikes: List<String>? = null,
    val allergies: List<String>? = null,
    @SerializedName("diet_type") val dietType: String? = null,
    @SerializedName("food_dislikes") val foodDislikes: String? = null
)

data class ChangePasswordRequestDto(
    @SerializedName("oldPassword") val oldPassword: String,
    @SerializedName("newPassword") val newPassword: String,
    @SerializedName("confirmPassword") val confirmPassword: String
)

data class ProfilePictureUploadResponse(
    val message: String,
    @SerializedName("profilePictureUrl") val profilePictureUrl: String
)
