package com.dd3boh.outertune.utils

import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import java.net.URI
import java.net.URLDecoder
import java.util.concurrent.TimeUnit

object RymFirstResult {
    private val client = OkHttpClient.Builder().followRedirects(false).connectTimeout(6, TimeUnit.SECONDS).readTimeout(6, TimeUnit.SECONDS).callTimeout(10, TimeUnit.SECONDS).build()
    fun resultUrl(raw: String, album: Boolean): String? = runCatching {
        var uri = URI("https://www.google.com").resolve(raw)
        if (uri.host in setOf("www.google.com", "google.com", "www.google.it", "google.it") && uri.path == "/url") {
            val query = uri.rawQuery.orEmpty().split('&').map { it.substringBefore('=') to URLDecoder.decode(it.substringAfter('=', ""), "UTF-8") }.toMap()
            uri = URI(query["q"] ?: query["url"] ?: return null)
        }
        val prefix = if (album) "/release/" else "/song/"
        if (uri.scheme != "https" || uri.host !in setOf("rateyourmusic.com", "www.rateyourmusic.com") || uri.userInfo != null || uri.port !in setOf(-1, 443) || !uri.path.orEmpty().startsWith(prefix)) null else uri.toString()
    }.getOrNull()
    fun firstLink(html: String, album: Boolean): String? {
        val page = Jsoup.parse(html)
        for (link in page.select("a:has(h3)")) resultUrl(link.attr("href"), album)?.let { return it }
        for (link in page.select("a[href]")) resultUrl(link.attr("href"), album)?.let { return it }
        return null
    }
    fun find(searchUrl: String, album: Boolean): String? = runCatching {
        client.newCall(Request.Builder().url("$searchUrl&btnI=1").header("Cookie", "SOCS=CAI").build()).execute().use { response ->
            response.header("Location")?.let { return resultUrl(it, album) }
            if (!response.isSuccessful || response.header("Content-Type")?.contains("text/html") != true) return null
            val source = response.body?.source() ?: return null
            source.request(3_000_001)
            if (source.buffer.size > 3_000_000) return null
            val html = source.buffer.readUtf8()
            if (html.contains("/sorry/") || html.contains("id=\"captcha")) return null
            firstLink(html, album)
        }
    }.getOrNull()
}
