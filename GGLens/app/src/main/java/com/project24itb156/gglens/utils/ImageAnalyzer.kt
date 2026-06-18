package com.project24itb156.gglens.utils

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.project24itb156.gglens.model.DetectedLabel
import com.project24itb156.gglens.model.LensMode
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

import com.project24itb156.gglens.api.TranslateRequest
import com.project24itb156.gglens.api.RetrofitClient
import com.project24itb156.gglens.model.LensResult

class ImageAnalyzer(private val context: Context) {

    private val cloudVisionAnalyzer = CloudVisionAnalyzer()
    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
    private val barcodeScanner = BarcodeScanning.getClient()

    private fun preprocessBitmap(bitmap: Bitmap): Bitmap {
        val maxSize = 1024
        val scale = minOf(maxSize.toFloat() / bitmap.width, maxSize.toFloat() / bitmap.height, 1f)
        return if (scale < 1f)
            Bitmap.createScaledBitmap(bitmap, (bitmap.width * scale).toInt(), (bitmap.height * scale).toInt(), true)
        else bitmap
    }

    suspend fun analyze(bitmap: Bitmap, mode: LensMode): LensResult {
        val processed = preprocessBitmap(bitmap)
        return when (mode) {
            LensMode.SEARCH, LensMode.SHOPPING -> {
                val cloudResult = cloudVisionAnalyzer.analyze(processed, mode)
                if (cloudResult.error != null || cloudResult.detectedLabels.isEmpty()) {
                    analyzeImageWithMLKit(processed, mode)
                } else {
                    cloudResult
                }
            }
            LensMode.TRANSLATE -> analyzeTranslateWithMlKit(processed)
            LensMode.TEXT -> analyzeText(processed, mode)
            LensMode.QR -> analyzeQr(processed)
        }
    }

    private suspend fun analyzeImageWithMLKit(bitmap: Bitmap, mode: LensMode): LensResult {
        return try {
            val inputImage = InputImage.fromBitmap(bitmap, 0)
            val labels = suspendCancellableCoroutine { cont ->
                labeler.process(inputImage)
                    .addOnSuccessListener { cont.resume(it) }
                    .addOnFailureListener { cont.resumeWithException(it) }
            }
            LensResult(
                originalBitmap = bitmap,
                detectedLabels = labels.map { DetectedLabel(it.text, it.confidence) },
                mode = mode,
                source = "ML Kit (Offline)"
            )
        } catch (e: Exception) {
            LensResult(originalBitmap = bitmap, mode = mode, error = e.message, source = "ML Kit Error")
        }
    }

    private suspend fun analyzeTranslateWithMlKit(
        bitmap: Bitmap
    ): LensResult {

        val extractedText = runMlKitOcr(bitmap)

        if (extractedText.isBlank()) {

            return LensResult(
                originalBitmap = bitmap,

                extractedText = "",

                translatedText = "Không tìm thấy chữ",

                mode = LensMode.TRANSLATE,

                source = "ML Kit OCR"
            )
        }

        return try {

            val response =
                RetrofitClient.backendApi.translate(
                    TranslateRequest(
                        extractedText
                    )
                )

            LensResult(

                originalBitmap = bitmap,

                extractedText = extractedText,

                translatedText =
                    response.translatedText,

                mode =
                    LensMode.TRANSLATE,

                source =
                    "ML Kit OCR + Backend"

            )

        } catch (e: Exception) {
            Log.e("ImageAnalyzer", "translate failed", e)
            LensResult(

                originalBitmap = bitmap,

                extractedText = extractedText,

                translatedText = "Lỗi dịch",

                mode = LensMode.TRANSLATE,

                source = "Backend Error",

                error = e.message

            )

        }
    }
    private suspend fun analyzeText(bitmap: Bitmap, mode: LensMode): LensResult {
        val text = runMlKitOcr(bitmap)
        return LensResult(originalBitmap = bitmap, extractedText = text, mode = mode, source = "ML Kit OCR")
    }

    private suspend fun analyzeQr(bitmap: Bitmap): LensResult {
        return try {
            val inputImage = InputImage.fromBitmap(bitmap, 0)
            val barcodes = suspendCancellableCoroutine { cont ->
                barcodeScanner.process(inputImage)
                    .addOnSuccessListener { cont.resume(it) }
                    .addOnFailureListener { cont.resumeWithException(it) }
            }
            val qrText = barcodes.firstOrNull()?.rawValue ?: ""
            LensResult(
                originalBitmap = bitmap,
                extractedText = qrText,
                mode = LensMode.QR,
                source = "ML Kit QR"
            )
        } catch (e: Exception) {
            LensResult(originalBitmap = bitmap, mode = LensMode.QR, error = e.message, source = "QR Error")
        }
    }


    private suspend fun runMlKitOcr(bitmap: Bitmap): String {
        val image = InputImage.fromBitmap(bitmap, 0)
        return suspendCancellableCoroutine { cont ->
            textRecognizer.process(image)
                .addOnSuccessListener { cont.resume(it.text) }
                .addOnFailureListener { cont.resumeWithException(it) }
        }
    }

    fun close() {
        textRecognizer.close()
        labeler.close()
        barcodeScanner.close()
    }

}
