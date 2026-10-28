package com.dd3boh.outertune.ui.player

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.datastore.preferences.core.stringPreferencesKey

/** Separate from the obsolete cover-background preference, so old installs default to CURRENT. */
enum class HifiPlayerFinish { CURRENT, BLACK_METAL, BLACK_SATIN, SILVER }

val HifiPlayerFinishKey = stringPreferencesKey("hifiPlayerFinish")
val LocalHifiPlayerFinish = staticCompositionLocalOf { HifiPlayerFinish.CURRENT }
/** Width of the fixed artwork window; the moving pages must not add their own bezel or gutters. */
val LocalHifiArtworkViewportWidth = staticCompositionLocalOf<Dp?> { null }

/** The visible queue button replaces the old two-peek strip in the hardware portrait player. */
internal fun PlayerQueueLayout.forHifiPortrait(bottomInset: Dp, enabled: Boolean): PlayerQueueLayout =
    if (!enabled) this else copy(
        // Lift the compact block another 32dp above the previous installed revision.
        reserve = bottomInset + 72.dp,
        sheetDismissed = 0.dp,
        sheetCollapsed = 0.dp,
        showHandle = false,
        queueButton = true,
        artworkPadding = 8.dp,
        outputRoom = 0.dp,
    )

/** Silver is a light faceplate, not a silver border around the dark player. */
fun hifiFinishColors(base: ColorScheme, finish: HifiPlayerFinish): ColorScheme {
    if (finish != HifiPlayerFinish.SILVER) return base
    val surface = Color(0xFFBFC3C6)
    val ink = Color(0xFF171B1F)
    var accent = base.primary
    // Preserve the chosen hue, but darken light accents enough for the silver material.
    val darkestPanel = Color(0xFF9DA3A8)
    repeat(20) {
        val light = maxOf(accent.luminance(), darkestPanel.luminance())
        val dark = minOf(accent.luminance(), darkestPanel.luminance())
        if ((light + 0.05f) / (dark + 0.05f) < 4.5f) accent = lerp(accent, Color.Black, 0.12f)
    }
    return base.copy(
        primary = accent, onPrimary = Color.White,
        secondary = accent, onSecondary = Color.White,
        primaryContainer = Color(0xFF8C9297), onPrimaryContainer = ink,
        secondaryContainer = Color(0xFFD4D7D9), onSecondaryContainer = ink,
        surface = surface, onSurface = ink,
        background = surface, onBackground = ink,
        surfaceVariant = Color(0xFFADB3B8), onSurfaceVariant = Color(0xFF30363B),
        surfaceContainerLowest = surface, surfaceContainerLow = surface,
        surfaceContainer = surface, surfaceContainerHigh = Color(0xFFCBCFD2),
        surfaceContainerHighest = Color(0xFFD6D9DB),
        outline = Color(0xFF535B62), outlineVariant = Color(0xFF737B82),
    )
}

/** Static native drawing: no image downloads, shaders, timers or animated texture. */
@Composable
fun HifiPlayerFaceplate(finish: HifiPlayerFinish, modifier: Modifier = Modifier) {
    val insets = WindowInsets.systemBars.asPaddingValues()
    val topInset = insets.calculateTopPadding()
    val bottomInset = insets.calculateBottomPadding()
    Box(modifier.fillMaxSize().drawWithCache {
        val silver = finish == HifiPlayerFinish.SILVER
        val satin = finish == HifiPlayerFinish.BLACK_SATIN
        val metal = if (silver) listOf(Color(0xFFA6ACB1), Color(0xFFD1D4D7), Color(0xFF9DA3A8), Color(0xFFC6C9CC))
            else listOf(Color(0xFF303235), Color(0xFF17191B), Color(0xFF25272A), Color(0xFF151719))
        val panelBrush = Brush.horizontalGradient(if (satin) listOf(Color(0xFF202326), Color(0xFF191C1E)) else metal)
        val rail = 5.dp.toPx()
        val panelTop = topInset.toPx() + 8.dp.toPx()
        val panelBottom = (size.height - bottomInset.toPx() - 8.dp.toPx()).coerceAtLeast(panelTop)
        val radius = 8.dp.toPx()
        val grainStep = if (satin) 3.dp.toPx() else 0.8.dp.toPx()
        val grainCount = (size.height / grainStep).toInt().coerceAtMost(1200)
        onDrawBehind {
            if (finish != HifiPlayerFinish.CURRENT) {
                drawRect(panelBrush)
                repeat(grainCount) { index ->
                    val y = index * grainStep
                    val alpha = if (satin) 0.007f else if (index % 3 == 0) 0.045f else 0.018f
                    drawLine(if (index % 2 == 0) Color.White.copy(alpha) else Color.Black.copy(alpha),
                        Offset(0f, y), Offset(size.width, y), 0.5.dp.toPx())
                }
                // Only an outline surrounds the faceplate: no opaque black panel over the material.
                val panelSize = androidx.compose.ui.geometry.Size((size.width - rail * 2).coerceAtLeast(0f), panelBottom - panelTop)
                drawRoundRect(Color.Black.copy(alpha = 0.42f), Offset(rail, panelTop), panelSize,
                    CornerRadius(radius), style = Stroke(1.dp.toPx()))
                drawLine(Color.White.copy(alpha = if (silver) 0.42f else 0.10f),
                    Offset(rail + radius, panelTop + 1.dp.toPx()),
                    Offset(size.width - rail - radius, panelTop + 1.dp.toPx()), 1.dp.toPx())
                val screwRadius = 2.5.dp.toPx()
                for (x in listOf(10.dp.toPx(), size.width - 10.dp.toPx())) {
                    for (y in listOf(panelTop + 6.dp.toPx(), panelBottom - 6.dp.toPx())) {
                        drawCircle(Color.Black.copy(alpha = 0.7f), screwRadius + 1.dp.toPx(), Offset(x, y + 0.5.dp.toPx()))
                        drawCircle(if (silver) Color(0xFF8B9095) else Color(0xFF44484B), screwRadius, Offset(x, y))
                        drawLine(Color.Black.copy(alpha = 0.8f), Offset(x - screwRadius * 0.55f, y + screwRadius * 0.55f),
                            Offset(x + screwRadius * 0.55f, y - screwRadius * 0.55f), 1.dp.toPx())
                    }
                }
            }
        }
    })
}

/** A light bevel on actual controls only; it never adds another touch target or label. */
fun Modifier.hifiHardwareButton(finish: HifiPlayerFinish): Modifier =
    if (finish == HifiPlayerFinish.CURRENT) this else drawWithCache {
        val bevel = Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.13f), Color.Transparent,
            Color.Black.copy(alpha = 0.25f)))
        onDrawWithContent {
            drawRect(bevel)
            drawContent()
            drawLine(Color.White.copy(alpha = 0.14f), Offset(6.dp.toPx(), 1.dp.toPx()),
                Offset(size.width - 6.dp.toPx(), 1.dp.toPx()), 1.dp.toPx())
            drawLine(Color.Black.copy(alpha = 0.45f), Offset(6.dp.toPx(), size.height - 1.dp.toPx()),
                Offset(size.width - 6.dp.toPx(), size.height - 1.dp.toPx()), 1.dp.toPx())
        }
    }

@Composable
fun Modifier.hifiTransportWell(finish: HifiPlayerFinish): Modifier =
    if (finish == HifiPlayerFinish.CURRENT) this else padding(horizontal = 3.dp)
        .height(56.dp)
        .clip(RoundedCornerShape(4.dp))
        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(4.dp))
        .background(MaterialTheme.colorScheme.surface)
        .hifiHardwareButton(finish)

/** A bezel around the artwork only, leaving the rest of the faceplate exposed. */
fun Modifier.hifiArtworkBezel(finish: HifiPlayerFinish): Modifier =
    if (finish == HifiPlayerFinish.CURRENT) this else
        border(3.dp, Color(0xFF101214), RoundedCornerShape(6.dp))
            .border(1.dp, Color(0xFF55595C), RoundedCornerShape(6.dp))
