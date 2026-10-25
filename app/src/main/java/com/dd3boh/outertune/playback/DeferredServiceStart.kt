package com.dd3boh.outertune.playback

/** Android may reject a service start while the Activity is behind the lock screen. */
internal class DeferredServiceStart(private val start: () -> Unit) {
    private var pending = false

    @Synchronized
    fun request(): Boolean = try {
        start()
        pending = false
        true
    } catch (_: IllegalStateException) {
        pending = true
        false
    }

    @Synchronized
    fun retryPending(): Boolean = if (pending) request() else true
}
