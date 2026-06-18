package com.project24itb156.gglens.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.project24itb156.gglens.api.AiResultData
import com.project24itb156.gglens.api.RetrofitClient
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiResultDetailScreen(
    aiResultId: String,
    onBack: () -> Unit
) {
    var resultData by remember { mutableStateOf<AiResultData?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(aiResultId) {
        scope.launch {
            try {
                val response = RetrofitClient.backendApi.getAiResult(aiResultId)
                if (response.isSuccessful) {
                    resultData = response.body()?.data
                } else {
                    error = "Không tìm thấy dữ liệu: ${response.code()}"
                }
            } catch (e: Exception) {
                error = e.message ?: "Lỗi kết nối"
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết phân tích") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (error != null) {
                Text(error!!, color = Color.Red, modifier = Modifier.align(Alignment.Center))
            } else {
                resultData?.let { data ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        DetailItem("ID Phiên", data.sessionId)
                        DetailItem("Chế độ", data.mode)
                        DetailItem("Nhãn chính", data.topLabel)
                        DetailItem("Độ tin cậy", "${(data.topConfidence * 100).toInt()}%")
                        DetailItem("Câu truy vấn", data.searchQuery)
                        
                        if (data.extractedText.isNotEmpty()) {
                            Divider(modifier = Modifier.padding(vertical = 8.dp))
                            Text("Văn bản trích xuất:", fontWeight = FontWeight.Bold)
                            Text(data.extractedText, modifier = Modifier.padding(top = 4.dp))
                        }

                        if (data.translatedText.isNotEmpty()) {
                            Divider(modifier = Modifier.padding(vertical = 8.dp))
                            Text("Văn bản dịch:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text(data.translatedText, modifier = Modifier.padding(top = 4.dp))
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Thời gian tạo: ${data.createdAt}", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun DetailItem(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        Text("$label: ", fontWeight = FontWeight.Bold, modifier = Modifier.width(120.dp))
        Text(value, modifier = Modifier.weight(1f))
    }
}
