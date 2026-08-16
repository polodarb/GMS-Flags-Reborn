package ua.polodarb.xposed.info.needle

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertEquals as assertEq
import org.junit.Test

class NeedleImageOverlayGeometryTest {

    private fun overlay(
        gravity: OverlayGravity,
        widthDp: Int = 24,
        heightDp: Int = 24,
        offsetXDp: Int = 0,
        offsetYDp: Int = 0,
    ) = NeedleImageOverlay(
        imageBase64 = "",
        gravity = gravity,
        offsetXDp = offsetXDp,
        offsetYDp = offsetYDp,
        widthDp = widthDp,
        heightDp = heightDp,
    )

    @Test
    fun `top-right anchors to the top-right corner`() {
        val rect = NeedleImageOverlayGeometry.destRect(400, 200, 2f, overlay(OverlayGravity.TOP_RIGHT))
        assertEquals(352f, rect.left, 0.001f)
        assertEquals(0f, rect.top, 0.001f)
        assertEquals(400f, rect.right, 0.001f)
        assertEquals(48f, rect.bottom, 0.001f)
    }

    @Test
    fun `center anchors to the middle`() {
        val rect = NeedleImageOverlayGeometry.destRect(400, 200, 2f, overlay(OverlayGravity.CENTER))
        assertEquals(176f, rect.left, 0.001f)
        assertEquals(76f, rect.top, 0.001f)
        assertEquals(224f, rect.right, 0.001f)
        assertEquals(124f, rect.bottom, 0.001f)
    }

    @Test
    fun `bottom-left honors offsets in both directions`() {
        val rect = NeedleImageOverlayGeometry.destRect(
            400, 200, 2f, overlay(OverlayGravity.BOTTOM_LEFT, offsetXDp = 10, offsetYDp = -5),
        )
        assertEquals(20f, rect.left, 0.001f)
        assertEquals(142f, rect.top, 0.001f)
        assertEquals(68f, rect.right, 0.001f)
        assertEquals(190f, rect.bottom, 0.001f)
    }

    @Test
    fun `png magic is detected`() {
        val png = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0, 0)
        assertEq(OverlayImageFormat.PNG, NeedleImageOverlayLimits.detectFormat(png))
    }

    @Test
    fun `webp magic is detected`() {
        val webp = "RIFF????WEBPVP8 ".toByteArray()
        assertEq(OverlayImageFormat.WEBP, NeedleImageOverlayLimits.detectFormat(webp))
    }

    @Test
    fun `unknown magic is not a supported format`() {
        assertNull(NeedleImageOverlayLimits.detectFormat("not an image".toByteArray()))
    }
}
