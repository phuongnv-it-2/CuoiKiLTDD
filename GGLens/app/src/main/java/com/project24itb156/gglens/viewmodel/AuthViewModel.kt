package com.project24itb156.gglens.viewmodel



import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.project24itb156.gglens.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    object Success : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AuthRepository(application)

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    init {
        viewModelScope.launch {
            _isLoggedIn.value = repository.restoreSession()
        }
    }

    fun register(email: String, password: String, displayName: String?) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState.Error("Vui lòng điền đầy đủ email và password")
            return
        }
        _uiState.value = AuthUiState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.register(email.trim(), password, displayName?.trim())
            result.fold(
                onSuccess = {
                    _isLoggedIn.value = true
                    _uiState.value = AuthUiState.Success
                },
                onFailure = { e ->
                    _uiState.value = AuthUiState.Error(e.message ?: "Đăng ký thất bại")
                }
            )
        }
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState.Error("Vui lòng điền đầy đủ email và password")
            return
        }
        _uiState.value = AuthUiState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.login(email.trim(), password)
            result.fold(
                onSuccess = {
                    _isLoggedIn.value = true
                    _uiState.value = AuthUiState.Success
                },
                onFailure = { e ->
                    _uiState.value = AuthUiState.Error(e.message ?: "Đăng nhập thất bại")
                }
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _isLoggedIn.value = false
            _uiState.value = AuthUiState.Idle
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }
}