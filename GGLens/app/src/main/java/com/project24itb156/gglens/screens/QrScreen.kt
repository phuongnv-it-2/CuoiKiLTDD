package com.project24itb156.gglens.screens

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.project24itb156.gglens.components.ScanOverlay
import com.project24itb156.gglens.model.QrUiState
import com.project24itb156.gglens.viewmodel.QrViewModel
import java.util.concurrent.Executors

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun QrScreen(
    viewModel: QrViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraPermission = rememberPermissionState(android.Manifest.permission.CAMERA)
    val uiState by viewModel.uiState.collectAsState()

    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var torchEnabled by remember { mutableStateOf(false) }
    var camera by remember { mutableStateOf<Camera?>(null) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val mainExecutor = remember { ContextCompat.getMainExecutor(context) }

    DisposableEffect(Unit) {
        onDispose { cameraExecutor.shutdown() }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            context.contentResolver.openInputStream(it)?.use { stream ->
                val bitmap = BitmapFactory.decodeStream(stream)
                bitmap?.let { bmp -> viewModel.scanQr(bmp) }
            }
        }
    }

    LaunchedEffect(Unit) {
        if (!cameraPermission.status.isGranted) {
            cameraPermission.launchPermissionRequest()
        }
    }

    // Xử lý sau khi quét thành công — mở URL
    LaunchedEffect(uiState) {
        if (uiState is QrUiState.Found) {
            val result = (uiState as QrUiState.Found).result
            if (result.isUrl) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(result.rawValue))
                context.startActivity(intent)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (cameraPermission.status.isGranted) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx -> PreviewView(ctx) },
                update = { previewView ->
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }
                        val imgCapture = ImageCapture.Builder()
                            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                            .build()
                        imageCapture = imgCapture

                        try {
                            cameraProvider.unbindAll()
                            camera = cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                CameraSelector.DEFAULT_BACK_CAMERA,
                                preview,
                                imgCapture
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }, mainExecutor)
                }
            )
        }

        // Scan overlay
        ScanOverlay(
            modifier = Modifier.fillMaxSize(),
            isScanning = uiState is QrUiState.Scanning
        )

        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, start = 16.dp, end = 16.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.4f))
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại", tint = Color.White)
            }

            Text(
                text = "Quét mã QR",
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp
            )

            IconButton(
                onClick = {
                    torchEnabled = !torchEnabled
                    camera?.cameraControl?.enableTorch(torchEnabled)
                },
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.4f))
            ) {
                Icon(
                    if (torchEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                    contentDescription = "Đèn flash",
                    tint = if (torchEnabled) Color.Yellow else Color.White
                )
            }
        }

        // Hướng dẫn ở giữa (dưới khung scan)
        Text(
            text = "Đưa mã QR vào khung để quét",
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(top = 240.dp)
        )

        // Bottom buttons + kết quả
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(Color.Black.copy(alpha = 0.6f))
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Hiển thị kết quả
            when (val state = uiState) {
                is QrUiState.Scanning -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Text("Đang xử lý...", color = Color.White, fontSize = 14.sp)
                    }
                }
                is QrUiState.Found -> {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = if (state.result.isUrl) "🔗 Đường dẫn URL" else "📄 Nội dung văn bản",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = state.result.rawValue,
                                fontSize = 13.sp,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (state.result.isUrl) {
                            Button(onClick = {
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse(state.result.rawValue)
                                )
                                context.startActivity(intent)
                            }) {
                                Text("Mở trình duyệt")
                            }
                        }
                        OutlinedButton(
                            onClick = { viewModel.reset() },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.White
                            )
                        ) {
                            Text("Quét lại")
                        }
                    }
                }
                is QrUiState.Error -> {
                    Text(
                        text = "⚠ ${state.message}",
                        color = MaterialTheme.colorScheme.errorContainer,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                    OutlinedButton(
                        onClick = { viewModel.reset() },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Text("Thử lại")
                    }
                }
                else -> {
                    // Nút chụp và chọn ảnh
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { galleryLauncher.launch("image/*") },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.White
                            )
                        ) {
                            Text("Chọn ảnh QR")
                        }
                        Button(
                            onClick = {
                                captureQrPhoto(imageCapture, cameraExecutor) { bitmap ->
                                    mainExecutor.execute {
                                        viewModel.scanQr(bitmap)
                                    }
                                }
                            },
                            modifier = Modifier.height(44.dp)
                        ) {
                            Text("Chụp & Quét")
                        }
                    }
                }
            }
        }
    }
}

private fun captureQrPhoto(
    imageCapture: ImageCapture?,
    executor: java.util.concurrent.Executor,
    onCaptured: (Bitmap) -> Unit
) {
    imageCapture ?: return
    imageCapture.takePicture(executor, object : ImageCapture.OnImageCapturedCallback() {
        override fun onCaptureSuccess(image: ImageProxy) {
            val rotationDegrees = image.imageInfo.rotationDegrees
            val bitmap = image.toBitmap()
            image.close()
            val correctedBitmap = if (rotationDegrees != 0) {
                val matrix = android.graphics.Matrix().apply {
                    postRotate(rotationDegrees.toFloat())
                }
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            } else bitmap
            onCaptured(correctedBitmap)
        }

        override fun onError(exception: ImageCaptureException) {
            exception.printStackTrace()
        }
    })
}