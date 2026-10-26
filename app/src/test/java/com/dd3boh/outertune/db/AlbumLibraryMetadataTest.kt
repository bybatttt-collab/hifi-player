package com.dd3boh.outertune.db

import com.dd3boh.outertune.db.entities.AlbumEntity
import com.zionhuang.innertube.models.AlbumItem
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDateTime

class AlbumLibraryMetadataTest {
    private fun card(year: Int? = 1964, thumbnail: String = "cover") = AlbumItem(
        browseId = "MPRE_album", playlistId = "OLAK_album", title = "The Sidewinder",
        artists = emptyList(), year = year, thumbnail = thumbnail)
    @Test fun cachedAlbumGetsMissingMetadataWithoutLosingSaveOrSongs() {
        val saved = LocalDateTime.of(2020,1,1,0,0)
        val old = AlbumEntity(id = "MPRE_album", title = "The Sidewinder", songCount = 5, duration = 2400,
            bookmarkedAt = saved, savedOrder = 42)
        val repaired = mergeAlbumLibraryMetadata(old, card())
        assertEquals(1964, repaired.year)
        assertEquals("cover", repaired.thumbnailUrl)
        assertEquals("OLAK_album", repaired.playlistId)
        assertEquals(saved, repaired.bookmarkedAt)
        assertEquals(42L, repaired.savedOrder)
        assertEquals(5, repaired.songCount)
        assertEquals(2400, repaired.duration)
    }
    @Test fun thinResponseKeepsKnownDetails() {
        val old = AlbumEntity(id = "MPRE_album", title = "The Sidewinder", year = 1964,
            thumbnailUrl = "known", songCount = 5, duration = 2400)
        val repaired = mergeAlbumLibraryMetadata(old, card(year = null, thumbnail = ""))
        assertEquals(1964, repaired.year)
        assertEquals("known", repaired.thumbnailUrl)
    }
}
