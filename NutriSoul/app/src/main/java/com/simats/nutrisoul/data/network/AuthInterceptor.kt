package com.simats.nutrisoul.data.network

import com.simats.nutrisoul.data.SessionManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val sessionManager: SessionManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val token = sessionManager.getToken()

        val requestBuilder = originalRequest.newBuilder()
        if (!token.isNullOrBlank()) {
            // Reverted to "Token" as many Django backends use this prefix instead of "Bearer"
            requestBuilder.addHeader("Authorization", "Token $token")
        }

        return chain.proceed(requestBuilder.build())
    }
}
