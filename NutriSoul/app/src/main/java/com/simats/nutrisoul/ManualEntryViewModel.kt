package com.simats.nutrisoul

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.nutrisoul.data.FoodRepository
import com.simats.nutrisoul.data.FoodLogEntity
import com.simats.nutrisoul.data.FoodLogRepository
import com.simats.nutrisoul.data.SessionManager
import com.simats.nutrisoul.data.UserProfileRepository
import com.simats.nutrisoul.data.models.FoodLog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import android.util.Log
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class ManualEntryViewModel @Inject constructor(
    private val repository: com.simats.nutrisoul.data.FoodRepository,
    private val foodLogRepository: com.simats.nutrisoul.data.FoodLogRepository,
    private val userProfileRepository: com.simats.nutrisoul.data.UserProfileRepository,
    private val sessionManager: com.simats.nutrisoul.data.SessionManager
) : ViewModel() {
    val foodName = MutableStateFlow("")
    val quantity = MutableStateFlow("")
    val unit = MutableStateFlow("g")
    val caloriesPer100g = MutableStateFlow("")
    val proteinPer100g = MutableStateFlow("")
    val carbsPer100g = MutableStateFlow("")
    val fatsPer100g = MutableStateFlow("")

    fun calculateAndLogFood() {
        viewModelScope.launch {
            val email = sessionManager.currentUserEmailFlow().first() ?: return@launch
            val qty = quantity.value.toDoubleOrNull() ?: 0.0
            val factor = qty / 100.0
            
            val totalCalories = (caloriesPer100g.value.toDoubleOrNull() ?: 0.0) * factor
            val totalProtein = (proteinPer100g.value.toDoubleOrNull() ?: 0.0) * factor
            val totalCarbs = (carbsPer100g.value.toDoubleOrNull() ?: 0.0) * factor
            val totalFats = (fatsPer100g.value.toDoubleOrNull() ?: 0.0) * factor

            // 1. Log to history and daily intake (consolidated in repository)
            val foodLog = FoodLog(
                name = foodName.value,
                calories = totalCalories,
                protein = totalProtein,
                carbs = totalCarbs,
                fats = totalFats,
                quantity = qty,
                unit = unit.value,
                mealType = "Manual Entry",
                date = LocalDate.now()
            )
            try {
                repository.addFoodToDailyIntake(email, foodLog, "Manual Entry", factor)
                userProfileRepository.refreshProfile()
            } catch (e: Exception) {
                Log.e("CRASH_FIX", "Manual food failed", e)
            }
        }
    }
}
