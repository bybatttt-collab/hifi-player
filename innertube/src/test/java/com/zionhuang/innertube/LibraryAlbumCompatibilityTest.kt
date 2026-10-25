package com.zionhuang.innertube

import com.zionhuang.innertube.models.GridRenderer
import com.zionhuang.innertube.models.MusicTwoRowItemRenderer
import com.zionhuang.innertube.models.YTItem
import com.zionhuang.innertube.pages.LibraryContinuationPage
import com.zionhuang.innertube.pages.LibraryPage
import com.zionhuang.innertube.pages.decodeLibrarySections
import com.zionhuang.innertube.models.SectionListRenderer
import com.zionhuang.innertube.utils.walkItems
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalSerializationApi::class)
class LibraryAlbumCompatibilityTest {
    private val json = Json { ignoreUnknownKeys = true; explicitNulls = false }
    private fun card(endpoint: String = "watchEndpoint", withPlaylist: Boolean = true): MusicTwoRowItemRenderer = json.decodeFromString("""
        {
          "title": {"runs": [{"text": "Test album"}]},
          "navigationEndpoint": {"browseEndpoint": {"browseId": "MPREtest"}},
          "thumbnailRenderer": {"musicThumbnailRenderer": {"thumbnail": {"thumbnails": [{"url": "https://example.org/cover.jpg"}]}}},
          "thumbnailOverlay": {"musicItemThumbnailOverlayRenderer": {"content": {"musicPlayButtonRenderer": {
            "playNavigationEndpoint": {"$endpoint": {${if (withPlaylist) "\"playlistId\": \"OLAKtest\"" else "\"videoId\": \"samplevideo\""}}}
          }}}}
        }
    """.trimIndent())

    @Test fun albumWithoutPageTypeUsesItsStableBrowseIdAndWatchPlaylist() {
        val album = LibraryPage.fromMusicTwoRowItemRenderer(card("watchPlaylistEndpoint"))
        assertNotNull(album)
        assertEquals("MPREtest", album!!.id)
    }

    @Test fun ordinaryWatchEndpointCanCarryTheAlbumPlaylist() {
        assertNotNull(LibraryPage.fromMusicTwoRowItemRenderer(card()))
    }

    @Test fun oneUnreadableCardDoesNotHideAllOtherAlbums() {
        val grid = GridRenderer(null, listOf(GridRenderer.Item(null, card()), GridRenderer.Item(null, card(withPlaylist = false))), null)
        val section = SectionListRenderer.Content(null, null, null, null, null, null, null, grid, null)
        val page = decodeLibrarySections(listOf(section))
        assertEquals(1, page.items.size)
        assertFalse(page.complete)
    }

    @Test fun partialPagesRemainPartialAfterFollowingAllContinuations() = runBlocking {
        val album = LibraryPage.fromMusicTwoRowItemRenderer(card())!!
        val first = LibraryPage(listOf(album), "next", complete = false)
        val result = first.walkItems { Result.success(LibraryContinuationPage(emptyList(), null, complete = false)) }
        assertEquals(listOf(album), result.items)
        assertFalse(result.complete)
    }

    @Test fun unreadableContinuationDoesNotPreventReadingLaterPages() = runBlocking {
        val album = LibraryPage.fromMusicTwoRowItemRenderer(card())!!
        val first = LibraryPage(emptyList(), "bad")
        val result = first.walkItems { token ->
            Result.success(if (token == "bad") LibraryContinuationPage(emptyList(), "good", false)
            else LibraryContinuationPage(listOf(album), null))
        }
        assertEquals(1, result.items.size)
        assertFalse(result.complete)
    }
}
