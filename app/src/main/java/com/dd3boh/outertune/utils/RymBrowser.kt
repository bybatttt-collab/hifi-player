package com.dd3boh.outertune.utils

import java.net.URI
import java.util.concurrent.atomic.AtomicLong
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow

object RymBrowserPolicy {
    const val HOME = "https://rateyourmusic.com/"
    fun internalUrl(raw: String): Boolean = runCatching {
        val uri = URI(raw)
        uri.scheme == "https" && uri.userInfo == null && uri.port in setOf(-1, 443) &&
            (uri.host in setOf("rateyourmusic.com", "www.rateyourmusic.com") ||
                (uri.host in setOf("www.google.com", "google.com", "www.google.it", "google.it") && uri.path in setOf("/search", "/url")) ||
                uri.host == "consent.google.com")
    }.getOrDefault(false)
}

/** Navigation requests only. Web cookies remain in Android WebView's private cookie store. */
object RymBrowser {
    data class Request(val id: Long, val url: String)
    private val ids = AtomicLong()
    private val current = MutableStateFlow<Request?>(null)
    val request = current.asStateFlow()
    private val openEvents = Channel<Unit>(Channel.CONFLATED)
    val openings = openEvents.receiveAsFlow()
    private val albumEvents = Channel<String>(Channel.CONFLATED)
    val albumOpenings = albumEvents.receiveAsFlow()
    private val videoEvents = Channel<com.zionhuang.innertube.models.SongItem>(Channel.CONFLATED)
    val videoOpenings = videoEvents.receiveAsFlow()
    fun openFullAlbum(song: com.zionhuang.innertube.models.SongItem) {
        if (song.id.matches(Regex("[A-Za-z0-9_-]{11}"))) videoEvents.trySend(song)
    }
    fun openAlbum(id: String) {
        if (id.matches(Regex("[A-Za-z0-9_-]{1,160}"))) albumEvents.trySend(id)
    }
    fun open(url: String) {
        if (!RymBrowserPolicy.internalUrl(url)) return
        current.value = Request(ids.incrementAndGet(), url)
        openEvents.trySend(Unit)
    }
}
