package com.dd3boh.outertune.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import java.net.URLEncoder
import kotlinx.coroutines.launch

object RymLinks {
    private val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.SupervisorJob() + kotlinx.coroutines.Dispatchers.IO)
    private val sequence = java.util.concurrent.atomic.AtomicLong()
    fun searchUrl(title: String, artists: List<String>, album: Boolean): String? {
        if (title.isBlank()) return null
        val scope = if (album) "site:rateyourmusic.com/release/" else "site:rateyourmusic.com/song/"
        val query = (artists.filter { it.isNotBlank() } + title.trim() + scope).joinToString(" ")
        return "https://www.google.com/search?q=" + URLEncoder.encode(query, "UTF-8")
    }

    fun open(context: Context, title: String, artists: List<String>, album: Boolean) {
        val url = searchUrl(title, artists, album) ?: return
        val app = context.applicationContext
        val requestId = sequence.incrementAndGet()
        scope.launch {
            val target = RymFirstResult.find(url, album) ?: url
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                if (sequence.get() != requestId) return@withContext
                RymBrowser.open(target)
            }
        }
    }
}
