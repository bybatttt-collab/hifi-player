package com.dd3boh.outertune.ui.player

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import com.dd3boh.outertune.ui.theme.hifiAccent
import com.dd3boh.outertune.ui.theme.hifiColors
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class HifiPlayerFinishTest {
    private fun contrast(a: Color, b: Color): Float =
        (maxOf(a.luminance(), b.luminance()) + 0.05f) / (minOf(a.luminance(), b.luminance()) + 0.05f)

    @Test
    fun `silver keeps text and accent readable for every supported accent`() {
        for (hex in listOf("E8B45C", "B7DE73", "83C9DF", "C2A1E8", "FF9A85", "EDA7CF",
            "88AEFF", "72DCCB", "D7DE73", "FFBC7A", "B6B4FF", "CFD3D9")) {
            val base = hifiColors(hifiAccent(hex))
            val silver = hifiFinishColors(base, HifiPlayerFinish.SILVER)
            // The brushed material has both light and dark streaks, not one flat colour.
            for (panel in listOf(Color(0xFFA6ACB1), Color(0xFFD1D4D7), Color(0xFF9DA3A8), Color(0xFFC6C9CC))) {
                assertTrue("Text on silver ($hex)", contrast(silver.onSurface, panel) >= 4.5f)
                assertTrue("Accent on silver ($hex)", contrast(silver.primary, panel) >= 4.5f)
            }
            // Computing the local finish must not alter the app's global palette.
            assertEquals(hifiAccent(hex), base.primary)
        }
    }

    @Test
    fun `current and dark finishes preserve classic night and alternate palettes`() {
        for (night in listOf(false, true)) {
            val base = hifiColors(hifiAccent("72DCCB"), night = night)
            for (finish in listOf(HifiPlayerFinish.CURRENT, HifiPlayerFinish.BLACK_METAL, HifiPlayerFinish.BLACK_SATIN)) {
                assertSame(base, hifiFinishColors(base, finish))
            }
        }
    }

    @Test
    fun `hardware portrait releases the unused strip but keeps queue access and system insets`() {
        val original = playerQueueLayout(820.dp, 24.dp, landscape = false, twoPane = false,
            queueAsButton = false, songLoaded = true)
        val hardware = original.forHifiPortrait(24.dp, enabled = true)
        assertTrue(hardware.reserve < original.reserve)
        assertTrue(hardware.reserve >= 24.dp)
        assertTrue(hardware.queueButton)
        assertEquals(0.dp, hardware.sheetCollapsed)
        assertEquals(0.dp, hardware.sheetDismissed)
        assertEquals(false, hardware.showHandle)
    }

    @Test
    fun `other layouts keep the existing queue and clearance`() {
        for (landscape in listOf(false, true)) {
            for (height in listOf(320.dp, 820.dp)) {
                val original = playerQueueLayout(height, 24.dp, landscape = landscape, twoPane = false,
                    queueAsButton = false, songLoaded = true)
                assertSame(original, original.forHifiPortrait(24.dp, enabled = false))
            }
        }
    }
}
