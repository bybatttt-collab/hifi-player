package com.dd3boh.outertune.utils

import android.content.Context
import java.net.URI
import java.util.Locale

/** Conservative subset of EasyList: unconditional domain rules and RYM cosmetic selectors. */
class RymAdBlocker(context: Context) {
    private val app = context.applicationContext
    private val lines by lazy { app.assets.open("hifi/easylist.txt").bufferedReader().use { it.readLines() } }
    private val domains by lazy {
        networkDomains(lines.asSequence()) + setOf(
            "doubleclick.net", "googlesyndication.com", "googleadservices.com",
            "googletagservices.com", "amazon-adsystem.com", "connatix.com",
        )
    }
    val css by lazy {
        val site = lines.filter { it.startsWith("rateyourmusic.com##") }.map { it.substringAfter("##") }
        (site + listOf(".adsbygoogle", ".google-auto-placed", "[id^='div-gpt-ad']", ".ad-container", ".advertisement"))
            .joinToString(",") + "{display:none!important;}"
    }
    fun blocks(url: String): Boolean = blockedHost(runCatching { URI(url).host }.getOrNull(), domains)
    companion object {
        fun networkDomains(lines: Sequence<String>): Set<String> = lines.mapNotNull { line ->
            if (!line.startsWith("||") || '^' !in line) return@mapNotNull null
            val rest = line.removePrefix("||")
            val domain = rest.substringBefore('^').lowercase(Locale.ROOT)
            val options = rest.substringAfter('^')
            if (options !in setOf("", "\$third-party") || !domain.matches(Regex("[a-z0-9.-]+")) || '.' !in domain) null else domain
        }.toSet()
        fun blockedHost(host: String?, domains: Set<String>): Boolean {
            var candidate = host?.lowercase(Locale.ROOT)?.trimEnd('.') ?: return false
            if (candidate == "rateyourmusic.com" || candidate.endsWith(".rateyourmusic.com")) return false
            while ('.' in candidate) {
                if (candidate in domains) return true
                candidate = candidate.substringAfter('.')
            }
            return false
        }
    }
}
