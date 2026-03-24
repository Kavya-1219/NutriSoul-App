package com.simats.nutrisoul

import com.simats.nutrisoul.data.meal.model.MealPlan

sealed interface MealPlanUiState {
    data object Loading : MealPlanUiState
    data class Ready(val plan: MealPlan) : MealPlanUiState
    data class Error(val message: String) : MealPlanUiState
}
