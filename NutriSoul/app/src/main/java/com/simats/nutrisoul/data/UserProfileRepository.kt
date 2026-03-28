package com.simats.nutrisoul.data

import com.google.gson.Gson
import com.simats.nutrisoul.data.network.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

interface UserProfileRepository {
    val profileUpdateEvents: Flow<Unit>
    fun getUserFlow(email: String): Flow<User?>
    suspend fun refreshProfile(): Result<User>
    suspend fun login(request: LoginRequest): Result<LoginResponse>
    suspend fun register(request: RegisterRequest): Result<RegisterResponse>
    suspend fun forgotPassword(request: ForgotPasswordRequest): Result<ForgotPasswordResponse>
    suspend fun verifyOtp(request: VerifyOtpRequest): Result<VerifyOtpResponse>
    suspend fun resetPassword(request: ResetPasswordRequest): Result<ResetPasswordResponse>
    suspend fun updateProfile(request: UpdateProfileRequestDto): Result<User>
    suspend fun uploadProfilePicture(image: MultipartBody.Part): Result<String>
    suspend fun changePassword(request: ChangePasswordRequestDto): Result<String>
    suspend fun logout()
}

@Singleton
class UserProfileRepositoryImpl @Inject constructor(
    private val userApi: UserApi,
    private val userDao: UserDao,
    private val sessionManager: SessionManager,
    private val gson: Gson
) : UserProfileRepository {

    private val _profileUpdateEvents = MutableSharedFlow<Unit>(replay = 0)
    override val profileUpdateEvents: Flow<Unit> = _profileUpdateEvents.asSharedFlow()

    override fun getUserFlow(email: String): Flow<User?> = userDao.getUserByEmail(email)

    override suspend fun refreshProfile(): Result<User> {
        if (!sessionManager.isLoggedIn()) {
            android.util.Log.w("SYNC", "Skipping profile refresh: User not logged in")
            return Result.failure(Exception("Not logged in"))
        }
        return try {
            val dto = userApi.getProfile()
            val user = dto.toUser()
            userDao.insertUser(user)
            android.util.Log.d("SYNC", "Profile successfully refreshed from backend")
            Result.success(user)
        } catch (e: Exception) {
            android.util.Log.e("SYNC", "Failed to refresh profile: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun login(request: LoginRequest): Result<LoginResponse> {
        return try {
            val response = userApi.login(request)
            sessionManager.saveToken(response.token)
            sessionManager.setCurrentUser(response.email)
            // Senior Review: Add retry for first profile fetch as backend might be lagging for new accounts
            for (i in 0..2) {
                if (refreshProfile().isSuccess) break
                if (i < 2) kotlinx.coroutines.delay(1000L * (i + 1))
            }
            Result.success(response)
        } catch (e: Exception) {
            val message = parseErrorBody(e)
            android.util.Log.e("SYNC", "Login failed: $message")
            Result.failure(Exception(message))
        }
    }

    override suspend fun register(request: RegisterRequest): Result<RegisterResponse> {
        return try {
            val response = userApi.register(request)
            // Senior Review: Enable auto-login after successful registration
            sessionManager.saveToken(response.token)
            sessionManager.setCurrentUser(response.user.email)
            // Senior Review: Add retry for first profile fetch as backend might be lagging for new accounts
            for (i in 0..2) {
                if (refreshProfile().isSuccess) break
                if (i < 2) kotlinx.coroutines.delay(1000L * (i + 1))
            }
            Result.success(response)
        } catch (e: Exception) {
            val message = parseErrorBody(e)
            android.util.Log.e("SYNC", "Registration failed: $message")
            Result.failure(Exception(message))
        }
    }

    private fun parseErrorBody(e: Exception): String {
        if (e is HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            if (errorBody.isNullOrBlank()) {
                return "HTTP ${e.code()}: ${e.message().ifBlank { "Unspecified error" }}"
            }
            return try {
                val errorResponse = gson.fromJson(errorBody, ErrorResponse::class.java)
                errorResponse.error ?: errorResponse.message ?: errorResponse.detail ?: run {
                    // Fallback for field-specific errors
                    val sb = StringBuilder()
                    errorResponse.email?.let { sb.append("Email: ${it.joinToString()}\n") }
                    errorResponse.username?.let { sb.append("Username: ${it.joinToString()}\n") }
                    errorResponse.password?.let { sb.append("Password: ${it.joinToString()}\n") }
                    if (sb.isEmpty()) "HTTP ${e.code()}: $errorBody" else sb.toString().trim()
                }
            } catch (jsonEx: Exception) {
                "HTTP ${e.code()}: $errorBody"
            }
        }
        return e.message ?: "An unknown error occurred: ${e.javaClass.simpleName}"
    }

    override suspend fun forgotPassword(request: ForgotPasswordRequest): Result<ForgotPasswordResponse> {
        return try {
            val response = userApi.forgotPassword(request)
            Result.success(response)
        } catch (e: Exception) {
            val message = parseErrorBody(e)
            android.util.Log.e("SYNC", "Forgot password request failed: $message")
            Result.failure(Exception(message))
        }
    }

    override suspend fun verifyOtp(request: VerifyOtpRequest): Result<VerifyOtpResponse> {
        return try {
            val response = userApi.verifyOtp(request)
            Result.success(response)
        } catch (e: Exception) {
            val message = parseErrorBody(e)
            android.util.Log.e("SYNC", "OTP verification failed: $message")
            Result.failure(Exception(message))
        }
    }

    override suspend fun resetPassword(request: ResetPasswordRequest): Result<ResetPasswordResponse> {
        return try {
            val response = userApi.resetPassword(request)
            Result.success(response)
        } catch (e: Exception) {
            val message = parseErrorBody(e)
            android.util.Log.e("SYNC", "Password reset failed: $message")
            Result.failure(Exception(message))
        }
    }

    override suspend fun updateProfile(request: UpdateProfileRequestDto): Result<User> {
        return try {
            val dto = userApi.updateProfile(request)
            val user = dto.toUser()
            userDao.insertUser(user)
            _profileUpdateEvents.emit(Unit)
            Result.success(user)
        } catch (e: Exception) {
            android.util.Log.e("SYNC", "Profile update failed: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun uploadProfilePicture(image: MultipartBody.Part): Result<String> {
        return try {
            val response = userApi.uploadProfilePicture(image)
            // Senior Review fix: Handle refresh failure instead of ignoring it
            val refreshResult = refreshProfile()
            if (refreshResult.isFailure) {
                android.util.Log.w("SYNC", "Picture uploaded but profile refresh failed")
            }
            _profileUpdateEvents.emit(Unit)
            Result.success(response.profilePictureUrl)
        } catch (e: Exception) {
            android.util.Log.e("SYNC", "Avatar upload failed: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun changePassword(request: ChangePasswordRequestDto): Result<String> {
        return try {
            val response = userApi.changePassword(request)
            Result.success(response.message)
        } catch (e: Exception) {
            android.util.Log.e("SYNC", "Password change failed: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        sessionManager.clearSession()
        // We do NOT clear the userDao here to allow for offline access of the profile 
        // until the next user logs in. The SessionManager will still block data access
        // based on the null currentUserEmail.
        android.util.Log.d("SYNC", "User session cleared. Local data preserved for offline consistency.")
    }
}
