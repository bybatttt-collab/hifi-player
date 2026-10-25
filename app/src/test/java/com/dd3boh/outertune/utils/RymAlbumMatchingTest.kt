package com.dd3boh.outertune.utils

import org.junit.Assert.*
import org.junit.Test

class RymAlbumMatchingTest {
    @Test fun matchesUnicodeAndPunctuationWithoutDiscardingEdition() {
        val context = RymAlbumContext("Tri Repetae", listOf("Autechre"), "https://rateyourmusic.com/release/album/autechre/tri-repetae/")
        assertTrue(RymAlbumMatching.exact("TRI REPETAE", listOf("autechre"), context))
        assertFalse(RymAlbumMatching.exact("Tri Repetae (Deluxe Edition)", listOf("Autechre"), context))
        assertFalse(RymAlbumMatching.exact("Tri Repetae", listOf("Another artist"), context))
    }
    @Test fun doesNotGuessWhenArtistIsMissingAndKeepsJapaneseTitles() {
        assertFalse(RymAlbumMatching.exact("Hits", listOf("Artist"), RymAlbumContext("Hits", emptyList(), "")))
        assertTrue(RymAlbumMatching.exact("無人テレビの設計図", listOf("blgtz"), RymAlbumContext("無人テレビの設計図", listOf("blgtz"), "")))
        assertFalse(RymAlbumMatching.exact("無人テレビの設計図", listOf("blgtz"), RymAlbumContext("保留", listOf("blgtz"), "")))
    }
}
