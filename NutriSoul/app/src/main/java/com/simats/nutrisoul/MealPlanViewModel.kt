package com.simats.nutrisoul

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.nutrisoul.data.meal.model.Meal as ModelMeal
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.simats.nutrisoul.data.MealRepository
import com.simats.nutrisoul.data.UserProfileRepository
import com.simats.nutrisoul.data.FoodRepository
import com.simats.nutrisoul.data.toNutritionProfile
import com.simats.nutrisoul.data.SessionManager
import com.simats.nutrisoul.data.FoodLogRepository
import com.simats.nutrisoul.data.FoodLogEntity
import com.simats.nutrisoul.data.IntakeDao
import com.simats.nutrisoul.data.IntakeEntity
import com.simats.nutrisoul.data.DailyTotalsUi
import com.simats.nutrisoul.data.models.DailyTotals
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class MealPlanViewModel @Inject constructor(
    private val mealRepository: MealRepository,
    private val userProfileRepository: UserProfileRepository,
    private val foodRepository: FoodRepository,
    private val foodLogRepository: FoodLogRepository,
    private val sessionManager: SessionManager,
    private val intakeDao: IntakeDao
) : ViewModel() {

    private val _uiState = MutableStateFlow<MealPlanUiState>(MealPlanUiState.Loading)
    val uiState: StateFlow<MealPlanUiState> = _uiState

    private val _tips = MutableStateFlow<List<com.simats.nutrisoul.data.network.AiTipDto>>(emptyList())
    val tips: StateFlow<List<com.simats.nutrisoul.data.network.AiTipDto>> = _tips

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val userProfile = sessionManager.currentUserEmailFlow()
        .flatMapLatest { email ->
            if (email.isNullOrBlank()) flowOf(null)
            else userProfileRepository.getUserFlow(email)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val todayTotals: StateFlow<DailyTotalsUi> =
        sessionManager.currentUserEmailFlow()
            .flatMapLatest { email ->
                if (email.isNullOrBlank()) flowOf(DailyTotals())
                else foodRepository.observeTodayTotals(email)
            }
            .map { totals ->
                DailyTotalsUi(
                    calories = totals.calories ?: 0.0,
                    protein = totals.protein ?: 0.0,
                    carbs = totals.carbs ?: 0.0,
                    fats = totals.fats ?: 0.0
                )
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DailyTotalsUi())

    init {
        viewModelScope.launch {
            // Wait for the first actual non-null profile before initial load
            userProfile.filterNotNull().first()
            refreshAll()
        }
    }

    private fun refreshAll() {
        loadToday()
        loadTips()
    }

    fun loadToday() = viewModelScope.launch {
        _uiState.value = MealPlanUiState.Loading
        try {
            val today = LocalDate.now().toString()
            val profile = userProfile.value?.toNutritionProfile()
            val plan = mealRepository.getTodayMealPlan(today, profile)
            _uiState.value = MealPlanUiState.Ready(plan)
        } catch (e: Exception) {
            _uiState.value = MealPlanUiState.Error(e.message ?: "Failed to load meal plan")
        }
    }

    private fun loadTips() = viewModelScope.launch {
        try {
            val profile = userProfile.value?.toNutritionProfile()
            _tips.value = mealRepository.getAiTips(profile)
        } catch (e: Exception) {}
    }

    fun refresh() = viewModelScope.launch {
        _uiState.value = MealPlanUiState.Loading
        try {
            val profile = userProfile.value?.toNutritionProfile()
            val plan = mealRepository.refreshMealPlan(profile)
            _uiState.value = MealPlanUiState.Ready(plan)
            loadTips()
        } catch (e: Exception) {
            _uiState.value = MealPlanUiState.Error(e.message ?: "Refresh failed")
        }
    }

    private val _selectedMealType = MutableStateFlow<String?>(null)
    
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val alternativeMeals: StateFlow<List<ModelMeal>> = _selectedMealType
        .flatMapLatest { type ->
            if (type == null) flowOf(emptyList())
            else {
                try {
                    val list = mealRepository.getMealAlternatives(type)
                    flowOf(list)
                } catch (e: Exception) {
                    flowOf(emptyList())
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun selectMealTypeForAlternatives(mealType: String?) {
        _selectedMealType.value = mealType
    }

    fun swapMeal(mealType: String, selected: ModelMeal) = viewModelScope.launch {
        _uiState.value = MealPlanUiState.Loading
        try {
            val plan = mealRepository.swapMeal(mealType, selected.id)
            _uiState.value = MealPlanUiState.Ready(plan)
            loadTips()
            _selectedMealType.value = null
        } catch (e: Exception) {
            _uiState.value = MealPlanUiState.Error(e.message ?: "Swap failed")
        }
    }

    fun markAsEaten(meal: ModelMeal) {
        viewModelScope.launch {
            try {
                val today = LocalDate.now().toString()
                val isMarkingEaten = !meal.isEaten
                
                // 1) Sync with Backend (Updates backend FoodLog and Profile)
                val response = mealRepository.markMealEaten(meal.mealType, isMarkingEaten, today)
                
                // 2) Update Local Data Sources
                val email = sessionManager.currentUserEmailFlow().firstOrNull()
                if (!email.isNullOrBlank()) {
                    val safeEmail = email.trim().lowercase()
                    
                    if (isMarkingEaten) {
                        // ADD to History Table (FoodLogEntity)
                        foodLogRepository.addLog(
                            FoodLogEntity(
                                userEmail = safeEmail,
                                name = meal.title,
                                caloriesPerUnit = meal.calories.toFloat(),
                                proteinPerUnit = meal.protein.toFloat(),
                                carbsPerUnit = meal.carbs.toFloat(),
                                fatsPerUnit = meal.fats.toFloat(),
                                quantity = 1.0f,
                                unit = "serving",
                                timestampMillis = System.currentTimeMillis()
                            )
                        )

                        // ADD to Today's Totals Table (IntakeEntity)
                        intakeDao.insert(
                            IntakeEntity(
                                userEmail = safeEmail,
                                name = meal.title,
                                calories = meal.calories.toDouble(),
                                protein = meal.protein.toDouble(),
                                carbs = meal.carbs.toDouble(),
                                fats = meal.fats.toDouble(),
                                quantity = 1.0,
                                unit = "serving",
                                mealType = meal.mealType,
                                date = today
                            )
                        )
                    } else {
                        // REMOVE from local tables if unmarked
                        intakeDao.deleteForMeal(today, safeEmail, meal.title, meal.mealType)
                        // Note: FoodLogEntity deletion is complex without a specific ID, 
                        // but typically meal plans are additive.
                    }
                }
                
                // 3) Reload today's plan to update the UI "Check" icon
                loadToday()
                
                // 4) Refresh UserProfile to update Home screen progress bars
                userProfileRepository.refreshProfile()
                
            } catch (e: Exception) {}
        }
    }
}
