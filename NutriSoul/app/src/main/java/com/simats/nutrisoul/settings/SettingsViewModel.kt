package com.simats.nutrisoul.settings

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.simats.nutrisoul.data.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    application: Application,
    private val sessionManager: SessionManager,
    private val settingsStore: SettingsStore
) : AndroidViewModel(application) {

    private val _ui = MutableStateFlow(SettingsUiState(isLoading = true))
    val ui: StateFlow<SettingsUiState> = _ui.asStateFlow()

    init {
        viewModelScope.launch {
            sessionManager.currentUserEmailFlow().collectLatest { email ->
                if (email != null) {
                    settingsStore.observe(email)
                        .collect { model ->
                            _ui.value = SettingsUiState(
                                isLoading = false,
                                userName = model.userName.ifBlank { "User" },
                                profilePictureUri = model.profilePictureUri,
                                darkMode = model.darkMode
                            )
                        }
                } else {
                    _ui.value = SettingsUiState(isLoading = false)
                }
            }
        }
    }

    fun toggleDarkMode() {
        viewModelScope.launch {
            val email = sessionManager.currentUserEmailFlow().firstOrNull()
            if (email != null) {
                val newMode = !_ui.value.darkMode
                settingsStore.setDarkMode(email, newMode)
            }
        }
    }

    fun setProfilePicture(uri: Uri) {
        viewModelScope.launch {
            val email = sessionManager.currentUserEmailFlow().firstOrNull()
            if (email != null) {
                settingsStore.setProfilePicture(email, uri)
            }
        }
    }

    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            sessionManager.clearSession()
            onDone()
        }
    }
}
