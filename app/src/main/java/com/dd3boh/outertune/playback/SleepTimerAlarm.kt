package com.dd3boh.outertune.playback

import android.app.AlarmManager
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log

interface SleepTimerAlarm {
    fun schedule(delayMillis: Long, action: () -> Unit)
    fun cancel()
    object None : SleepTimerAlarm {
        override fun schedule(delayMillis: Long, action: () -> Unit) {}
        override fun cancel() {}
    }
}

/** Listener alarms belong to this live playback service and need no exact-alarm permission.
 * https://developer.android.com/develop/background-work/services/alarms
 */
class AndroidSleepTimerAlarm(context: Context) : SleepTimerAlarm {
    private val manager = context.getSystemService(AlarmManager::class.java)
    private val handler = Handler(Looper.getMainLooper())
    private var listener: AlarmManager.OnAlarmListener? = null

    override fun schedule(delayMillis: Long, action: () -> Unit) {
        cancel()
        val callback = AlarmManager.OnAlarmListener { action() }
        listener = callback
        val at = SystemClock.elapsedRealtime() + delayMillis.coerceAtLeast(0)
        try {
            manager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, at, "HiFiSleepTimer", callback, handler)
        } catch (e: SecurityException) {
            // Keep the monotonic service countdown as a fallback on restrictive devices.
            listener = null
            Log.w("SleepTimer", "System deadline unavailable; service countdown retained", e)
        }
    }

    override fun cancel() {
        listener?.let(manager::cancel)
        listener = null
    }
}
