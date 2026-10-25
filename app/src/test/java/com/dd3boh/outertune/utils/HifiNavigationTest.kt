package com.dd3boh.outertune.utils

import com.dd3boh.outertune.constants.DEFAULT_ENABLED_TABS
import com.dd3boh.outertune.ui.screens.Screens
import org.junit.Assert.*
import org.junit.Test

class HifiNavigationTest {
    @Test fun fiveTabsInRequestedOrder() {
        assertEquals(listOf("home", "songs", "albums", "library", "rym"), Screens.getScreens(DEFAULT_ENABLED_TABS).map { it.route })
    }

    @Test fun remotePlaylistPinSurvivesDifferentLocalIds() {
        assertEquals(playlistPinKey("before", "PL42"), playlistPinKey("after", "PL42"))
        assertNotEquals(playlistPinKey("PL42", null), playlistPinKey("", "PL42"))
    }
}
