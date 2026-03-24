package com.simats.nutrisoul.settings

import android.net.Uri

data class SettingsUiState(
    val isLoading: Boolean = false,
    val userName: String = "User",
    val profilePictureUri: Uri? = null,
    val darkMode: Boolean = false,
    val error: String? = null
)
