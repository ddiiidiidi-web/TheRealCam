package com.therealcam.sensors

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SteadyFilterTest {
    private val ms = 1_000_000L

    @Test
    fun becomesSteadyOnlyAfterAHoldBelowTheThreshold() {
        val f = SteadyFilter()
        assertFalse(f.update(0.005, 0))
        assertFalse(f.update(0.005, 500 * ms))
        assertTrue(f.update(0.005, 700 * ms))
    }

    @Test
    fun windAroundTheThresholdDoesNotFlicker() {
        val f = SteadyFilter()
        f.update(0.005, 0)
        f.update(0.005, 1000 * ms)
        // Gusts push the RMS just over the threshold and back, every 100 ms for 10 s.
        var t = 1000 * ms
        repeat(100) { i ->
            t += 100 * ms
            assertTrue(f.update(if (i % 3 == 0) 0.01 else 0.018, t))
        }
    }

    @Test
    fun pickingThePhoneUpEndsSteadyAtOnce() {
        val f = SteadyFilter()
        f.update(0.005, 0)
        f.update(0.005, 1000 * ms)
        assertFalse(f.update(0.08, 1100 * ms))
    }

    @Test
    fun sustainedMotionAboveTheThresholdEndsSteady() {
        val f = SteadyFilter()
        f.update(0.005, 0)
        f.update(0.005, 1000 * ms)
        assertTrue(f.update(0.02, 1100 * ms))
        assertTrue(f.update(0.02, 2900 * ms))
        assertFalse(f.update(0.02, 3100 * ms))
    }

    @Test
    fun noGyroIsNeverSteady() {
        val f = SteadyFilter()
        assertFalse(f.update(Double.NaN, 0))
    }
}
