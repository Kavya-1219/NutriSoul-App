package com.simats.nutrisoul.data

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.simats.nutrisoul.settings.SettingsStore
import com.simats.nutrisoul.data.network.*
import com.simats.nutrisoul.steps.StepTrackingService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    application: Application,
    private val sessionManager: SessionManager,
    private val settingsStore: SettingsStore,
    private val repository: UserProfileRepository
) : AndroidViewModel(application) {

    // Start loading true to prevent "Error loading user data" on initial composition
    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _automaticTracking = MutableStateFlow(false)
    val automaticTracking = _automaticTracking.asStateFlow()

    // Single source of truth from local database Flow
    val user: StateFlow<User?> = sessionManager.currentUserEmailFlow()
        .flatMapLatest { email ->
            if (email.isNullOrBlank()) flowOf(null)
            else repository.getUserFlow(email)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)


    // Professional: Use SettingsStore as the single source of truth for app-wide settings
    val darkMode = sessionManager.currentUserEmailFlow().flatMapLatest { email ->
        settingsStore.observe(email ?: "").map { it.darkMode }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val profilePictureUri = sessionManager.currentUserEmailFlow().flatMapLatest { email ->
        settingsStore.observe(email ?: "").map { it.profilePictureUri }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val userName = sessionManager.currentUserEmailFlow().flatMapLatest { email ->
        settingsStore.observe(email ?: "").map { it.userName }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "User")

    private val _profileUpdated = MutableSharedFlow<Unit>(replay = 0)
    val profileUpdated = _profileUpdated.asSharedFlow()

    init {
        viewModelScope.launch {
            sessionManager.currentUserEmailFlow().collect { email ->
                if (!email.isNullOrBlank()) {
                    _isLoading.value = true
                    try {
                        repository.refreshProfile()
                    } finally {
                        _isLoading.value = false
                    }
                    syncSettings(email)
                } else {
                    _isLoading.value = false
                    getApplication<Application>().stopService(Intent(getApplication(), StepTrackingService::class.java))
                }
            }
        }
    }


    private fun syncSettings(email: String) {
        viewModelScope.launch {
            user.collect { user ->
                user?.let {
                    settingsStore.setDarkMode(email, it.darkMode)
                    if (it.name.isNotBlank()) {
                        settingsStore.setUserName(email, it.name)
                    }
                    if (!it.profilePictureUrl.isNullOrBlank()) {
                        settingsStore.setProfilePicture(email, Uri.parse(it.profilePictureUrl))
                    }
                }
            }
        }
    }

    fun updateUser(updatedUser: User) {
        viewModelScope.launch {
            val email = sessionManager.currentUserEmailFlow().firstOrNull() ?: return@launch
            val safeEmail = email.trim().lowercase()
            
            // Pro-tip: Ensure the email in the User object matches the session
            val userToUpdate = updatedUser.copy(email = safeEmail)
            
            val dto = UpdateProfileRequestDto(
                name = userToUpdate.name,
                age = userToUpdate.age,
                gender = userToUpdate.gender,
                height = userToUpdate.height,
                weight = userToUpdate.weight,
                goals = userToUpdate.goals,
                activityLevel = userToUpdate.activityLevel,
                targetWeight = userToUpdate.targetWeight,
                currentWeight = userToUpdate.currentWeight,
                targetWeeks = userToUpdate.targetWeeks,
                mealsPerDay = userToUpdate.mealsPerDay,
                healthConditions = userToUpdate.healthConditions,
                foodAllergies = userToUpdate.foodAllergies,
                dietaryRestrictions = userToUpdate.dietaryRestrictions,
                systolic = userToUpdate.systolic,
                diastolic = userToUpdate.diastolic,
                thyroidCondition = userToUpdate.thyroidCondition,
                diabetesType = userToUpdate.diabetesType,
                cholesterolLevel = userToUpdate.cholesterolLevel,
                otherAllergies = userToUpdate.otherAllergies,
                dislikes = userToUpdate.dislikes,
                allergies = userToUpdate.allergies,
                targetCalories = userToUpdate.targetCalories,
                dietType = userToUpdate.dietType,
                foodDislikes = userToUpdate.foodDislikes
            )
            val result = repository.updateProfile(dto)
            if (result.isSuccess) {
                _profileUpdated.emit(Unit)
            }
        }
    }

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateProfile(UpdateProfileRequestDto(darkMode = enabled))
            sessionManager.currentUserEmailFlow().firstOrNull()?.let { email ->
                settingsStore.setDarkMode(email, enabled)
            }
        }
    }

    fun setProfilePictureUri(uri: Uri) {
        viewModelScope.launch {
            try {
                val inputStream = getApplication<Application>().contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes() ?: return@launch
                val mediaType = "image/*".toMediaTypeOrNull()
                val requestFile = okhttp3.RequestBody.create(mediaType, bytes)
                val body = MultipartBody.Part.createFormData("profile_picture", "avatar.jpg", requestFile)
                
                val result = repository.uploadProfilePicture(body)
                if (result.isSuccess) {
                    repository.refreshProfile()
                    sessionManager.currentUserEmailFlow().firstOrNull()?.let { email ->
                        settingsStore.setProfilePicture(email, uri)
                    }
                    _profileUpdated.emit(Unit)
                }
            } catch (e: Exception) {
                Log.e("UserViewModel", "Failed to upload profile picture", e)
            }
        }
    }

    fun updateGoals(goals: List<String>) {
        viewModelScope.launch { 
            if (repository.updateProfile(UpdateProfileRequestDto(goals = goals)).isSuccess) {
                _profileUpdated.emit(Unit)
            }
        }
    }

    fun updateTargetWeight(weight: Float, weeks: Int) {
        viewModelScope.launch { 
            if (repository.updateProfile(UpdateProfileRequestDto(targetWeight = weight, targetWeeks = weeks)).isSuccess) {
                _profileUpdated.emit(Unit)
            }
        }
    }

    fun updateCurrentWeight(weight: Float) {
        viewModelScope.launch { 
            if (repository.updateProfile(UpdateProfileRequestDto(currentWeight = weight)).isSuccess) {
                _profileUpdated.emit(Unit)
            }
        }
    }

    fun addCalories(calories: Int) {
        user.value?.let { currentUser ->
            viewModelScope.launch {
                repository.updateProfile(UpdateProfileRequestDto(todaysCalories = currentUser.todaysCalories + calories))
            }
        }
    }

    fun updateWaterIntake(amount: Int) {
        user.value?.let { currentUser ->
            viewModelScope.launch {
                val valToUpdate = (currentUser.todaysWaterIntake + amount).coerceAtLeast(0)
                repository.updateProfile(UpdateProfileRequestDto(todaysWaterIntake = valToUpdate))
            }
        }
    }

    fun updateSteps(steps: Int) {
        user.value?.let { currentUser ->
            viewModelScope.launch {
                val valToUpdate = (currentUser.todaysSteps + steps).coerceAtLeast(0)
                repository.updateProfile(UpdateProfileRequestDto(todaysSteps = valToUpdate))
            }
        }
    }

    fun setAutomaticTracking(enabled: Boolean) {
        _automaticTracking.value = enabled
    }

    fun updateStepsFromSensor(steps: Int) {
        viewModelScope.launch {
            repository.updateProfile(UpdateProfileRequestDto(todaysSteps = steps))
        }
    }

    fun changePassword(old: String, new: String) {
        viewModelScope.launch {
            repository.changePassword(ChangePasswordRequestDto(oldPassword = old, newPassword = new, confirmPassword = new))
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }

    fun retryLoadUserData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.refreshProfile()
            } finally {
                _isLoading.value = false
            }
        }
    }

}
