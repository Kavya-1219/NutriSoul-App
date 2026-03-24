package com.simats.nutrisoul

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.simats.nutrisoul.data.UserProfileRepository
import com.simats.nutrisoul.data.network.*
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

enum class ResetStep { EMAIL, OTP, NEW_PASSWORD }

sealed class ResetUiState {
    data object Idle : ResetUiState()
    data object Loading : ResetUiState()
    data class Error(val message: String) : ResetUiState()
    data class Message(val text: String) : ResetUiState()
    data object Done : ResetUiState()
}



@HiltViewModel
class ResetPasswordViewModel @Inject constructor(
    private val repository: UserProfileRepository
) : ViewModel() {

    var step by mutableStateOf(ResetStep.EMAIL)
        private set

    var email by mutableStateOf("")
        private set

    var otp by mutableStateOf("")
        private set

    var newPassword by mutableStateOf("")
        private set

    var confirmPassword by mutableStateOf("")
        private set

    // token is no longer returned explicitly but part of the 3-step Django flow
    var resetToken by mutableStateOf<String?>(null)
        private set

    var uiState by mutableStateOf<ResetUiState>(ResetUiState.Idle)
        private set

    // resend timer
    var resendSeconds by mutableStateOf(0)
        private set

    private var timerJob: Job? = null

    val isEmailValid: Boolean get() = Validation.isEmailValid(email)
    val isOtpValid: Boolean get() = otp.length == 6
    val isPasswordValid: Boolean get() = Validation.isPasswordValid(newPassword)
    val isPasswordMatch: Boolean get() = newPassword == confirmPassword

    fun onEmailChange(v: String) { email = v.trim() }
    fun onOtpChange(v: String) { otp = v.filter { it.isDigit() }.take(6) }
    fun onNewPasswordChange(v: String) { newPassword = v }
    fun onConfirmPasswordChange(v: String) { confirmPassword = v }

    fun clearUiMessage() {
        if (uiState is ResetUiState.Message || uiState is ResetUiState.Error) {
            uiState = ResetUiState.Idle
        }
    }

    private fun startResendTimer(seconds: Int = 120) {
        timerJob?.cancel()
        resendSeconds = seconds
        timerJob = viewModelScope.launch {
            while (resendSeconds > 0) {
                delay(1000)
                resendSeconds -= 1
            }
        }
    }

    fun sendOtp() {
        if (!isEmailValid) {
            uiState = ResetUiState.Error("Please enter a valid email address.")
            return
        }
        uiState = ResetUiState.Loading

        viewModelScope.launch {
            val result = repository.forgotPassword(ForgotPasswordRequest(email))
            if (result.isSuccess) {
                uiState = ResetUiState.Message("OTP sent to $email")
                step = ResetStep.OTP
                startResendTimer(120)
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: ""
                if (errorMsg.contains("500")) {
                    // Special case: Backend generated OTP but failed to send email (SMTP error)
                    // We allow proceeding to OTP step so developer can use OTP from console
                    uiState = ResetUiState.Message("Server error during email delivery. You can try entering the OTP if you have it.")
                    step = ResetStep.OTP
                    startResendTimer(30)
                } else {
                    uiState = ResetUiState.Error(if (errorMsg.isNotBlank()) errorMsg else "Email likely not registered.")
                    step = ResetStep.EMAIL
                }
            }
        }
    }

    fun resendOtp() {
        if (resendSeconds > 0) return
        sendOtp()
    }

    fun verifyOtp() {
        if (!isOtpValid) {
            uiState = ResetUiState.Error("Enter a valid 6-digit OTP.")
            return
        }
        uiState = ResetUiState.Loading

        viewModelScope.launch {
            val result = repository.verifyOtp(VerifyOtpRequest(email, otp))
            if (result.isSuccess) {
                uiState = ResetUiState.Idle
                step = ResetStep.NEW_PASSWORD
            } else {
                uiState = ResetUiState.Error(result.exceptionOrNull()?.message ?: "Invalid or expired OTP.")
            }
        }
    }

    fun confirmReset() {
        if (!isPasswordValid) {
            uiState = ResetUiState.Error(Validation.passwordHint())
            return
        }
        if (!isPasswordMatch) {
            uiState = ResetUiState.Error("Passwords do not match.")
            return
        }
        uiState = ResetUiState.Loading
        viewModelScope.launch {
            val result = repository.resetPassword(ResetPasswordRequest(email, otp, newPassword, confirmPassword))
            if (result.isSuccess) {
                uiState = ResetUiState.Done
            } else {
                uiState = ResetUiState.Error(result.exceptionOrNull()?.message ?: "Failed to reset password. Session might have expired.")
            }
        }
    }

    fun back(navBack: () -> Unit) {
        clearUiMessage()
        when (step) {
            ResetStep.EMAIL -> navBack()
            ResetStep.OTP -> step = ResetStep.EMAIL
            ResetStep.NEW_PASSWORD -> step = ResetStep.OTP
        }
    }
}
