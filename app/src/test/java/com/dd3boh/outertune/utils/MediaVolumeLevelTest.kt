package com.dd3boh.outertune.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class MediaVolumeLevelTest {
    @Test fun zeroAndMaximumMatchPhoneEndpoints() {
        assertEquals(0, mediaVolumePercent(0, 15))
        assertEquals(100, mediaVolumePercent(15, 15))
        assertEquals(0, mediaVolumeIndex(0f, 15))
        assertEquals(15, mediaVolumeIndex(100f, 15))
    }

    @Test fun everyDiscretePhoneLevelSurvivesPercentageRounding() {
        for (max in listOf(15, 25, 100)) for (index in 0..max) {
            assertEquals(index, mediaVolumeIndex(mediaVolumePercent(index, max).toFloat(), max))
        }
    }

    @Test fun valuesOutsideThePhoneRangeAreClamped() {
        assertEquals(100, mediaVolumePercent(20, 15))
        assertEquals(0, mediaVolumePercent(-2, 15))
        assertEquals(15, mediaVolumeIndex(120f, 15))
        assertEquals(0, mediaVolumeIndex(-2f, 15))
        assertEquals(0, mediaVolumePercent(1, 0))
        assertEquals(0, mediaVolumeIndex(100f, 0))
    }
}
