package com.project24itb156.gglens.utils

import android.graphics.Bitmap
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.project24itb156.gglens.model.QrResult
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class QrAnalyzer {

    private val scanner = BarcodeScanning.getClient()

    suspend fun analyze(bitmap: Bitmap): QrResult? {
        val image = InputImage.fromBitmap(bitmap, 0)
        val barcodes = suspendCancellableCoroutine { cont ->
            scanner.process(image)
                .addOnSuccessListener { cont.resume(it) }
                .addOnFailureListener { cont.resumeWithException(it) }
        }

        val barcode = barcodes.firstOrNull() ?: return null
        val raw = barcode.rawValue ?: return null

        val isUrl = barcode.valueType == Barcode.TYPE_URL
                || raw.startsWith("http://")
                || raw.startsWith("https://")

        return QrResult(rawValue = raw, isUrl = isUrl)
    }

    fun close() = scanner.close()
}