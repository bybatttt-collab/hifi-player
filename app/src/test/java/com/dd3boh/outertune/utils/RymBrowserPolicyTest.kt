package com.dd3boh.outertune.utils

import org.junit.Assert.*
import org.junit.Test

class RymBrowserPolicyTest {
    @Test fun allowsRymBrowsingAndSearchFallback() {
        listOf("https://rateyourmusic.com/", "https://rateyourmusic.com/account/login", "https://www.rateyourmusic.com/release/album/test/", "https://www.google.com/search?q=artist+album", "https://consent.google.com/m").forEach { assertTrue(it, RymBrowserPolicy.internalUrl(it)) }
    }
    @Test fun rejectsOtherOriginsAndPrivilegedSchemes() {
        listOf("https://rateyourmusic.com.evil.example/", "https://rateyourmusic.com@evil.example/", "https://user@rateyourmusic.com/", "http://rateyourmusic.com/", "https://www.google.com/other/", "file:///etc/passwd", "javascript:alert(1)", "intent://anything", "https://example.com/").forEach { assertFalse(it, RymBrowserPolicy.internalUrl(it)) }
    }
}
