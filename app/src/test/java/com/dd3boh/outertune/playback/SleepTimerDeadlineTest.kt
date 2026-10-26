package com.dd3boh.outertune.playback

import androidx.media3.common.Player
import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Test
import java.lang.reflect.Proxy

class SleepTimerDeadlineTest {
    private class Alarm : SleepTimerAlarm {
        val callbacks = mutableListOf<() -> Unit>()
        override fun schedule(delayMillis: Long, action: () -> Unit) { callbacks += action }
        override fun cancel() {}
    }
    private fun runCase(test: (SleepTimer, Alarm, (Long) -> Unit, () -> Int) -> Unit) {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        var now = 0L
        var pauses = 0
        val player = Proxy.newProxyInstance(Player::class.java.classLoader, arrayOf(Player::class.java)) { _, method, _ ->
            when (method.name) {
                "pause" -> { pauses++; null }
                "toString" -> "test player"
                else -> error("Unexpected player call: " + method.name)
            }
        } as Player
        val alarm = Alarm()
        val timer = SleepTimer(scope, player, alarm, elapsed = { now }, wallClock = { 1000L }).apply { fadeEnabled = false }
        try { test(timer, alarm, { now = it }, { pauses }) }
        finally { timer.clear(); scope.cancel() }
    }
    @Test fun deadlinePausesEvenBeforeCoroutineResumes() = runCase { timer, alarm, advance, pauses ->
        timer.start(1)
        assertEquals(60_000L, timer.remainingTimeMs)
        advance(60_000)
        alarm.callbacks.last().invoke()
        assertEquals(1, pauses())
        assertFalse(timer.isActive)
        assertEquals(1f, timer.fadeFactor.value)
    }
    @Test fun replacedAlarmCannotStopNewTimer() = runCase { timer, alarm, advance, pauses ->
        timer.start(1)
        val old = alarm.callbacks.last()
        timer.start(2)
        advance(60_000)
        old()
        assertEquals(0, pauses())
        assertEquals(60_000L, timer.remainingTimeMs)
        advance(120_000)
        val current = alarm.callbacks.last()
        current()
        current()
        assertEquals(1, pauses())
        assertFalse(timer.isActive)
    }
    @Test fun earlyAlarmWaitsForMonotonicDeadline() = runCase { timer, alarm, advance, pauses ->
        timer.start(1)
        alarm.callbacks.last().invoke()
        assertEquals(0, pauses())
        assertTrue(timer.isActive)
        advance(60_000)
        alarm.callbacks.last().invoke()
        assertEquals(1, pauses())
    }
    @Test fun clearingDisarmsQueuedAlarm() = runCase { timer, alarm, advance, pauses ->
        timer.start(1)
        val callback = alarm.callbacks.last()
        timer.clear()
        advance(60_000)
        callback()
        assertEquals(0, pauses())
    }
}
