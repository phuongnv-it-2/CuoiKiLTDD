package com.project24itb156.gglens.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.project24itb156.gglens.model.HistoryUiState
import com.project24itb156.gglens.repository.LensRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = LensRepository(application)

    private val _uiState = MutableStateFlow<HistoryUiState>(HistoryUiState.Idle)
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    fun loadHistory() {
        _uiState.value = HistoryUiState.Loading
        viewModelScope.launch {
            val result = repository.getHistory()
            result.fold(
                onSuccess = { items ->
                    _uiState.value = HistoryUiState.Success(items)
                },
                onFailure = { e ->
                    _uiState.value = HistoryUiState.Error(e.message ?: "Không thể tải lịch sử")
                }
            )
        }
    }

    fun deleteHistoryItem(id: Int) {
        viewModelScope.launch {
            repository.deleteHistoryItem(id).onSuccess {
                loadHistory() // Reload after delete
            }
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAllHistory().onSuccess {
                _uiState.value = HistoryUiState.Success(emptyList())
            }
        }
    }
}
