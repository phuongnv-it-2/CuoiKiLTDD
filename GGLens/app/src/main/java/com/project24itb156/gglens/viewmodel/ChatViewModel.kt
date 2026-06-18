package com.project24itb156.gglens.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project24itb156.gglens.model.ChatRole
import com.project24itb156.gglens.model.ChatUiMessage
import com.project24itb156.gglens.repository.ChatRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {

    private val repository = ChatRepository()

    private val _messages = MutableStateFlow<List<ChatUiMessage>>(emptyList())
    val messages: StateFlow<List<ChatUiMessage>> = _messages.asStateFlow()

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadHistory()
    }

    fun loadHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.loadHistory().onSuccess { history ->
                _messages.value = history
            }
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank() || _isSending.value) return

        val userMsg = ChatUiMessage(role = ChatRole.USER, content = text.trim())
        _messages.value = _messages.value + userMsg
        _isSending.value = true
        _errorMessage.value = null

        viewModelScope.launch(Dispatchers.IO) {
            repository.sendMessage(text.trim()).fold(
                onSuccess = { reply ->
                    _messages.value = _messages.value + ChatUiMessage(role = ChatRole.AI, content = reply)
                },
                onFailure = { e ->
                    _errorMessage.value = e.message ?: "Không gửi được tin nhắn"
                }
            )
            _isSending.value = false
        }
    }

    fun clearChat() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearHistory()
            _messages.value = emptyList()
        }
    }
}