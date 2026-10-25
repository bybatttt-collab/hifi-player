package com.dd3boh.outertune.utils

import java.net.URLEncoder
import org.junit.Assert.*
import org.junit.Test

class RymFirstResultTest {
    private val album = "https://rateyourmusic.com/release/album/autechre/tri-repetae/"

    @Test fun unwrapsGoogleLinkWithUnicodeAndScope() {
        assertEquals(album, RymFirstResult.resultUrl("/url?q=${URLEncoder.encode(album, "UTF-8")}&sa=U", true))
        assertNull(RymFirstResult.resultUrl(album, false))
    }

    @Test fun ignoresUnrelatedAndUnsafeTargets() {
        listOf("javascript:alert(1)", "https://example.com/release/album/test/", "https://rateyourmusic.com.evil.example/release/x/", "https://evil@rateyourmusic.com/release/x/", "http://rateyourmusic.com/release/x/", "https://rateyourmusic.com/artist/autechre").forEach { assertNull(it, RymFirstResult.resultUrl(it, true)) }
    }

    @Test fun firstAlbumResultBeatsOtherSearchLinks() {
        val second = album.replace("tri-repetae", "elsewhere")
        assertEquals(album, RymFirstResult.firstLink("<a href='https://example.com'>Other</a><a href='$album'><h3>Tri Repetae</h3></a><a href='$second'><h3>Second</h3></a>", true))
        assertNull(RymFirstResult.firstLink("<html>Consent required</html>", true))
    }
}
