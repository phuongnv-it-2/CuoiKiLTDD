package com.project24itb156.gglens.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthApi {

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @GET("api/auth/me")
    suspend fun getMe(@Header("Authorization") bearerToken: String): Response<MeResponse>
}

// ─── Request DTOs ─────────────────────────────────────────────────────────────

data class RegisterRequest(
    val email: String,
    val password: String,
    val displayName: String? = null
)

data class LoginRequest(
    val email: String,
    val password: String
)

// ─── Response DTOs ────────────────────────────────────────────────────────────

data class AuthResponse(
    val success: Boolean,
    val token: String,
    val user: UserDto
)

data class UserDto(
    val id: Int,
    val email: String,
    val displayName: String?
)

data class MeResponse(
    val success: Boolean,
    val user: UserDto
)