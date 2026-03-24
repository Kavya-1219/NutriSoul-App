package com.simats.nutrisoul.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserPreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    val isAutoStepsTrackingEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_AUTO_STEPS_TRACKING] ?: false
    }

    val stepsGoal: Flow<Int> = dataStore.data.map { preferences ->
        preferences[KEY_STEPS_GOAL] ?: 10000
    }

    suspend fun setAutoStepsTracking(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_AUTO_STEPS_TRACKING] = enabled
        }
    }

    suspend fun setStepsGoal(goal: Int) {
        dataStore.edit { preferences ->
            preferences[KEY_STEPS_GOAL] = goal
        }
    }

    private companion object {
        val KEY_AUTO_STEPS_TRACKING = booleanPreferencesKey("auto_steps_tracking_enabled")
        val KEY_STEPS_GOAL = intPreferencesKey("steps_goal")
    }
}
