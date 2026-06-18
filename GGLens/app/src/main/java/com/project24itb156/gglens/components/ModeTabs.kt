package com.project24itb156.gglens.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project24itb156.gglens.model.LensMode

@Composable
fun ModeTabs(
    selectedMode: LensMode,
    onModeSelected: (LensMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val modes = listOf(
        LensMode.SEARCH to "Tìm kiếm",
        LensMode.TRANSLATE to "Dịch",
        LensMode.TEXT to "Văn bản",
        LensMode.SHOPPING to "Mua sắm"
    )

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
    ) {
        modes.forEach { (mode, label) ->
            val isSelected = mode == selectedMode
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (isSelected) Color.White.copy(alpha = 0.2f)
                        else Color.Transparent
                    )
                    .clickable { onModeSelected(mode) }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.55f),
                    fontSize = 13.sp
                )
            }
        }
    }
}
