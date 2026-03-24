package com.simats.nutrisoul.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore("session_manager")

@Singleton
class SessionManager @Inject constructor(private val context: Context) {

    private val KEY_CURRENT_USER_EMAIL = stringPreferencesKey("current_user_email")
    private val KEY_AUTH_TOKEN = stringPreferencesKey("auth_token")

    fun currentUserEmailFlow(): Flow<String?> = context.dataStore.data.map { 
        it[KEY_CURRENT_USER_EMAIL] 
    }

    suspend fun setCurrentUser(email: String) {
        val normalizedEmail = email.trim().lowercase()
        context.dataStore.edit { it[KEY_CURRENT_USER_EMAIL] = normalizedEmail }
    }

    suspend fun saveToken(token: String) {
        context.dataStore.edit { it[KEY_AUTH_TOKEN] = token }
    }

    fun getToken(): String? = runBlocking {
        context.dataStore.data.map { it[KEY_AUTH_TOKEN] }.firstOrNull()
    }

    fun isLoggedIn(): Boolean = !getToken().isNullOrBlank()

    suspend fun clearSession() {
        context.dataStore.edit { 
            it.remove(KEY_CURRENT_USER_EMAIL)
            it.remove(KEY_AUTH_TOKEN)
        }
    }
}
