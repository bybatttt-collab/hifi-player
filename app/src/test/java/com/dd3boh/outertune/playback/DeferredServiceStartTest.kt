package com.dd3boh.outertune.playback

import org.junit.Assert.*
import org.junit.Test

class DeferredServiceStartTest {
    @Test fun lockedScreenRejectionIsRetriedOnlyAfterARequestWasDeferred() {
        var foreground = false
        var attempts = 0
        val service = DeferredServiceStart {
            attempts++
            if (!foreground) throw IllegalStateException("Background start denied")
        }
        assertTrue(service.retryPending())
        assertEquals(0, attempts)
        assertFalse(service.request())
        assertFalse(service.retryPending())
        foreground = true
        assertTrue(service.retryPending())
        assertEquals(3, attempts)
        assertTrue(service.retryPending())
        assertEquals(3, attempts)
    }

    @Test fun unrelatedErrorsAreNotHidden() {
        val service = DeferredServiceStart { throw UnsupportedOperationException() }
        assertThrows(UnsupportedOperationException::class.java) { service.request() }
    }
}
