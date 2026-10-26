package com.dd3boh.outertune.ui.theme

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.Offset
import androidx.compose.runtime.Composable
import androidx.compose.material3.MaterialTheme

@Composable
fun Modifier.hifiPanel(header: Boolean = false): Modifier {
    val night = MaterialTheme.colorScheme.background == Color.Black
    val accent = MaterialTheme.colorScheme.primary
    return drawWithCache {
    val finish = Brush.verticalGradient(if (night) {
        if (header) listOf(Color(0xFF101319), Color.Black, Color(0xFF090B0E)) else listOf(Color.Black, Color.Black)
    } else listOf(Color(0xFF292826), Color(0xFF161616), Color(0xFF222120)))
    onDrawBehind {
        drawRect(finish)
        drawRect(accent.copy(alpha = if (night) 0.16f else 0.25f), style = Stroke(1f))
        if (header) {
            var y = 2f
            while (y < size.height) { drawLine(Color.White.copy(alpha = 0.018f), Offset(0f, y), Offset(size.width, y)); y += 4f }
            listOf(Offset(10f, 10f), Offset(size.width - 10f, 10f)).forEach { p ->
                drawCircle(Color(0xFF101010), 4f, p)
                drawCircle(Color(0xFF786B56), 4f, p, style = Stroke(1f))
            }
        }
    }
}
}
