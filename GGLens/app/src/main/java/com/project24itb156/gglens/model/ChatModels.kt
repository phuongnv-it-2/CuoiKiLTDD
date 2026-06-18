package com.project24itb156.gglens.model

enum class ChatRole { USER, AI }

data class ChatUiMessage(
    val role: ChatRole,
    val content: String
)