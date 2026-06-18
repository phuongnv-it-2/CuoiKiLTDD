package com.project24itb156.gglens.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun ScanOverlay(
    modifier: Modifier = Modifier,
    isScanning: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "scan")
    val scanLineY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scanLine"
    )

    val cornerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cornerAlpha"
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val boxSize = size.width * 0.65f
            val boxLeft = (size.width - boxSize) / 2f
            val boxTop = (size.height - boxSize) / 2f
            val cornerLen = boxSize * 0.15f
            val strokeW = 3.dp.toPx()
            val cornerColor = Color.White.copy(alpha = cornerAlpha)

            // Mờ 4 góc ngoài box
            drawRect(
                color = Color.Black.copy(alpha = 0.45f),
                topLeft = Offset(0f, 0f),
                size = Size(size.width, boxTop)
            )
            drawRect(
                color = Color.Black.copy(alpha = 0.45f),
                topLeft = Offset(0f, boxTop + boxSize),
                size = Size(size.width, size.height - boxTop - boxSize)
            )
            drawRect(
                color = Color.Black.copy(alpha = 0.45f),
                topLeft = Offset(0f, boxTop),
                size = Size(boxLeft, boxSize)
            )
            drawRect(
                color = Color.Black.copy(alpha = 0.45f),
                topLeft = Offset(boxLeft + boxSize, boxTop),
                size = Size(size.width - boxLeft - boxSize, boxSize)
            )

            // Góc trên trái
            drawLine(cornerColor, Offset(boxLeft, boxTop + cornerLen), Offset(boxLeft, boxTop), strokeW, StrokeCap.Round)
            drawLine(cornerColor, Offset(boxLeft, boxTop), Offset(boxLeft + cornerLen, boxTop), strokeW, StrokeCap.Round)

            // Góc trên phải
            drawLine(cornerColor, Offset(boxLeft + boxSize - cornerLen, boxTop), Offset(boxLeft + boxSize, boxTop), strokeW, StrokeCap.Round)
            drawLine(cornerColor, Offset(boxLeft + boxSize, boxTop), Offset(boxLeft + boxSize, boxTop + cornerLen), strokeW, StrokeCap.Round)

            // Góc dưới trái
            drawLine(cornerColor, Offset(boxLeft, boxTop + boxSize - cornerLen), Offset(boxLeft, boxTop + boxSize), strokeW, StrokeCap.Round)
            drawLine(cornerColor, Offset(boxLeft, boxTop + boxSize), Offset(boxLeft + cornerLen, boxTop + boxSize), strokeW, StrokeCap.Round)

            // Góc dưới phải
            drawLine(cornerColor, Offset(boxLeft + boxSize - cornerLen, boxTop + boxSize), Offset(boxLeft + boxSize, boxTop + boxSize), strokeW, StrokeCap.Round)
            drawLine(cornerColor, Offset(boxLeft + boxSize, boxTop + boxSize), Offset(boxLeft + boxSize, boxTop + boxSize - cornerLen), strokeW, StrokeCap.Round)

            // Đường scan ngang
            if (isScanning) {
                val lineY = boxTop + boxSize * scanLineY
                drawLine(
                    color = Color.White.copy(alpha = 0.7f),
                    start = Offset(boxLeft + 8.dp.toPx(), lineY),
                    end = Offset(boxLeft + boxSize - 8.dp.toPx(), lineY),
                    strokeWidth = 1.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }
    }
}
