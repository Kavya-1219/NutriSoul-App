package com.simats.nutrisoul

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.simats.nutrisoul.data.SessionManager
import com.simats.nutrisoul.data.UserViewModel
import com.simats.nutrisoul.settings.SettingsModel
import com.simats.nutrisoul.settings.SettingsStore
import com.simats.nutrisoul.ui.theme.NutriSoulTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity(), SensorEventListener {

    @Inject lateinit var sessionManager: SessionManager
    @Inject lateinit var settingsStore: SettingsStore

    private lateinit var sensorManager: SensorManager
    private var stepCounterSensor: Sensor? = null
    private var initialSteps: Int = -1

    private val userViewModel: UserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        handleWindDownIntent(intent)

        setContent {
            val navController = rememberNavController()
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            // 1️⃣ Get the current user email
            val emailState by sessionManager.currentUserEmailFlow()
                .collectAsStateWithLifecycle(initialValue = null)
            
            val email = emailState ?: ""

            // 2️⃣ Observe dark mode for that user
            val settings by settingsStore.observe(email)
                .collectAsStateWithLifecycle(
                    initialValue = SettingsModel(
                        userName = "User",
                        darkMode = false,
                        profilePictureUri = null
                    )
                )

            val isDarkThemeRequested = settings.darkMode

            // List of routes that should ALWAYS be light theme
            val lightOnlyRoutes = listOf(
                Screen.Splash.route,
                Screen.Onboarding1.route,
                Screen.Onboarding2.route,
                Screen.Onboarding3.route,
                Screen.Login.route,
                Screen.Register.route,
                Screen.ResetPassword.route,
                Screen.PersonalDetails.route,
                Screen.BodyDetails.route,
                Screen.FoodPreferences.route,
                Screen.LifestyleAndActivity.route,
                Screen.Goals.route,
                Screen.GoalWeight.route,
                Screen.HealthConditions.route,
                Screen.HealthDetails.route,
                Screen.MealsPerDay.route
            )

            // Normalize route comparison (remove arguments if any)
            val baseRoute = currentRoute?.split("/")?.firstOrNull() ?: ""
            val darkTheme = if (baseRoute in lightOnlyRoutes || currentRoute in lightOnlyRoutes) false else isDarkThemeRequested

            // 3️⃣ Apply theme
            NutriSoulTheme(darkTheme = darkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavGraph(navController = navController)
                }
            }
        }

        lifecycleScope.launch {
            userViewModel.automaticTracking.collectLatest { enabled ->
                if (enabled && stepCounterSensor != null) {
                    registerStepSensor()
                } else {
                    unregisterStepSensor()
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleWindDownIntent(intent)
    }

    private fun handleWindDownIntent(intent: Intent?) {
        val show = intent?.getBooleanExtra("SHOW_WIND_DOWN", false) ?: false
        val userEmail = intent?.getStringExtra("USER_EMAIL") ?: ""
        if (show && userEmail.isNotBlank()) {
            // Check if MindCarePrefs exists or needs to be created
            // MindCarePrefs.setPendingWindDown(this, userEmail, true)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        val totalSteps = event.values[0].toInt()
        if (initialSteps == -1) {
            lifecycleScope.launch {
                val user = userViewModel.user.first()
                val stepsToday = user?.todaysSteps ?: 0
                initialSteps = totalSteps - stepsToday
            }
        }
        val newSteps = totalSteps - initialSteps
        userViewModel.updateStepsFromSensor(newSteps)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun registerStepSensor() {
        if (stepCounterSensor != null) {
            sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    private fun unregisterStepSensor() {
        sensorManager.unregisterListener(this)
        initialSteps = -1
    }
}
