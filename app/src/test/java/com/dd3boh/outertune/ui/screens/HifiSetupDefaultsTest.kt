package com.dd3boh.outertune.ui.screens

import androidx.datastore.preferences.core.mutablePreferencesOf
import com.dd3boh.outertune.constants.*
import org.junit.Assert.*
import org.junit.Test

class HifiSetupDefaultsTest {
    @Test fun newInstallFinishesWithoutOldQuestionsOrTour() {
        val prefs = mutablePreferencesOf()
        markFirstSetup(prefs)
        completeHifiSetup(prefs, updates = true, versionCode = 6)
        assertEquals(true, prefs[UpdateCheckEnabledKey])
        assertEquals(false, prefs[PollsEnabledKey])
        assertEquals(false, prefs[AnnouncementsEnabledKey])
        assertEquals(false, prefs[UsageCountEnabledKey])
        assertEquals(6, prefs[WalkthroughSeenVersionKey])
        assertEquals(true, prefs[AccountNoticeSeenKey])
        assertTrue(com.dd3boh.outertune.engine.SimilarSources.asked(prefs[SimilarSourceKey], prefs[SimilarFromLastFmKey]))
        assertFalse(catchUpOwed(prefs[UpdateCheckEnabledKey], prefs[PollsEnabledKey],
            prefs[AnnouncementsEnabledKey], prefs[UsageCountEnabledKey], lastFmOwed = false))
    }
    @Test fun decliningUpdatesIsKeptAndExistingAccountIsUntouched() {
        val prefs = mutablePreferencesOf(InnerTubeCookieKey to "test-session-not-a-real-cookie",
            AccountNameKey to "Test account", PollsEnabledKey to true)
        completeHifiSetup(prefs, updates = false, versionCode = 6)
        assertEquals(false, prefs[UpdateCheckEnabledKey])
        assertEquals("test-session-not-a-real-cookie", prefs[InnerTubeCookieKey])
        assertEquals("Test account", prefs[AccountNameKey])
        assertEquals(true, prefs[PollsEnabledKey])
        assertNull(prefs[OobeStatusKey]) // only the screen marks completion after these answers are saved
    }
}
