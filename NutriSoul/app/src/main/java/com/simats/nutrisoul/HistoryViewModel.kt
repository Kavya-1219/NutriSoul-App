package com.simats.nutrisoul

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.nutrisoul.data.FoodLogEntity
import com.simats.nutrisoul.data.FoodLogRepository
import com.simats.nutrisoul.data.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repo: FoodLogRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val zoneId = ZoneId.systemDefault()

    init {
        viewModelScope.launch {
            sessionManager.currentUserEmailFlow().collect { email ->
                if (!email.isNullOrBlank()) {
                    repo.refreshHistory(email)
                }
            }
        }
    }

    val historyData: StateFlow<HistoryUiState> =
        sessionManager.currentUserEmailFlow()
            .flatMapLatest { email ->
                if (email.isNullOrBlank()) flowOf(emptyList())
                else {
                    val (startMillis, endMillis) = last7DaysRangeMillis()
                    repo.observeLast7Days(email, startMillis, endMillis)
                }
            }
            .map { logs -> mapLogsToUi(logs) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = HistoryUiState()
            )

    private fun last7DaysRangeMillis(): Pair<Long, Long> {
        val today = LocalDate.now(zoneId)
        val start = today.minusDays(6).atStartOfDay(zoneId).toInstant().toEpochMilli()
        val end = today.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli() - 1
        return start to end
    }

    private fun mapLogsToUi(logs: List<FoodLogEntity>): HistoryUiState {
        if (logs.isEmpty()) return HistoryUiState()

        val grouped = logs.groupBy { log ->
            Instant.ofEpochMilli(log.timestampMillis).atZone(zoneId).toLocalDate()
        }

        val today = LocalDate.now(zoneId)
        val yesterday = today.minusDays(1)
        val timeFmt = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())

        val dayLogs = grouped.entries
            .map { (date, items) ->

                val totalCalories =
                    items.sumOf { (it.caloriesPerUnit * it.quantity).toDouble() }.roundToInt()
                val totalProtein =
                    items.sumOf { (it.proteinPerUnit * it.quantity).toDouble() }.roundToInt()
                val totalCarbs =
                    items.sumOf { (it.carbsPerUnit * it.quantity).toDouble() }.roundToInt()
                val totalFats =
                    items.sumOf { (it.fatsPerUnit * it.quantity).toDouble() }.roundToInt()

                val label = when (date) {
                    today -> "Today"
                    yesterday -> "Yesterday"
                    else -> date.format(DateTimeFormatter.ofPattern("MMM d", Locale.getDefault()))
                }

                DayLogUi(
                    label = label,
                    sortKey = date.toEpochDay(),
                    totalCalories = totalCalories,
                    totalProtein = totalProtein,
                    totalCarbs = totalCarbs,
                    totalFats = totalFats,
                    foods = items
                        .sortedByDescending { it.timestampMillis }
                        .map { log ->
                            val time = Instant.ofEpochMilli(log.timestampMillis)
                                .atZone(zoneId)
                                .toLocalTime()
                                .format(timeFmt)

                            LoggedFoodUi(
                                name = log.name,
                                quantity = "${(log.quantity * 100).roundToInt()}g",
                                calories = (log.caloriesPerUnit * log.quantity).roundToInt(),
                                time = time
                            )
                        }
                )
            }
            .sortedByDescending { it.sortKey }

        return HistoryUiState(
            daysLogged = dayLogs.size,
            totalMeals = dayLogs.sumOf { it.foods.size },
            dayLogs = dayLogs
        )
    }
}

data class HistoryUiState(
    val daysLogged: Int = 0,
    val totalMeals: Int = 0,
    val dayLogs: List<DayLogUi> = emptyList()
)

data class DayLogUi(
    val label: String,
    val sortKey: Long,
    val totalCalories: Int,
    val totalProtein: Int,
    val totalCarbs: Int,
    val totalFats: Int,
    val foods: List<LoggedFoodUi>
)

data class LoggedFoodUi(
    val name: String,
    val quantity: String,
    val calories: Int,
    val time: String
)
