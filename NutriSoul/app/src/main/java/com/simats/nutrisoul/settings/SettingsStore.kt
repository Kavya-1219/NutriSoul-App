package com.simats.nutrisoul.settings

import android.content.Context
import android.net.Uri
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsDataStore by preferencesDataStore(name = "settings_store")

data class SettingsModel(
    val userName: String,
    val darkMode: Boolean,
    val profilePictureUri: Uri?
)

@Singleton
class SettingsStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private fun keyName(email: String) = stringPreferencesKey("name_$email")
    private fun keyDark(email: String) = booleanPreferencesKey("dark_$email")
    private fun keyPhoto(email: String) = stringPreferencesKey("photo_$email")

    fun observe(email: String): Flow<SettingsModel> {
        return context.settingsDataStore.data.map { prefs ->
            val name = prefs[keyName(email)] ?: "User"
            val dark = prefs[keyDark(email)] ?: false
            val photo = prefs[keyPhoto(email)]?.let { Uri.parse(it) }
            SettingsModel(name, dark, photo)
        }
    }

    suspend fun setDarkMode(email: String, enabled: Boolean) {
        context.settingsDataStore.edit { it[keyDark(email)] = enabled }
    }

    suspend fun setProfilePicture(email: String, uri: Uri) {
        context.settingsDataStore.edit { it[keyPhoto(email)] = uri.toString() }
    }

    suspend fun setUserName(email: String, name: String) {
        context.settingsDataStore.edit { it[keyName(email)] = name }
    }
}
