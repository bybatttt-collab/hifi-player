package com.dd3boh.outertune.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.stringPreferencesKey
import com.dd3boh.outertune.R
import com.dd3boh.outertune.utils.rememberPreference

val HifiAccentKey = stringPreferencesKey("hifiAccentHex")
val HifiAppearanceKey = stringPreferencesKey("hifiAppearance")
private val presets = linkedMapOf(
    "Ambra" to "E8B45C", "Verde" to "B7DE73", "Ghiaccio" to "83C9DF", "Viola" to "C2A1E8",
    "Corallo" to "FF9A85", "Rosa" to "EDA7CF", "Blu" to "88AEFF", "Acqua" to "72DCCB",
    "Lime" to "D7DE73", "Arancio" to "FFBC7A", "Lavanda" to "B6B4FF", "Argento" to "CFD3D9",
)
val HifiTitleFont = FontFamily(Font(R.font.dot_gothic_16))
val HifiShapes = Shapes(
    extraSmall = RoundedCornerShape(2.dp), small = RoundedCornerShape(2.dp),
    medium = RoundedCornerShape(4.dp), large = RoundedCornerShape(6.dp), extraLarge = RoundedCornerShape(8.dp),
)

fun hifiAccent(hex: String): Color = Color(0xFF000000 or (hex.removePrefix("#").toLongOrNull(16)?.takeIf { it in 0..0xFFFFFF } ?: 0xE8B45C))

fun hifiColors(accent: Color, night: Boolean = false, pureBlack: Boolean = false, highContrast: Boolean = false): ColorScheme {
    val background = if (night || pureBlack) Color.Black else Color(0xFF141414)
    val surface = if (night || pureBlack) Color(0xFF090B0E) else Color(0xFF1C1C1C)
    val text = if (night || highContrast) Color(0xFFF4F5F7) else Color(0xFFF1E5D3)
    val onAccent = if (accent.luminance() > 0.35f) Color(0xFF171717) else Color.White
    return darkColorScheme(
        primary = accent, onPrimary = onAccent,
        primaryContainer = lerp(surface, accent, 0.18f), onPrimaryContainer = text,
        secondary = accent, onSecondary = onAccent,
        secondaryContainer = lerp(surface, accent, 0.10f), onSecondaryContainer = text,
        background = background, onBackground = text,
        surface = surface, onSurface = text,
        surfaceVariant = if (night) Color(0xFF1C2027) else Color(0xFF303030),
        onSurfaceVariant = if (highContrast) text else Color(0xFFCBC8C2),
        surfaceContainerLowest = background,
        surfaceContainer = if (night) Color(0xFF101318) else Color(0xFF222222),
        surfaceContainerLow = surface,
        surfaceContainerHigh = if (night) Color(0xFF181C23) else Color(0xFF2B2B2B),
        surfaceContainerHighest = if (night) Color(0xFF20252D) else Color(0xFF343434),
        outline = if (highContrast) Color(0xFFC2C2C2) else Color(0xFF716D66),
        outlineVariant = if (highContrast) Color(0xFF888888) else Color(0xFF494641),
    )
}

@Composable
fun HifiThemeSettings(showTitle: Boolean = true) {
    var accent by rememberPreference(HifiAccentKey, "E8B45C")
    var appearance by rememberPreference(HifiAppearanceKey, "classic")
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (showTitle) Text("Aspetto HiFi", style = MaterialTheme.typography.titleMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("classic" to "HiFi classico", "night" to "Dark / Night").forEach { (value, name) ->
                FilterChip(selected = appearance == value, onClick = { appearance = value }, label = { Text(name) })
            }
        }
        Text("Colore", style = MaterialTheme.typography.titleMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            presets.forEach { (name, hex) ->
                val selected = accent.equals(hex, true)
                OutlinedButton(
                    onClick = { accent = hex },
                    border = BorderStroke(if (selected) 2.dp else 1.dp,
                        if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    Box(Modifier.size(24.dp).background(hifiAccent(hex), RoundedCornerShape(2.dp)), contentAlignment = Alignment.Center) {
                        if (selected) Icon(Icons.Rounded.Check, "Colore selezionato", tint = Color(0xFF171717), modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(name)
                }
            }
        }
    }
}
