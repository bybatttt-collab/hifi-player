package com.dd3boh.outertune.utils

import org.junit.Assert.*
import org.junit.Test

class LibrarySearchTest {
    @Test fun artistMatchesKeepExistingOrder() {
        val rows = listOf("The Sidewinder" to "Lee Morgan", "Blue Train" to "John Coltrane", "Cornbread" to "Lee Morgan")
        assertEquals(listOf("The Sidewinder", "Cornbread"), rows.filter { LibrarySearch.matches("lee morgan", it.first, listOf(it.second)) }.map { it.first })
    }
    @Test fun combineTitleAndArtistIgnoringCaseAndAccents() {
        assertTrue(LibrarySearch.matches("  MORGAN   sidewinder ", "The Sidewinder", listOf("Lee Morgan")))
        assertTrue(LibrarySearch.matches("bjork", "Vespertine", listOf("Björk")))
        assertFalse(LibrarySearch.matches("lee morgan", "Blue Train", listOf("John Coltrane")))
    }
    @Test fun blankRestoresAllAndPunctuationIsLiteral() {
        assertTrue(LibrarySearch.matches("   ", "Any playlist"))
        assertTrue(LibrarySearch.matches("100%", "100% jazz"))
        assertFalse(LibrarySearch.matches("100%", "1000 jazz"))
        assertTrue(LibrarySearch.matches("無人", "無人テレビの設計図"))
    }
}
