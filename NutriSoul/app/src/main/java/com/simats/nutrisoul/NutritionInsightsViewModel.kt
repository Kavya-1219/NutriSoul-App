package com.simats.nutrisoul

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.nutrisoul.data.FoodLogEntity
import com.simats.nutrisoul.data.FoodLogRepository
import com.simats.nutrisoul.data.SessionManager
import com.simats.nutrisoul.data.network.ChatRequest
import com.simats.nutrisoul.data.network.NutriSoulApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.roundToInt

@HiltViewModel
class NutritionInsightsViewModel @Inject constructor(
    private val repo: FoodLogRepository,
    private val sessionManager: SessionManager,
    private val apiService: NutriSoulApiService
) : ViewModel() {

    private val zoneId = ZoneId.systemDefault()

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<NutritionInsightsUiState> =
        sessionManager.currentUserEmailFlow()
            .flatMapLatest { email ->
                if (email.isNullOrBlank()) {
                    flowOf<NutritionInsightsUiState>(NutritionInsightsUiState.Empty)
                } else {
                    flow {
                        emit(NutritionInsightsUiState.Loading)
                        try {
                            val response = apiService.getNutritionInsights()
                            if (response.hasData) {
                                emit(NutritionInsightsUiState.Success(
                                    NutritionInsightsData(
                                        weeklyConsistency = response.weeklyConsistency,
                                        consistencyPercent = response.consistencyPercent,
                                        daysLogged = response.daysLogged,
                                        totalDays = response.totalDays,
                                        averageCalories = response.averageCalories,
                                        targetCalories = response.targetCalories,
                                        averageProtein = response.averageProtein.toFloat(),
                                        averageCarbs = response.averageCarbs.toFloat(),
                                        averageFats = response.averageFats.toFloat(),
                                        proteinPercentage = response.proteinPercentage,
                                        carbsPercentage = response.carbsPercentage,
                                        fatsPercentage = response.fatsPercentage,
                                        calorieStatus = mapToDomain(response.calorieStatus)
                                    )
                                ))
                            } else {
                                emit(NutritionInsightsUiState.Empty)
                            }
                        } catch (e: Exception) {
                            Log.e("INSIGHTS", "Error fetching insights", e)
                            emit(NutritionInsightsUiState.Empty) // Or an Error state if you prefer
                        }
                    }
                }
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                NutritionInsightsUiState.Loading
            )

    private fun mapToDomain(dto: com.simats.nutrisoul.data.network.CalorieStatusDto): CalorieStatus {
        return CalorieStatus(
            label = dto.label,
            tone = try { StatusTone.valueOf(dto.tone.uppercase()) } catch(e: Exception) { StatusTone.NEUTRAL },
            emoji = dto.emoji
        )
    }

    // AI Chat Integration remains...
    private val _chatResponse = MutableStateFlow<String?>(null)
    val chatResponse: StateFlow<String?> = _chatResponse

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading

    fun sendChatMessage(message: String) {
        viewModelScope.launch {
            _isChatLoading.value = true
            _chatResponse.value = null
            try {
                val resp = apiService.getAiAssistantResponse(ChatRequest(message))
                _chatResponse.value = resp.response
            } catch (e: Exception) {
                Log.e("HelpChat", "AI chat error", e)
                _chatResponse.value = "Sorry, I'm having trouble connecting to the AI right now. Please try again shortly."
            } finally {
                _isChatLoading.value = false
            }
        }
    }

    fun clearChatResponse() {
        _chatResponse.value = null
    }
}

// ---------------- UI models ----------------

sealed class NutritionInsightsUiState {
    data object Loading : NutritionInsightsUiState()
    data object Empty : NutritionInsightsUiState()
    data class Success(val data: NutritionInsightsData) : NutritionInsightsUiState()
}

data class NutritionInsightsData(
    val weeklyConsistency: Float,
    val consistencyPercent: Int,
    val daysLogged: Int,
    val totalDays: Int,
    val averageCalories: Int,
    val targetCalories: Int,
    val averageProtein: Float,
    val averageCarbs: Float,
    val averageFats: Float,
    val proteinPercentage: Int,
    val carbsPercentage: Int,
    val fatsPercentage: Int,
    val calorieStatus: CalorieStatus
)

data class CalorieStatus(
    val label: String,
    val tone: StatusTone,
    val emoji: String
)

enum class StatusTone { GOOD, OK, WARN, INFO, NEUTRAL }
