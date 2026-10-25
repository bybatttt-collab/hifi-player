package com.zionhuang.innertube

import com.zionhuang.innertube.models.AlbumItem
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test

class HifiPublicSearchTest {
    @Test fun publicAlbumSearch() = runBlocking {
        assumeTrue(System.getenv("HIFI_LIVE_TESTS") == "1")
        val page = YouTube.search("Autechre Tri Repetae", YouTube.SearchFilter.FILTER_ALBUM).getOrThrow()
        assertTrue(page.items.filterIsInstance<AlbumItem>().any { it.title.contains("Tri Repetae", true) })
    }

    @Test fun publicFullAlbumSearch() = runBlocking {
        assumeTrue(System.getenv("HIFI_LIVE_TESTS") == "1")
        val page = FullAlbumSearch.search("blgtz 無人テレビの設計図").getOrThrow()
        assertTrue(page.items.any { it.title.contains("無人テレビ") })
    }
}
