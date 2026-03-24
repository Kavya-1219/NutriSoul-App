package com.simats.nutrisoul.data.settings

import com.simats.nutrisoul.data.network.UpdateProfileRequestDto
import com.simats.nutrisoul.data.network.UserApi
import com.simats.nutrisoul.data.SessionManager
import okhttp3.MultipartBody
import javax.inject.Inject

class UserSettingsRepository @Inject constructor(
    private val api: UserApi,
    private val sessionManager: SessionManager
) {
    suspend fun loadMe() = api.getProfile()

    suspend fun setDarkMode(enabled: Boolean) =
        api.updateProfile(UpdateProfileRequestDto(darkMode = enabled))

    suspend fun uploadProfileImage(part: MultipartBody.Part) =
        api.uploadProfilePicture(part)

    suspend fun logout() = sessionManager.clearSession()
}
