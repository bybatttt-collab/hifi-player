package com.dd3boh.outertune.ui.theme

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Shapes
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.stringPreferencesKey
import com.dd3boh.outertune.R
import com.dd3boh.outertune.utils.rememberPreference

val HifiAccentKey = stringPreferencesKey("hifiAccentHex")
private val presets = linkedMapOf("Ambra" to "E8B45C", "Verde" to "B7DE73", "Ghiaccio" to "83C9DF", "Viola" to "C2A1E8")
val HifiTitleFont = FontFamily(Font(R.font.dot_gothic_16))
val HifiShapes = Shapes(
    extraSmall = RoundedCornerShape(2.dp), small = RoundedCornerShape(2.dp),
    medium = RoundedCornerShape(4.dp), large = RoundedCornerShape(6.dp), extraLarge = RoundedCornerShape(8.dp),
)

fun hifiAccent(hex: String): Color = Color(0xFF000000 or (hex.removePrefix("#").toLongOrNull(16)?.takeIf { it in 0..0xFFFFFF } ?: 0xE8B45C))

fun hifiColors(accent: Color) = darkColorScheme(
    primary = accent,
    onPrimary = if (accent.luminance() > 0.35f) Color(0xFF171717) else Color.White,
    primaryContainer = Color(0xFF393025), onPrimaryContainer = Color(0xFFFFECD0),
    secondary = accent, onSecondary = if (accent.luminance() > 0.35f) Color.Black else Color.White,
    secondaryContainer = Color(0xFF343434), onSecondaryContainer = Color(0xFFF1E5D3),
    background = Color(0xFF141414), onBackground = Color(0xFFF1E5D3),
    surface = Color(0xFF1C1C1C), onSurface = Color(0xFFF1E5D3),
    surfaceVariant = Color(0xFF303030), onSurfaceVariant = Color(0xFFCFC5B7),
    surfaceContainer = Color(0xFF222222), surfaceContainerLow = Color(0xFF1C1C1C),
    surfaceContainerHigh = Color(0xFF2B2B2B), surfaceContainerHighest = Color(0xFF343434),
    outline = Color(0xFF71685B), outlineVariant = Color(0xFF49433B),
)

@Composable
fun HifiThemeSettings() {
    var accent by rememberPreference(HifiAccentKey, "E8B45C")
    var entry by remember(accent) { mutableStateOf(accent) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(R.string.hifi_themes), style = MaterialTheme.typography.titleMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            presets.forEach { (name, hex) ->
                OutlinedButton(onClick = { accent = hex }) { Text(name) }
            }
        }
        OutlinedTextField(
            value = entry,
            onValueChange = {
                entry = it.removePrefix("#").take(6)
                if (entry.matches(Regex("[0-9a-fA-F]{6}"))) accent = entry.uppercase()
            },
            label = { Text(stringResource(R.string.hifi_custom_color)) },
            singleLine = true,
        )
    }
}

