package com.dd3boh.outertune.ui.component

import android.media.AudioManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.dd3boh.outertune.R
import com.dd3boh.outertune.ui.theme.HifiTitleFont
import com.dd3boh.outertune.utils.mediaVolumePercent
import com.dd3boh.outertune.utils.mediaVolumeIndex
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HifiVolumeControl(onVolumeSet: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current.applicationContext
    val audio = remember(context) { context.getSystemService(AudioManager::class.java) }
    val lifecycle = LocalLifecycleOwner.current
    var maximum by remember(audio) { mutableIntStateOf(audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC)) }
    var current by remember(audio) { mutableIntStateOf(audio.getStreamVolume(AudioManager.STREAM_MUSIC)) }
    var available by remember(audio) { mutableStateOf(!audio.isVolumeFixed) }
    val latestOnVolumeSet by rememberUpdatedState(onVolumeSet)
    LaunchedEffect(audio, lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (isActive) {
                maximum = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                current = audio.getStreamVolume(AudioManager.STREAM_MUSIC)
                available = !audio.isVolumeFixed
                delay(200)
            }
        }
    }
    val percent = mediaVolumePercent(current, maximum)
    val accent = MaterialTheme.colorScheme.primary
    val inactive = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    val label = stringResource(R.string.volume)
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.AutoMirrored.Rounded.VolumeUp, null, tint = MaterialTheme.colorScheme.onSurface)
        Column(Modifier.widthIn(min = 48.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("VOL", fontFamily = HifiTitleFont, color = MaterialTheme.colorScheme.onSurface)
            Text(percent.toString(), fontFamily = HifiTitleFont, color = accent)
        }
        Slider(
            value = percent.toFloat(),
            enabled = available && maximum > 0,
            onValueChange = { requested ->
                val target = mediaVolumeIndex(requested, maximum)
                try {
                    audio.setStreamVolume(AudioManager.STREAM_MUSIC, target, 0)
                    current = audio.getStreamVolume(AudioManager.STREAM_MUSIC)
                    latestOnVolumeSet()
                } catch (_: SecurityException) { available = false }
            },
            valueRange = 0f..100f,
            steps = (maximum - 1).coerceAtLeast(0),
            modifier = Modifier.weight(1f).semantics { contentDescription = label },
            thumb = { Spacer(Modifier.size(1.dp)) },
            track = { state ->
                Canvas(Modifier.fillMaxWidth().height(30.dp)) {
                    val count = 48
                    val step = size.width / count
                    val fraction = (state.value / 100f).coerceIn(0f, 1f)
                    repeat(count) { index ->
                        drawRect(if (index < (fraction * count).roundToInt()) accent else inactive,
                            topLeft = Offset(index * step, 0f), size = Size((step - 2.dp.toPx()).coerceAtLeast(1f), size.height))
                    }
                }
            }
        )
    }
}
