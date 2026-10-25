package com.dd3boh.outertune.utils

import java.text.Normalizer
import java.util.Locale

data class RymAlbumContext(val title: String, val artists: List<String>, val url: String)
object RymAlbumMatching {
    private fun normalize(value: String) = Normalizer.normalize(value, Normalizer.Form.NFKC)
        .lowercase(Locale.ROOT).replace(Regex("[^\\p{L}\\p{N}]"), "")
    fun exact(title: String, artists: List<String>, context: RymAlbumContext): Boolean =
        context.artists.isNotEmpty() && normalize(title) == normalize(context.title) &&
            context.artists.all { expected -> artists.any { normalize(it) == normalize(expected) } }
}
