package com.zionhuang.innertube

import com.zionhuang.innertube.models.Continuation
import com.zionhuang.innertube.models.GridRenderer
import com.zionhuang.innertube.models.SectionListRenderer
import com.zionhuang.innertube.pages.decodeLibrarySections
import org.junit.Assert.*
import org.junit.Test

class LibraryPageDecoderTest {
    private fun section(grid: GridRenderer? = null) = SectionListRenderer.Content(
        musicCarouselShelfRenderer = null, musicShelfRenderer = null, musicCardShelfRenderer = null,
        musicPlaylistShelfRenderer = null, musicDescriptionShelfRenderer = null,
        musicResponsiveHeaderRenderer = null, musicEditablePlaylistDetailHeaderRenderer = null,
        gridRenderer = grid, itemSectionRenderer = null,
    )

    private fun grid(token: String? = null) = GridRenderer(
        header = null, items = emptyList(),
        continuations = token?.let { listOf(Continuation(Continuation.NextContinuationData(it))) },
    )

    @Test fun headerBeforeShelfDoesNotHideTheContinuation() {
        val page = decodeLibrarySections(listOf(section(), section(grid("second-page"))))
        assertEquals("second-page", page.continuation)
    }

    @Test fun unknownResponseCannotBecomeACompleteEmptyLibrary() {
        assertThrows(IllegalArgumentException::class.java) { decodeLibrarySections(listOf(section())) }
        assertThrows(IllegalArgumentException::class.java) { decodeLibrarySections(null) }
    }

    @Test fun aRecognizedEmptyGridIsAllowed() {
        val page = decodeLibrarySections(listOf(section(grid())))
        assertTrue(page.items.isEmpty())
        assertNull(page.continuation)
    }

    @Test fun independentContinuationsCannotSilentlyDropOneShelf() {
        assertThrows(IllegalArgumentException::class.java) {
            decodeLibrarySections(listOf(section(grid("a")), section(grid("b"))))
        }
    }
}
