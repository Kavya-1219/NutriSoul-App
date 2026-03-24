package com.simats.nutrisoul.ui.steps
 
import kotlinx.coroutines.ExperimentalCoroutinesApi

import android.app.Application
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.simats.nutrisoul.data.SessionManager
import com.simats.nutrisoul.data.repository.StepsRepository
import com.simats.nutrisoul.steps.StepTrackingService
import com.simats.nutrisoul.steps.StepsStore
import com.simats.nutrisoul.data.UserProfileRepository
import com.simats.nutrisoul.data.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class StepsViewModel @Inject constructor(
    app: Application,
    private val stepsRepository: StepsRepository,
    private val userProfileRepository: UserProfileRepository
) : AndroidViewModel(app) {

    private val store = StepsStore(app.applicationContext)
    private val sessionManager = SessionManager(app.applicationContext)

    private suspend fun currentUserKey(): String {
        return sessionManager.currentUserEmailFlow().first() ?: "guest"
    }

    val todaySteps: StateFlow<Int> =
        sessionManager.currentUserEmailFlow()
            .flatMapLatest { email -> store.todayStepsFlow(email ?: "guest") }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val autoEnabled: StateFlow<Boolean> =
        sessionManager.currentUserEmailFlow()
            .flatMapLatest { email -> store.autoEnabledFlow(email ?: "guest") }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val weeklyAverageSteps: StateFlow<Long> =
        stepsRepository.getWeeklySteps()
            .map { list ->
                if (list.isEmpty()) 0L
                else {
                    val total = list.sumOf { it.steps + it.manualSteps }
                    total / list.size
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val caloriesBurned: StateFlow<Double> =
        combine(
            todaySteps,
            sessionManager.currentUserEmailFlow().flatMapLatest { email ->
                if (email == null) flowOf(null) else userProfileRepository.getUserFlow(email)
            }
        ) { steps, user ->
            val weight = user?.weight?.toDouble() ?: 70.0 // Default 70kg if weight unknown
            // Formula from QA Report: (steps / 1000) * (weight * 0.4)
            (steps.toDouble() / 1000.0) * (weight * 0.4)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    val distanceKm: StateFlow<Double> =
        todaySteps.map { steps ->
            // Approx 0.762 meters per step
            (steps * 0.762) / 1000.0
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    init {
        viewModelScope.launch {
            stepsRepository.fetchStepsFromBackend()
        }
    }

    val isSensorAvailable: Boolean by lazy {
        val sensorManager = app.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null
    }

    fun setAutoTracking(enabled: Boolean) {
        if (enabled && !isSensorAvailable) return // Defensive check

        val ctx = getApplication<Application>().applicationContext
        val intent = Intent(ctx, StepTrackingService::class.java)

        viewModelScope.launch {
            val userKey = currentUserKey()
            store.setAutoEnabled(userKey, enabled)
        }

        if (enabled) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ctx.startForegroundService(intent)
            } else {
                ctx.startService(intent)
            }
        } else {
            ctx.stopService(intent)
        }
    }

    fun addManualSteps(steps: Int) {
        if (steps <= 0) return
        viewModelScope.launch {
            val userKey = currentUserKey()
            // Update local store for instant UI feedback
            store.addManualSteps(userKey, steps)
            // Sync to backend and Room
            stepsRepository.logManualSteps(steps)
        }
    }

    fun removeManualSteps(steps: Int) {
        if (steps <= 0) return
        viewModelScope.launch {
            val userKey = currentUserKey()
            // Update local store for instant UI feedback
            store.removeManualSteps(userKey, steps)
            // Sync to backend and Room (as negative delta)
            stepsRepository.logManualSteps(-steps)
        }
    }
}
