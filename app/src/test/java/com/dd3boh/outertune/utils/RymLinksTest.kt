package com.dd3boh.outertune.utils

import java.net.URLDecoder
import org.junit.Assert.*
import org.junit.Test

class RymLinksTest {
    @Test fun unicodeAlbumSearchHasCorrectScopeAndContext() {
        val url = RymLinks.searchUrl("無人テレビの設計図 & Test", listOf("blgtz"), album = true)!!
        val decoded = URLDecoder.decode(url.substringAfter("?q="), "UTF-8")
        assertEquals("blgtz 無人テレビの設計図 & Test site:rateyourmusic.com/release/", decoded)
        assertFalse(url.contains("& Test"))
    }

    @Test fun missingTitleDoesNotOpenAnUnrelatedSearch() {
        assertNull(RymLinks.searchUrl("  ", listOf("Artist"), album = true))
    }

    @Test fun songSearchUsesSongScope() {
        assertTrue(URLDecoder.decode(RymLinks.searchUrl("Dael", listOf("Autechre"), false)!!, "UTF-8").contains("site:rateyourmusic.com/song/"))
    }
}
