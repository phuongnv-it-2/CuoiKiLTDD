package com.project24itb156.gglens.api

import retrofit2.Response
import retrofit2.http.*

// ─── Google Cloud Vision ──────────────────────────────────────────────────────

interface VisionApi {
    @POST("v1/images:annotate")
    suspend fun annotateImage(
        @Query("key") apiKey: String,
        @Body request: VisionRequest
    ): Response<VisionResponse>
}

data class VisionRequest(val requests: List<AnnotateImageRequest>)
data class AnnotateImageRequest(val image: VisionImage, val features: List<Feature>)
data class VisionImage(val content: String)
data class Feature(val type: String, val maxResults: Int = 10)
data class VisionResponse(val responses: List<AnnotateImageResponse>)
data class AnnotateImageResponse(
    val labelAnnotations: List<LabelAnnotation>?,
    val localizedObjectAnnotations: List<LocalizedObjectAnnotation>?,
    val fullTextAnnotation: FullTextAnnotation?,
    val error: VisionError?
)
data class FullTextAnnotation(val text: String)
data class VisionError(val code: Int, val message: String, val status: String)
data class LabelAnnotation(val description: String, val score: Float)
data class LocalizedObjectAnnotation(val name: String, val score: Float)

// ─── Google Custom Search ─────────────────────────────────────────────────────

interface GoogleSearchApi {
    @GET("customsearch/v1")
    suspend fun search(
        @Query("key") apiKey: String,
        @Query("cx") cx: String,
        @Query("q") query: String
    ): SearchResponse
}

data class SearchResponse(val items: List<SearchItem>?)
data class SearchItem(val title: String, val link: String, val snippet: String?)

// ─── GG Lens Node.js Backend ─────────────────────────────────────────────────

interface GGLensBackendApi {

    // --- AI Results (MongoDB) ---
    @POST("api/ai-results")
    suspend fun saveAiResult(@Body request: SaveAiResultRequest): Response<SaveAiResultResponse>

    @GET("api/ai-results/{id}")
    suspend fun getAiResult(@Path("id") id: String): Response<AiResultResponse>

    // --- Search History (MySQL) ---
    @POST("api/history")
    suspend fun saveHistory(@Body request: SaveHistoryRequest): Response<SaveHistoryResponse>

    @GET("api/history")
    suspend fun getHistory(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<HistoryListResponse>

    @DELETE("api/history/all")
    suspend fun clearHistory(): Response<BaseResponse>

    @DELETE("api/history/{id}")
    suspend fun deleteHistory(@Path("id") id: Int): Response<BaseResponse>


    @POST("api/translate")
    suspend fun translate(@Body request: TranslateRequest): TranslateResponse

    @GET("api/health")
    suspend fun health(): Response<HealthResponse>
}

// ─── Request DTOs ─────────────────────────────────────────────────────────────

data class SaveAiResultRequest(
    val sessionId: String,
    val mode: String,
    val source: String,
    val detectedLabels: List<LabelDto>,
    val topLabel: String,
    val topConfidence: Float,
    val searchQuery: String,
    val extractedText: String,
    val translatedText: String,
    val processingTimeMs: Long
)

data class LabelDto(val text: String, val confidence: Float)

data class SaveHistoryRequest(
    val sessionId: String,
    val query: String,
    val mode: String,
    val resultCount: Int,
    val aiResultId: String?
)
data class TranslateRequest(val text: String)

// ─── Response DTOs ────────────────────────────────────────────────────────────

data class BaseResponse(val success: Boolean, val message: String?)

data class SaveAiResultResponse(val success: Boolean, val id: String?, val message: String?)

data class AiResultResponse(val success: Boolean, val data: AiResultData?)

data class AiResultData(
    val id: String,
    val sessionId: String,
    val mode: String,
    val source: String,
    val topLabel: String,
    val topConfidence: Float,
    val searchQuery: String,
    val extractedText: String,
    val translatedText: String,
    val createdAt: String
)

data class SaveHistoryResponse(val success: Boolean, val id: Int?, val message: String?)

data class HistoryListResponse(
    val success: Boolean,
    val data: List<HistoryItemDto>?,
    val total: Int,
    val page: Int,
    val totalPages: Int
)

data class HistoryItemDto(
    val id: Int,
    val session_id: String,
    val query: String,
    val mode: String,
    val result_count: Int,
    val ai_result_id: String?,
    val created_at: String
)

data class HealthResponse(val status: String, val mongo: String, val mysql: String)

data class TranslateResponse(val translatedText: String)