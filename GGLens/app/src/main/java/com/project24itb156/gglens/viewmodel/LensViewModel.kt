package com.project24itb156.gglens.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.project24itb156.gglens.model.LensMode
import com.project24itb156.gglens.model.LensResult
import com.project24itb156.gglens.model.LensUiState
import com.project24itb156.gglens.repository.LensRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LensViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = LensRepository(application)

    private val deviceId: String =
        Settings.Secure.getString(application.contentResolver, Settings.Secure.ANDROID_ID)
            ?: "unknown_device"

    private val _uiState = MutableStateFlow<LensUiState>(LensUiState.Idle)
    val uiState: StateFlow<LensUiState> = _uiState.asStateFlow()

    private val _currentMode = MutableStateFlow(LensMode.SEARCH)
    val currentMode: StateFlow<LensMode> = _currentMode.asStateFlow()

    private val _capturedBitmap = MutableStateFlow<Bitmap?>(null)
    val capturedBitmap: StateFlow<Bitmap?> = _capturedBitmap.asStateFlow()

    fun setMode(mode: LensMode) {
        _currentMode.value = mode
    }

    fun analyze(bitmap: Bitmap) {
        _capturedBitmap.value = bitmap
        _uiState.value = LensUiState.Loading

        viewModelScope.launch(Dispatchers.IO) {
            val analyzeResult = repository.analyzeImage(
                resizeBitmap(bitmap),
                _currentMode.value
            )

            analyzeResult.fold(
                onSuccess = { lensResult ->
                    when (_currentMode.value) {
                        LensMode.SEARCH, LensMode.SHOPPING -> handleSearchMode(lensResult)
                        else -> _uiState.value = LensUiState.Success(lensResult)
                    }
                },
                onFailure = { e ->
                    _uiState.value = LensUiState.Error(e.message ?: "Lỗi phân tích ảnh")
                }
            )
        }
    }

    private suspend fun handleSearchMode(lensResult: LensResult) {
        if (lensResult.detectedLabels.isEmpty()) {
            _uiState.value = LensUiState.Error("Không nhận diện được đối tượng")
            return
        }

        val searchResult = repository.search(lensResult.searchQuery)
        val finalResult = searchResult.fold(
            onSuccess = { items -> lensResult.copy(searchResults = items) },
            onFailure = { lensResult }
        )

        _uiState.value = LensUiState.Success(finalResult)

        repository.saveHistory(
            sessionId = finalResult.sessionId,
            query = finalResult.searchQuery,
            mode = _currentMode.value,
            resultCount = finalResult.searchResults.size,
            aiResultId = finalResult.aiResultId
        )
    }


    fun translateText(text: String) {
        val currentState = _uiState.value
        if (currentState is LensUiState.Success) {
            viewModelScope.launch(Dispatchers.IO) {
                // Tạm thời set trạng thái loading nhẹ (không làm trắng màn hình)
                val result = repository.translate(text)
                result.onSuccess { translated ->
                    _uiState.value = LensUiState.Success(
                        currentState.result.copy(translatedText = translated)
                    )
                }.onFailure {
                     // Có thể xử lý thông báo lỗi ở đây
                }
            }
        }
    }

    fun reset() {
        _uiState.value = LensUiState.Idle
        _capturedBitmap.value = null
    }

    private fun resizeBitmap(bitmap: Bitmap, maxSize: Int = 512): Bitmap {
        val ratio = bitmap.width.toFloat() / bitmap.height.toFloat()
        return if (bitmap.width > bitmap.height) {
            Bitmap.createScaledBitmap(bitmap, maxSize, (maxSize / ratio).toInt(), true)
        } else {
            Bitmap.createScaledBitmap(bitmap, (maxSize * ratio).toInt(), maxSize, true)
        }
    }


    override fun onCleared() {
        super.onCleared()
        repository.close()
    }
}
