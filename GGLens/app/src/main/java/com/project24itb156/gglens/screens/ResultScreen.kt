package com.project24itb156.gglens.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.project24itb156.gglens.components.LabelResultCard
import com.project24itb156.gglens.components.TextResultCard
import com.project24itb156.gglens.model.LensMode
import com.project24itb156.gglens.model.LensResult
import com.project24itb156.gglens.model.LensUiState
import com.project24itb156.gglens.model.SearchResult
import com.project24itb156.gglens.viewmodel.LensViewModel
import java.net.URLEncoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    viewModel: LensViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kết quả nhận diện") },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.reset()
                        onBack()
                    }) {
                        Icon(Icons.Default.ArrowBack, "Quay lại")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(padding)) {
            when (val state = uiState) {
                is LensUiState.Loading -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Đang tìm kiếm...",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                is LensUiState.Success -> {
                    ResultContent(
                        result = state.result,
                        onOpenUrl = { url ->
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        },
                        onWebSearch = { query ->
                            val encoded = URLEncoder.encode(query, "UTF-8")
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://www.google.com/search?q=$encoded")
                            )
                            context.startActivity(intent)
                        },
                        onShoppingSearch = { query ->
                            val encoded = URLEncoder.encode(query, "UTF-8")
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://www.google.com/search?q=$encoded&tbm=shop")
                            )
                            context.startActivity(intent)
                        },
                        onCopyText = { text ->
                            clipboard.setText(AnnotatedString(text))
                        }
                    )
                }

                is LensUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("😕", style = MaterialTheme.typography.displayMedium)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = {
                            viewModel.reset()
                            onBack()
                        }) {
                            Text("Thử lại")
                        }
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
private fun ResultContent(
    result: LensResult,
    onOpenUrl: (String) -> Unit,
    onWebSearch: (String) -> Unit,
    onShoppingSearch: (String) -> Unit,
    onCopyText: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        result.originalBitmap?.let { bitmap ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Ảnh đã chụp",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Column(modifier = Modifier.padding(16.dp)) {
            if (result.mode == LensMode.SEARCH || result.mode == LensMode.SHOPPING) {
                if (result.searchResults.isNotEmpty()) {
                    Text(
                        text = "Kết quả tìm kiếm",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    result.searchResults.forEach { searchResult ->
                        SearchItemCard(searchResult, onOpenUrl)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                } else {
                    val query = result.searchQuery
                    if (query.isNotEmpty()) {
                        Text(
                            text = "Nhận diện: $query",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { onWebSearch(query) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Tìm trên Web")
                            }
                            Button(
                                onClick = { onShoppingSearch(query) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                            ) {
                                Text("Mua sắm")
                            }
                        }
                    }
                }

                if (result.detectedLabels.isNotEmpty() && result.searchResults.isEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    LabelResultCard(result = result)
                }
            }

            if (result.mode == LensMode.TRANSLATE) {
                if (result.extractedText.isNotEmpty()) {
                    TextResultCard(text = result.extractedText)
                    Spacer(modifier = Modifier.height(16.dp))
                }
                if (result.translatedText.isNotEmpty()) {
                    TranslatedTextCard(text = result.translatedText)
                }
            }

            if (result.mode == LensMode.TEXT && result.extractedText.isNotEmpty()) {
                TextResultCard(text = result.extractedText)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun SearchItemCard(result: SearchResult, onClick: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(result.link) },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = result.title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = result.link,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (result.snippet.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = result.snippet,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun TranslatedTextCard(text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}
