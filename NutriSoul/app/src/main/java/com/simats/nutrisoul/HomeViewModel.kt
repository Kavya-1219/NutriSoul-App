package com.simats.nutrisoul

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.nutrisoul.data.FoodRepository
import com.simats.nutrisoul.data.SessionManager
import com.simats.nutrisoul.data.models.DailyTotals
import com.simats.nutrisoul.data.network.ProfileResponseDto
import com.simats.nutrisoul.ui.DailyTotalsUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: FoodRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val todayTotals: StateFlow<DailyTotalsUi> =
        sessionManager.currentUserEmailFlow()
            .flatMapLatest { email ->
                if (email.isNullOrBlank()) {
                    flowOf(DailyTotals(0.0, 0.0, 0.0, 0.0))
                } else {
                    repository.observeTodayTotals(email)
                }
            }
            .map { totals ->
                DailyTotalsUi(
                    calories = totals.calories ?: 0.0,
                    protein = totals.protein ?: 0.0,
                    carbs = totals.carbs ?: 0.0,
                    fats = totals.fats ?: 0.0
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = DailyTotalsUi()
            )

    init {
        refreshHomeData()
        refreshTodayMacros()
    }

    fun refreshHomeData() {
        viewModelScope.launch {
            repository.getHomeData().collect { result ->
                result?.let { response ->
                    _uiState.update { it.copy(
                        dailyTip = response.dailyTip,
                        recentHistory = response.recentHistory,
                        aiTips = response.aiTips
                    ) }
                }
            }
        }
    }

    fun refreshTodayMacros() {
        viewModelScope.launch {
            val email = sessionManager.currentUserEmailFlow().firstOrNull() ?: return@launch
            repository.getTodayMacros(email).collect { totals ->
                // Totals are now synced inside repository.getTodayMacros
            }
        }
    }
}

data class HomeUiState(
    val dailyTip: String = "",
    val recentHistory: List<String> = emptyList(),
    val aiTips: List<String> = emptyList()
)
