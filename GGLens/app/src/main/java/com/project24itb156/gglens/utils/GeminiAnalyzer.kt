package com.project24itb156.gglens.utils

import android.graphics.Bitmap
import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import com.project24itb156.gglens.BuildConfig

class GeminiAnalyzer {

    // Đổi tên biến để tránh xung đột và thêm cấu hình JSON nếu cần
    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.0-flash",
        apiKey = BuildConfig.GEMINI_API_KEY,
        generationConfig = generationConfig {
            responseMimeType = "application/json"
        }
    )

    /** Dùng cho mode SEARCH và SHOPPING: nhận diện vật thể, trả JSON */
    suspend fun describe(bitmap: Bitmap, tfliteLabels: List<String>): String {
        return try {
            val labelsHint = if (tfliteLabels.isNotEmpty())
                "TFLite gợi ý đây có thể là: ${tfliteLabels.joinToString(", ")}."
            else ""

            val prompt = """
                $labelsHint
                Nhìn vào ảnh và cho biết:
                1. Đây là vật gì / sản phẩm gì? (tên tiếng Việt)
                2. Từ khóa tìm kiếm Google tốt nhất (tiếng Anh, ngắn gọn 2-4 từ)
                
                Trả lời ĐÚNG format JSON sau:
                {"name":"tên vật thể","searchQuery":"best search keyword"}
            """.trimIndent()

            val response = generativeModel.generateContent(
                content {
                    image(bitmap)
                    text(prompt)
                }
            )

            cleanJsonResponse(response.text)

        } catch (e: Exception) {
            Log.e("GeminiAnalyzer", "describe error: ${e.message}")
            ""
        }
    }

    /** 
     * Dùng để dịch văn bản sang tiếng Việt
     */
    suspend fun translateText(textToTranslate: String): String {
        if (textToTranslate.isBlank()) return ""
        return try {
            val prompt = """
                Dịch văn bản sau đây sang tiếng Việt một cách tự nhiên nhất.
                Văn bản: $textToTranslate
                
                Trả lời ĐÚNG format JSON:
                {"originalText":"$textToTranslate","translatedText":"nội dung đã dịch"}
            """.trimIndent()

            val response = generativeModel.generateContent(prompt)
            cleanJsonResponse(response.text)

        } catch (e: Exception) {
            Log.e("GeminiAnalyzer", "translateText error: ${e.message}")
            ""
        }
    }

    /** Fallback dịch trực tiếp từ ảnh */
    suspend fun translateImage(bitmap: Bitmap): String {
        return try {
            val prompt = """
                1. Đọc văn bản trong ảnh (OCR).
                2. Dịch văn bản đó sang tiếng Việt.
                
                Trả lời format JSON:
                {"originalText":"văn bản gốc","translatedText":"bản dịch"}
            """.trimIndent()

            val response = generativeModel.generateContent(
                content {
                    image(bitmap)
                    text(prompt)
                }
            )
            cleanJsonResponse(response.text)

        } catch (e: Exception) {
            Log.e("GeminiAnalyzer", "translateImage error: ${e.message}")
            ""
        }
    }

    private fun cleanJsonResponse(text: String?): String {
        return text?.trim()
            ?.replace("```json", "")
            ?.replace("```", "")
            ?.trim() ?: ""
    }
}
