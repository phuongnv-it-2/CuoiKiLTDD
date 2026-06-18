package com.project24itb156.gglens.model

import android.graphics.Bitmap

// ─── Enums ───────────────────────────────────────────────────────────────────

enum class LensMode { SEARCH, TRANSLATE, TEXT, SHOPPING, QR }

// ─── Domain models ───────────────────────────────────────────────────────────

data class DetectedLabel(
    val text: String,
    val confidence: Float
)

data class SearchResult(
    val title: String,
    val link: String,
    val snippet: String = ""
)

data class HistoryItem(
    val id: Int,
    val sessionId: String,
    val query: String,
    val mode: String,
    val resultCount: Int,
    val aiResultId: String?,
    val createdAt: String
)

data class LensResult(
    val originalBitmap: Bitmap? = null,
    val detectedLabels: List<DetectedLabel> = emptyList(),
    val searchResults: List<SearchResult> = emptyList(),
    val extractedText: String = "",
    val translatedText: String = "",
    val mode: LensMode = LensMode.SEARCH,
    val error: String? = null,
    val geminiSearchQuery: String = "",
    val source: String = "",
    val sessionId: String = "",
    val aiResultId: String? = null   // MongoDB _id sau khi save
) {
    val topLabel: String get() = detectedLabels.firstOrNull()?.text ?: "Không nhận diện được"
    val topConfidence: Float get() = detectedLabels.firstOrNull()?.confidence ?: 0f
    val searchQuery: String get() = geminiSearchQuery.ifEmpty {
        detectedLabels.take(2).joinToString(" ") { it.text }
    }
}
data class QrResult(
    val rawValue: String,
    val isUrl: Boolean,
    val sessionId: String = "",
    val aiResultId: String? = null
)

sealed class QrUiState {
    object Idle : QrUiState()
    object Scanning : QrUiState()
    data class Found(val result: QrResult) : QrUiState()
    data class Error(val message: String) : QrUiState()
}

// ─── UI state ─────────────────────────────────────────────────────────────────

sealed class LensUiState {
    object Idle : LensUiState()
    object Loading : LensUiState()
    data class Success(val result: LensResult) : LensUiState()
    data class Error(val message: String) : LensUiState()
}

sealed class HistoryUiState {
    object Idle : HistoryUiState()
    object Loading : HistoryUiState()
    data class Success(val items: List<HistoryItem>) : HistoryUiState()
    data class Error(val message: String) : HistoryUiState()
}
