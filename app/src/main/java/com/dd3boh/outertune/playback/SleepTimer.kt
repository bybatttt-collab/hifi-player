package com.dd3boh.outertune.playback

import android.os.SystemClock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import com.dd3boh.outertune.constants.SleepTimerDefaults
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/** One monotonic deadline shared by the service, its wake-up alarm and the countdown UI. */
class SleepTimer(
    private val scope: CoroutineScope,
    val player: Player,
    private val alarm: SleepTimerAlarm = SleepTimerAlarm.None,
    private val elapsed: () -> Long = { SystemClock.elapsedRealtime() },
    private val wallClock: () -> Long = System::currentTimeMillis,
) : Player.Listener {
    private var sleepTimerJob: Job? = null
    private var deadline = -1L
    private var generation = 0L
    var triggerTime by mutableLongStateOf(-1L)
        private set
    var pauseWhenSongEnd by mutableStateOf(false)
        private set
    val isActive: Boolean get() = triggerTime != -1L || pauseWhenSongEnd
    val remainingTimeMs: Long get() = if (deadline < 0) 0 else (deadline - elapsed()).coerceAtLeast(0)
    var fadeEnabled: Boolean = SleepTimerDefaults.FADE_ENABLED
    var fadeDurationMs: Long = SleepTimerDefaults.FADE_DURATION_SECONDS * 1000L
    private val _fadeFactor = MutableStateFlow(1f)
    val fadeFactor = _fadeFactor.asStateFlow()
    var onFinish: () -> Unit = { player.pause() }

    fun start(minute: Int) {
        clear()
        val ticket = generation
        if (minute == -1) {
            pauseWhenSongEnd = true
            if (fadeEnabled) sleepTimerJob = scope.launch {
                while (currentCoroutineContext().isActive && pauseWhenSongEnd) {
                    val duration = player.duration
                    val remaining = if (duration == C.TIME_UNSET || duration <= 0) Long.MAX_VALUE else duration - player.currentPosition
                    _fadeFactor.value = fadeFactorFor(remaining)
                    delay(if (remaining <= fadeDurationMs) FADE_TICK_MS else POLL_MS)
                }
            }
        } else {
            val duration = minute.coerceAtLeast(0) * 60_000L
            deadline = elapsed() + duration
            triggerTime = wallClock() + duration // notification's wall-clock display only
            alarm.schedule(duration) { deadlineReached(ticket) }
            sleepTimerJob = scope.launch {
                while (currentCoroutineContext().isActive && ticket == generation) {
                    val remaining = remainingTimeMs
                    if (remaining == 0L) {
                        finish(ticket)
                        break
                    }
                    _fadeFactor.value = if (fadeEnabled) fadeFactorFor(remaining) else 1f
                    delay(minOf(remaining, if (fadeEnabled && remaining <= fadeDurationMs) FADE_TICK_MS else POLL_MS))
                }
            }
        }
    }

    fun clear() {
        generation++
        alarm.cancel()
        sleepTimerJob?.cancel()
        sleepTimerJob = null
        deadline = -1L
        pauseWhenSongEnd = false
        triggerTime = -1L
        _fadeFactor.value = 1f
    }

    private fun finish(ticket: Long) {
        if (ticket != generation || !isActive) return
        clear() // disarm before player callbacks, so an alarm and a transition cannot stop twice
        onFinish()
    }

    private fun deadlineReached(ticket: Long) {
        if (ticket != generation || deadline < 0) return
        val remaining = remainingTimeMs
        if (remaining > 0) alarm.schedule(remaining) { deadlineReached(ticket) }
        else finish(ticket)
    }

    private fun fadeFactorFor(remainingMs: Long): Float =
        if (fadeDurationMs <= 0 || remainingMs >= fadeDurationMs) 1f
        else (remainingMs.toFloat() / fadeDurationMs).coerceIn(0f, 1f)

    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        if (pauseWhenSongEnd && endsSong(reason)) finish(generation)
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        if (playbackState == Player.STATE_ENDED && pauseWhenSongEnd) finish(generation)
    }

    companion object {
        internal fun endsSong(reason: Int) =
            reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO || reason == Player.MEDIA_ITEM_TRANSITION_REASON_REPEAT
        private const val FADE_TICK_MS = 40L
        private const val POLL_MS = 1000L
    }
}
