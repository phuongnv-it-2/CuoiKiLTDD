package com.project24itb156.gglens.repository



import android.content.Context
import android.util.Log
import com.project24itb156.gglens.api.LoginRequest
import com.project24itb156.gglens.api.RegisterRequest
import com.project24itb156.gglens.api.RetrofitClient
import com.project24itb156.gglens.api.UserDto
import com.project24itb156.gglens.data.TokenManager
import kotlinx.coroutines.flow.Flow

class AuthRepository(context: Context) {

    private val authApi = RetrofitClient.authApi
    private val tokenManager = TokenManager(context)

    val isLoggedInFlow: Flow<Boolean> = tokenManager.isLoggedInFlow

    suspend fun register(email: String, password: String, displayName: String?): Result<UserDto> {
        return try {
            val response = authApi.register(RegisterRequest(email, password, displayName))
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                saveSession(body.token, body.user)
                Result.success(body.user)
            } else {
                Result.failure(Exception(parseError(response.errorBody()?.string())))
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "register error: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun login(email: String, password: String): Result<UserDto> {
        return try {
            val response = authApi.login(LoginRequest(email, password))
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                saveSession(body.token, body.user)
                Result.success(body.user)
            } else {
                Result.failure(Exception(parseError(response.errorBody()?.string())))
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "login error: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun restoreSession(): Boolean {
        val token = tokenManager.getToken()
        return if (token != null) {
            RetrofitClient.authToken = token
            true
        } else {
            false
        }
    }

    suspend fun logout() {
        tokenManager.clearSession()
        RetrofitClient.authToken = null
    }

    private suspend fun saveSession(token: String, user: UserDto) {
        RetrofitClient.authToken = token
        tokenManager.saveSession(token, user.id, user.email, user.displayName)
    }

    private fun parseError(errorBody: String?): String {
        return try {
            val json = org.json.JSONObject(errorBody ?: "{}")
            json.optString("error", "Đã có lỗi xảy ra")
        } catch (e: Exception) {
            "Đã có lỗi xảy ra"
        }
    }
}