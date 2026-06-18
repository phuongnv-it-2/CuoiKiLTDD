package com.project24itb156.gglens.repository

import com.project24itb156.gglens.api.ChatMessageDto
import com.project24itb156.gglens.api.RetrofitClient
import com.project24itb156.gglens.api.SendChatRequest
import com.project24itb156.gglens.model.ChatRole
import com.project24itb156.gglens.model.ChatUiMessage

class ChatRepository {

    private val api = RetrofitClient.backendApi

    suspend fun loadHistory(): Result<List<ChatUiMessage>> {
        return try {
            val response = api.getChatHistory()
            if (response.isSuccessful) {
                val messages = response.body()?.data?.map { it.toUiMessage() } ?: emptyList()
                Result.success(messages)
            } else {
                Result.failure(Exception("HTTP ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendMessage(text: String): Result<String> {
        return try {
            val response = api.sendChatMessage(SendChatRequest(text))
            val reply = response.body()?.reply
            if (response.isSuccessful && reply != null) {
                Result.success(reply)
            } else {
                Result.failure(Exception("HTTP ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun clearHistory(): Result<Unit> {
        return try {
            api.clearChat()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun ChatMessageDto.toUiMessage(): ChatUiMessage {
        val mappedRole = if (role == "user") ChatRole.USER else ChatRole.AI
        return ChatUiMessage(role = mappedRole, content = content)
    }
}