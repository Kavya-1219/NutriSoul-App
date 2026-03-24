package com.simats.nutrisoul.data.network

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val email: String,
    val password: String
)

data class ProfileResponseDto(
    val email: String = "",
    val name: String = "",
    val age: Int = 0,
    val gender: String = "",
    val height: Float = 0f,
    val weight: Float = 0f,
    val bmi: Float = 0f,
    val goals: List<String> = emptyList(), // Multi-select support
    @SerializedName("activityLevel") val activityLevel: String = "",
    @SerializedName("targetWeight") val targetWeight: Float = 0f,
    @SerializedName("currentWeight") val currentWeight: Float = 0f,
    @SerializedName("targetWeeks") val targetWeeks: Int = 0,
    @SerializedName("mealsPerDay") val mealsPerDay: Int = 0,
    @SerializedName("healthConditions") val healthConditions: List<String> = emptyList(),
    @SerializedName("foodAllergies") val foodAllergies: List<String> = emptyList(),
    @SerializedName("dietaryRestrictions") val dietaryRestrictions: List<String> = emptyList(),
    @SerializedName("targetCalories") val targetCalories: Int = 0,
    @SerializedName("darkMode") val darkMode: Boolean = false,
    @SerializedName("profilePictureUrl") val profilePictureUrl: String? = null,
    @SerializedName("todaysWaterIntake") val todaysWaterIntake: Int = 0,
    @SerializedName("todaysSteps") val todaysSteps: Int = 0,
    @SerializedName("todaysCalories") val todaysCalories: Int = 0,
    val systolic: Int = 0,
    val diastolic: Int = 0,
    @SerializedName("thyroidCondition") val thyroidCondition: String = "",
    @SerializedName("diabetesType") val diabetesType: String = "",
    @SerializedName("cholesterolLevel") val cholesterolLevel: String = "",
    @SerializedName("otherAllergies") val otherAllergies: String = "",
    val dislikes: List<String> = emptyList(),
    val allergies: List<String> = emptyList(),
    @SerializedName("diet_type") val dietType: String? = null,
    @SerializedName("food_dislikes") val foodDislikes: String? = null,
    @SerializedName("calorieGoal") val calorieGoal: Int = 0,
    val bmr: Int = 0,
    @SerializedName("dailyTip") val dailyTip: String = "",
    @SerializedName("recentHistory") val recentHistory: List<String> = emptyList(),
    @SerializedName("aiTips") val aiTips: List<String> = emptyList()
)

data class LoginResponse(
    val token: String,
    val user_id: Int,
    val email: String,
    val username: String,
    val message: String
)

data class RegisterRequest(
    val email: String,
    val password: String,
    val confirm_password: String,
    val username: String? = null
)

data class RegisterResponse(
    val message: String,
    val token: String,
    val user_id: Int,
    val user: RegisteredUserDto
)

data class RegisteredUserDto(
    val username: String,
    val email: String
)

data class ForgotPasswordRequest(
    val email: String
)

data class ForgotPasswordResponse(
    val message: String
)

data class VerifyOtpRequest(
    val email: String,
    val otp: String
)

data class VerifyOtpResponse(
    val message: String
)

data class ResetPasswordRequest(
    val email: String,
    val otp: String,
    val password: String,
    val confirm_password: String
)

data class ResetPasswordResponse(
    val message: String
)

data class ErrorResponse(
    val error: String? = null,
    val message: String? = null,
    val detail: String? = null,
    val email: List<String>? = null,
    val username: List<String>? = null,
    val password: List<String>? = null,
    val confirm_password: List<String>? = null
)
