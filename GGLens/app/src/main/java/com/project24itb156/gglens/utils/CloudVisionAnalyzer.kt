package com.project24itb156.gglens.utils

import android.util.Base64
import android.graphics.Bitmap
import android.util.Log
import com.project24itb156.gglens.BuildConfig
import com.project24itb156.gglens.api.*
import com.project24itb156.gglens.model.DetectedLabel
import com.project24itb156.gglens.model.LensMode
import com.project24itb156.gglens.model.LensResult
import java.io.ByteArrayOutputStream

class CloudVisionAnalyzer {

    private val visionApi = RetrofitClient.visionApi

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    suspend fun analyze(bitmap: Bitmap, mode: LensMode): LensResult {
        return try {
            val base64Image = bitmapToBase64(bitmap)

            val request = VisionRequest(
                requests = listOf(
                    AnnotateImageRequest(
                        image = VisionImage(content = base64Image),
                        features = listOf(
                            Feature(type = "LABEL_DETECTION"),
                            Feature(type = "OBJECT_LOCALIZATION"),
                            Feature(type = "TEXT_DETECTION")
                        )
                    )
                )
            )

            Log.d("CloudVision", "Sending request to Google Cloud...")
            val response = visionApi.annotateImage(BuildConfig.CLOUD_VISION_API_KEY, request)

            if (response.isSuccessful) {
                val visionResponse = response.body()?.responses?.firstOrNull()

                if (visionResponse == null) {
                    return LensResult(originalBitmap = bitmap, mode = mode, error = "Empty response", source = "Cloud Error")
                }

                if (visionResponse.error != null) {
                    return LensResult(originalBitmap = bitmap, mode = mode, error = visionResponse.error.message, source = "Cloud Error Detail")
                }

                val detectedLabels = mutableListOf<DetectedLabel>()
                visionResponse.labelAnnotations?.forEach {
                    detectedLabels.add(DetectedLabel(it.description, it.score))
                }

                visionResponse.localizedObjectAnnotations?.forEach { obj ->
                    if (detectedLabels.none { it.text.equals(obj.name, ignoreCase = true) }) {
                        detectedLabels.add(0, DetectedLabel(obj.name, obj.score))
                    }
                }

                val extractedText = visionResponse.fullTextAnnotation?.text ?: ""

                Log.d("CloudVision", "Success! Text found: ${extractedText.take(20)}...")

                LensResult(
                    originalBitmap = bitmap,
                    detectedLabels = detectedLabels,
                    extractedText = extractedText,
                    mode = mode,
                    source = "Google Cloud Vision"
                )
            } else {
                LensResult(originalBitmap = bitmap, mode = mode, error = "HTTP ${response.code()}", source = "Cloud HTTP Error")
            }
        } catch (e: Exception) {
            Log.e("CloudVision", "Error: ${e.message}")
            LensResult(originalBitmap = bitmap, mode = mode, error = e.message, source = "Cloud Vision Exception")
        }
    }
}