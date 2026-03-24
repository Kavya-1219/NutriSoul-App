package com.simats.nutrisoul

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.simats.nutrisoul.data.FoodLogEntity
import com.simats.nutrisoul.data.FoodLogRepository
import com.simats.nutrisoul.data.FoodRepository
import com.simats.nutrisoul.data.SessionManager
import com.simats.nutrisoul.data.UserProfileRepository
import com.simats.nutrisoul.data.models.DailyTotals
import com.simats.nutrisoul.data.models.FoodItem
import com.simats.nutrisoul.data.models.FoodLog
import com.simats.nutrisoul.ui.DailyTotalsUi
import com.simats.nutrisoul.ui.FoodItemUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.util.Calendar
import javax.inject.Inject

data class LogFoodUiState(
    val isLoading: Boolean = false,
    val imageUri: Uri? = null,
    val extractedText: String = "",
    val detectedFoods: List<String> = emptyList(),
    val nutrition: List<FoodItemUi> = emptyList(),
    val error: String? = null,
    val scanMessage: String? = null
)

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class LogFoodViewModel @Inject constructor(
    private val repository: FoodRepository,
    private val foodLogRepository: FoodLogRepository,
    private val userProfileRepository: UserProfileRepository,
    private val sessionManager: SessionManager,
    private val app: Application
) : AndroidViewModel(app) {

    val todayTotals: StateFlow<DailyTotalsUi> =
        sessionManager.currentUserEmailFlow()
            .flatMapLatest { email ->
                if (email == null) flowOf(DailyTotals())
                else repository.observeTodayTotals(email)
            }
            .map { totals: DailyTotals ->
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

    private val _uiState = MutableStateFlow(LogFoodUiState())
    val uiState: StateFlow<LogFoodUiState> = _uiState.asStateFlow()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    private val _searchResults = MutableStateFlow<List<FoodItemUi>>(emptyList())
    val searchResults: StateFlow<List<FoodItemUi>> = _searchResults

    init {
        viewModelScope.launch {
            _query
                .debounce(300)
                .map { it.trim() }
                .distinctUntilChanged()
                .filter { it.length >= 2 }
                .flatMapLatest { q: String ->
                    repository.searchFoods(q)
                        .catch { e ->
                            Log.e("LogFood", "Search error", e)
                            emit(emptyList<FoodItem>())
                        }
                }
                .map { list -> list.map(::toFoodItemUi) }
                .collect { uiList -> _searchResults.value = uiList }
        }
    }

    suspend fun getTargetCaloriesOrDefault(default: Double): Double {
        val email = sessionManager.currentUserEmailFlow().first() ?: return default
        return userProfileRepository.getUserFlow(email).first()?.targetCalories?.toDouble() ?: default
    }

    fun onQueryChanged(query: String) { _query.value = query }

    fun addFood(foodItem: FoodItemUi, grams: Double) {
        viewModelScope.launch {
            val email = sessionManager.currentUserEmailFlow().first() ?: return@launch
            val qtySafe = grams.coerceAtLeast(1.0)
            val baseQty = if (foodItem.servingQuantity > 0.0) foodItem.servingQuantity else 100.0
            val factor = qtySafe / baseQty

            val totalCals = foodItem.calories * factor
            val totalProt = foodItem.protein * factor
            val totalCarb = foodItem.carbs * factor
            val totalFat = foodItem.fats * factor

            val foodLog = FoodLog(
                name = foodItem.name,
                calories = totalCals,
                protein = totalProt,
                carbs = totalCarb,
                fats = totalFat,
                quantity = qtySafe,
                unit = foodItem.servingUnit,
                mealType = getMealType(),
                date = LocalDate.now()
            )

            try {
                android.util.Log.d("DEBUG_SYNC", "Adding food: name=${foodItem.name}, qty=$qtySafe, factor=$factor, mealType=${foodLog.mealType}")
                
                repository.addFoodToDailyIntake(email, foodLog, foodLog.mealType, factor)
                repository.getTodayMacros(email).collect()
                userProfileRepository.refreshProfile()
            } catch (e: Exception) {
                Log.e("CRASH_FIX", "Food log failed", e)
                _uiState.update { it.copy(error = "Failed to sync with backend. Please try again.") }
            }
        }
    }

    fun addManualFood(
        name: String,
        quantity: Double,
        calories: Double,
        protein: Double,
        carbs: Double,
        fats: Double
    ) {
        viewModelScope.launch {
            val email = sessionManager.currentUserEmailFlow().first() ?: return@launch
            val gramsSafe = quantity.coerceAtLeast(1.0)
            val per100Factor = 100.0 / gramsSafe
            
            val foodLog = FoodLog(
                name = name,
                calories = calories,
                protein = protein,
                carbs = carbs,
                fats = fats,
                quantity = gramsSafe,
                unit = "g",
                mealType = "Manual Entry",
                date = LocalDate.now()
            )

            try {
                android.util.Log.d("DEBUG_SYNC", "Adding manual food: name=$name, qty=$gramsSafe, mealType=Manual Entry")
                
                // For manual entry, the 'factor' we pass to history is (gramsSafe / 100.0) 
                // and FoodLog.unit is usually "g"
                repository.addFoodToDailyIntake(email, foodLog, "Manual Entry", (gramsSafe / 100.0))
                repository.getTodayMacros(email).collect()
                userProfileRepository.refreshProfile()
            } catch (e: Exception) {
                Log.e("CRASH_FIX", "Manual food failed", e)
                _uiState.update { it.copy(error = "Failed to sync with backend. Please try again.") }
            }
        }
    }

    fun onImageSelected(uri: Uri) {
        viewModelScope.launch {
        _uiState.update {
            it.copy(
                imageUri = uri,
                isLoading = true,
                error = null,
                nutrition = emptyList(),
                scanMessage = "Analyzing image..."
            )
        }
            try {
                // 1) Prepare Image
                val inputStream = getApplication<Application>().contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes() ?: throw Exception("Failed to read image")
                inputStream.close()
                val mediaType = (getApplication<Application>().contentResolver.getType(uri) ?: "image/*").toMediaTypeOrNull()
                val requestFile = okhttp3.RequestBody.create(mediaType, bytes)
                val body = okhttp3.MultipartBody.Part.createFormData("image", "food_scan.jpg", requestFile)

                // 2) Call Backend (localLabels REMOVED to prevent bias)
                Log.d("NutriSoul", "NUTRI-VIEW-V2: Scanning food with ZERO local labels to prevent bias.")
                repository.scanFood(body, "").collect { response ->
                    if (response != null && !response.detectedItems.isNullOrEmpty()) {
                        val items = response.detectedItems.map { detected ->
                            FoodItemUi(
                                id = 0L,
                                name = detected.name,
                                calories = detected.calories,
                                protein = detected.protein,
                                carbs = detected.carbs,
                                fats = detected.fats,
                                fiber = detected.fiber,
                                sugar = detected.sugar,
                                saturatedFat = detected.saturatedFat,
                                vitaminA = detected.vitaminA,
                                vitaminC = detected.vitaminC,
                                vitaminD = detected.vitaminD,
                                vitaminB12 = detected.vitaminB12,
                                calcium = detected.calcium,
                                iron = detected.iron,
                                magnesium = detected.magnesium,
                                potassium = detected.potassium,
                                sodium = detected.sodium,
                                zinc = detected.zinc,
                                healthAlternative = detected.healthierAlternative,
                                proTip = detected.proTip,
                                servingQuantity = detected.servingQuantity,
                                servingUnit = detected.servingUnit,
                                confidence = detected.confidence,
                                source = detected.source
                            )
                        }
                        _uiState.update { it.copy(
                            isLoading = false,
                            nutrition = items,
                            scanMessage = response.message,
                            error = null
                        ) }
                    } else {
                        _uiState.update { it.copy(isLoading = false, error = response?.message ?: "Could not detect food.") }
                    }
                }
            } catch (e: Exception) {
                Log.e("LogFood", "Scan error", e)
                _uiState.update { it.copy(isLoading = false, error = "Scan failed.") }
            }
        }
    }

    private fun getMealType(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 6..10 -> "Breakfast"
            in 12..15 -> "Lunch"
            in 18..21 -> "Dinner"
            else -> "Snack"
        }
    }

    private fun toFoodItemUi(foodItem: FoodItem): FoodItemUi {
        return FoodItemUi(
            id = foodItem.id,
            name = foodItem.name,
            calories = foodItem.caloriesPer100g,
            protein = foodItem.proteinPer100g,
            carbs = foodItem.carbsPer100g,
            fats = foodItem.fatsPer100g,
            servingQuantity = foodItem.servingQuantity,
            servingUnit = foodItem.servingUnit
        )
    }
}
