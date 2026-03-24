package com.simats.nutrisoul.data.settings

data class UserSettingsState(
    val isLoading: Boolean = true,
    val userName: String = "User",
    val profileImageUrl: String? = null, // from Django (absolute URL)
    val darkMode: Boolean = false,
    val error: String? = null
)
