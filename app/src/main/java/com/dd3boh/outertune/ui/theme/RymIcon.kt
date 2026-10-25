package com.dd3boh.outertune.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier

/** Monochrome adaptation of RYM's circular wave mark; Icon supplies the active theme tint. */
val RymIcon: ImageVector = ImageVector.Builder("RYM", 24.dp, 24.dp, 24f, 24f).apply {
    path(fill = SolidColor(Color.White), pathFillType = PathFillType.EvenOdd) {
        moveTo(12f, 1f); arcTo(11f, 11f, 0f, true, true, 12f, 23f); arcTo(11f, 11f, 0f, true, true, 12f, 1f); close()
        moveTo(2f, 9f); curveTo(6f, 4f, 8f, 6f, 12f, 9f); curveTo(16f, 12f, 19f, 10f, 22f, 8f)
        lineTo(22f, 11f); curveTo(18f, 14f, 15f, 14f, 11f, 12f); curveTo(7f, 9f, 5f, 8f, 2f, 12f); close()
        moveTo(2f, 17f); curveTo(7f, 12f, 10f, 14f, 14f, 16f); curveTo(18f, 18f, 20f, 16f, 22f, 14f)
        lineTo(22f, 17f); curveTo(18f, 21f, 15f, 20f, 11f, 18f); curveTo(7f, 16f, 5f, 17f, 3f, 19f); close()
    }
}.build()

/** Original supplied mark, recoloured at render time; neutral greys remain neutral. */
@androidx.compose.runtime.Composable
fun RymBrandIcon() {
    val accent = androidx.compose.material3.MaterialTheme.colorScheme.primary
    val matrix = androidx.compose.ui.graphics.ColorMatrix(floatArrayOf(
        1f - accent.red, 0f, accent.red, 0f, 0f,
        1f - accent.green, 0f, accent.green, 0f, 0f,
        1f - accent.blue, 0f, accent.blue, 0f, 0f,
        0f, 0f, 0f, 1f, 0f,
    ))
    androidx.compose.foundation.Image(
        painter = androidx.compose.ui.res.painterResource(com.dd3boh.outertune.R.drawable.rym_mark),
        contentDescription = null,
        colorFilter = androidx.compose.ui.graphics.ColorFilter.colorMatrix(matrix),
        modifier = Modifier.size(24.dp).clip(CircleShape)
    )
}

