package com.project24itb156.gglens.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.project24itb156.gglens.model.QrUiState
import com.project24itb156.gglens.repository.LensRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class QrViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = LensRepository(application)

    private val _uiState = MutableStateFlow<QrUiState>(QrUiState.Idle)
    val uiState: StateFlow<QrUiState> = _uiState.asStateFlow()

    fun scanQr(bitmap: Bitmap) {
        _uiState.value = QrUiState.Scanning
        viewModelScope.launch(Dispatchers.IO) {
            repository.analyzeQr(bitmap).fold(
                onSuccess = { result ->
                    _uiState.value = QrUiState.Found(result)
                },
                onFailure = { e ->
                    _uiState.value = QrUiState.Error(e.message ?: "Lỗi quét QR")
                }
            )
        }
    }

    fun reset() {
        _uiState.value = QrUiState.Idle
    }

    override fun onCleared() {
        super.onCleared()
        repository.close()
    }
}