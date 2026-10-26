package com.dd3boh.outertune.utils

import org.junit.Assert.*
import org.junit.Test
import java.nio.file.Files

class PlaylistCoverTest {
    @Test fun largeAndNarrowImagesKeepTheirAspectRatioWithinTheLimit() {
        assertEquals(1024 to 512, playlistCoverSize(4000,2000))
        assertEquals(256 to 1024, playlistCoverSize(1000,4000))
        assertEquals(16 to 16, playlistCoverSize(16,16))
        assertEquals(1 to 1024, playlistCoverSize(1,20000))
    }
    @Test fun synchronizationKeepsPersonalCoverAndRecoversWhenFileIsMissing() {
        val root=Files.createTempDirectory("hifi-cover-test").toFile()
        val directory=root.resolve(PLAYLIST_COVER_DIRECTORY).apply {mkdir()}
        val file=directory.resolve("example.jpg").apply {writeBytes(byteArrayOf(1))}
        try {
            assertEquals(file.absolutePath,playlistCoverAfterSync(file.absolutePath,"https://example.org/remote.jpg"))
            file.delete()
            assertEquals("https://example.org/remote.jpg",playlistCoverAfterSync(file.absolutePath,"https://example.org/remote.jpg"))
            assertFalse(isCustomPlaylistCover("https://example.org/"+PLAYLIST_COVER_DIRECTORY+"/example.jpg"))
        } finally {file.delete();directory.delete();root.delete()}
    }
}
