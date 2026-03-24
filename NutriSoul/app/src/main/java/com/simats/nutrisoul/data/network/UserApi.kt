package com.simats.nutrisoul.data.network

import okhttp3.MultipartBody
import retrofit2.http.*

interface UserApi {

    @POST("login/")
    suspend fun login(@Body body: LoginRequest): LoginResponse

    @POST("register/")
    suspend fun register(@Body body: RegisterRequest): RegisterResponse

    @POST("forgot-password/")
    suspend fun forgotPassword(@Body body: ForgotPasswordRequest): ForgotPasswordResponse

    @POST("verify-otp/")
    suspend fun verifyOtp(@Body body: VerifyOtpRequest): VerifyOtpResponse

    @POST("reset-password/")
    suspend fun resetPassword(@Body body: ResetPasswordRequest): ResetPasswordResponse

    @GET("profile/")
    suspend fun getProfile(): ProfileResponseDto

    @PATCH("profile/")
    suspend fun updateProfile(@Body body: UpdateProfileRequestDto): ProfileResponseDto

    @POST("profile/password/")
    suspend fun changePassword(@Body body: ChangePasswordRequestDto): GenericResponse

    @Multipart
    @POST("profile/picture/")
    suspend fun uploadProfilePicture(
        @Part profile_picture: MultipartBody.Part
    ): ProfilePictureUploadResponse
}

data class GenericResponse(val message: String)
