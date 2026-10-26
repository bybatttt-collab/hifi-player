package com.dd3boh.outertune.utils

import java.text.Normalizer
import java.util.Locale

object LibrarySearch {
    private val marks = Regex("\\p{M}+")
    private fun normalise(value: String) =
        Normalizer.normalize(value, Normalizer.Form.NFD).replace(marks, "").lowercase(Locale.ROOT)

    fun matches(query: String, title: String, artists: Iterable<String> = emptyList()): Boolean {
        val words = normalise(query).trim().split(Regex("\\s+")).filter(String::isNotEmpty)
        if (words.isEmpty()) return true
        val haystack = normalise((listOf(title) + artists).joinToString(" "))
        return words.all(haystack::contains)
    }
}
