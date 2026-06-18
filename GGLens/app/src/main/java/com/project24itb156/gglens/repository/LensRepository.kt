package com.project24itb156.gglens.repository

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.project24itb156.gglens.BuildConfig
import com.project24itb156.gglens.api.*
import com.project24itb156.gglens.model.*
import com.project24itb156.gglens.utils.ImageAnalyzer
import java.util.UUID

class LensRepository(context: Context) {

    private val imageAnalyzer = ImageAnalyzer(context)
    private val backendApi: GGLensBackendApi = RetrofitClient.backendApi
    private val googleSearchApi: GoogleSearchApi = RetrofitClient.googleSearchApi

    suspend fun analyzeImage(bitmap: Bitmap, mode: LensMode): Result<LensResult> {
        return try {
            val sessionId = UUID.randomUUID().toString()
            val startTime = System.currentTimeMillis()

            // 1. Phân tích ảnh
            val result = imageAnalyzer.analyze(bitmap, mode).copy(sessionId = sessionId)
            val processingTime = System.currentTimeMillis() - startTime

            // 2. Lưu kết quả AI (non-blocking)
            val aiResultId = saveAiResultToBackend(result, processingTime)

            Result.success(result.copy(aiResultId = aiResultId))
        } catch (e: Exception) {
            Log.e("LensRepository", "analyzeImage error: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun search(query: String): Result<List<SearchResult>> {
        return try {
            val response = googleSearchApi.search(
                BuildConfig.CLOUD_VISION_API_KEY,
                BuildConfig.SEARCH_CX,
                query
            )
            val results = response.items?.map { item ->
                SearchResult(item.title, item.link, item.snippet ?: "")
            } ?: emptyList<SearchResult>()
            Result.success(results)
        } catch (e: Exception) {
            Log.e("LensRepository", "search exception: ${e.message}")
            Result.failure(e)
        }
    }

    private suspend fun saveAiResultToBackend(result: LensResult, processingTime: Long): String? {
        return try {
            val request = SaveAiResultRequest(
                sessionId = result.sessionId,
                mode = result.mode.name,
                source = result.source,
                detectedLabels = result.detectedLabels.map { LabelDto(it.text, it.confidence) },
                topLabel = result.topLabel,
                topConfidence = result.topConfidence,
                searchQuery = result.searchQuery,
                extractedText = result.extractedText,
                translatedText = result.translatedText,
                processingTimeMs = processingTime
            )
            val response = backendApi.saveAiResult(request)
            if (response.isSuccessful) response.body()?.id else null
        } catch (e: Exception) {
            Log.w("LensRepository", "saveAiResult failed (non-critical): ${e.message}")
            null
        }
    }

    suspend fun saveHistory(
        sessionId: String,
        query: String,
        mode: LensMode,
        resultCount: Int,
        aiResultId: String?
    ) {
        try {
            val request = SaveHistoryRequest(
                sessionId = sessionId,
                query = query,
                mode = mode.name,
                resultCount = resultCount,
                aiResultId = aiResultId
            )
            backendApi.saveHistory(request)
        } catch (e: Exception) {
            Log.w("LensRepository", "saveHistory failed (non-critical): ${e.message}")
        }
    }

    suspend fun getHistory(page: Int = 1): Result<List<HistoryItem>> {
        return try {
            val response = backendApi.getHistory(page)
            if (response.isSuccessful) {
                val items = response.body()?.data?.map {
                    HistoryItem(
                        id = it.id,
                        sessionId = it.session_id,
                        query = it.query,
                        mode = it.mode,
                        resultCount = it.result_count,
                        aiResultId = it.ai_result_id,
                        createdAt = it.created_at
                    )
                } ?: emptyList()
                Result.success(items)
            } else {
                Result.failure(Exception("HTTP ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("LensRepository", "getHistory error: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun clearAllHistory(): Result<Unit> {
        return try {
            backendApi.clearHistory()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun deleteHistoryItem(id: Int): Result<Unit> {
        return try {
            val response = backendApi.deleteHistory(id)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Delete failed: ${response.code()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun translate(text: String): Result<String> {
        return try {
            val response = backendApi.translate(TranslateRequest(text))
            Result.success(response.translatedText)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun close() {
        imageAnalyzer.close()
    }
}