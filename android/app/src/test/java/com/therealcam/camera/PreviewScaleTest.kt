package com.therealcam.camera

import org.junit.Assert.assertEquals
import org.junit.Test

class PreviewScaleTest {
    private fun scale(bw: Int, bh: Int, so: Int, vw: Int, vh: Int) = CameraController.previewScale(bw, bh, so, vw, vh)

    @Test
    fun matchingAspectIsUnchanged() {
        assertEquals(1f to 1f, scale(1440, 1080, 90, 1080, 1440))
        assertEquals(1f to 1f, scale(1440, 1080, 270, 1080, 1440))
    }

    @Test
    fun wideBufferInATallerViewIsCroppedNotSqueezed() {
        // 16:9 buffer in a 3:4 view: upright 9:16 content is taller, so y grows and top and bottom are cropped.
        val (sx, sy) = scale(1920, 1080, 270, 1080, 1440)
        assertEquals(1f, sx, 1e-4f)
        assertEquals((1080f / 1440) / (1080f / 1920), sy, 1e-4f)
    }

    @Test
    fun squareBufferIsCroppedAtTheSides() {
        // 1:1 content shown in 3:4 would be stretched vertically; widening x restores the circle.
        val (sx, sy) = scale(1080, 1080, 270, 1080, 1440)
        assertEquals(1f / (1080f / 1440), sx, 1e-4f)
        assertEquals(1f, sy, 1e-4f)
    }

    @Test
    fun unrotatedSensorUsesTheBufferAspectAsIs() {
        // Sensor orientation 0: 4:3 landscape content in a 3:4 portrait view.
        val (sx, sy) = scale(1440, 1080, 0, 1080, 1440)
        assertEquals((1440f / 1080) / (1080f / 1440), sx, 1e-4f)
        assertEquals(1f, sy, 1e-4f)
    }

    @Test
    fun unknownSizesAreIdentity() {
        assertEquals(1f to 1f, scale(1440, 1080, 90, 0, 0))
    }
}
