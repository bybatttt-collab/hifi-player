package com.dd3boh.outertune.ui.theme

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.Offset

fun Modifier.hifiPanel(header: Boolean = false): Modifier = drawWithCache {
    val finish = Brush.verticalGradient(listOf(Color(0xFF292826), Color(0xFF161616), Color(0xFF222120)))
    onDrawBehind {
        drawRect(finish)
        drawRect(Color(0xFF75634A).copy(alpha = 0.4f), style = Stroke(1f))
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
