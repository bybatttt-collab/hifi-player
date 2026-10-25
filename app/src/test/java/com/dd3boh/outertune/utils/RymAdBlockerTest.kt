package com.dd3boh.outertune.utils

import org.junit.Assert.*
import org.junit.Test

class RymAdBlockerTest {
    @Test fun conservativeParserKeepsExceptionsAndConditionalRulesOut() {
        val rules = RymAdBlocker.networkDomains(sequenceOf(
            "||ads.example^", "||images.example^\$third-party",
            "@@||allowed.example^", "||cdn.example^\$domain=elsewhere",
        ))
        assertEquals(setOf("ads.example", "images.example"), rules)
    }
    @Test fun matchesDomainBoundariesAndNeverBlocksRym() {
        val rules = setOf("ads.example", "rateyourmusic.com")
        assertTrue(RymAdBlocker.blockedHost("a.b.ads.example", rules))
        assertFalse(RymAdBlocker.blockedHost("notads.example", rules))
        assertFalse(RymAdBlocker.blockedHost("rateyourmusic.com", rules))
        assertFalse(RymAdBlocker.blockedHost(null, rules))
    }
}
