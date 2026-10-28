package com.dd3boh.outertune.ui.theme

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HifiTypographyTest {
    @Test fun shortLatinTitlesCanUseDigitalFont() {
        assertTrue(usesHifiDisplayFont("Cellophane"))
        assertTrue(usesHifiDisplayFont("Cerca album su RYM"))
        assertTrue(usesHifiDisplayFont("Björk"))
    }

    @Test fun longNamesKeepReadableDeviceFont() {
        assertFalse(usesHifiDisplayFont("The Complete Science Fiction Sessions"))
    }

    @Test fun nonLatinTitlesKeepReadableDeviceFont() {
        assertFalse(usesHifiDisplayFont("無人テレビの設計図"))
        assertFalse(usesHifiDisplayFont("موسيقى"))
    }
}
