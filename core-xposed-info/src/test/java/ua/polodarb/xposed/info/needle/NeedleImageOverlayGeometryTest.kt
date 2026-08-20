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
    fun `content anchor centers within the padded box`() {
        // View 400x200 inset by padding [l20,t10,r20,b40] -> content box (20,10,360,150).
        // icon 24dp @2x = 48px; centered in box: left=20+(360-48)/2=176, top=10+(150-48)/2=61.
        val rect = NeedleImageOverlayGeometry.destRectInBox(
            20f, 10f, 360f, 150f, 2f, overlay(OverlayGravity.CENTER),
        )
        assertEquals(176f, rect.left, 0.001f)
        assertEquals(61f, rect.top, 0.001f)
        assertEquals(224f, rect.right, 0.001f)
        assertEquals(109f, rect.bottom, 0.001f)
    }

    @Test
    fun `content box shorter at the bottom lifts a centered icon above the whole-view center`() {
        // A bottom-heavier inset (e.g. Gboard's spacebar label strip) makes the content-box center
        // sit higher than the raw-view center - which is exactly the fix for the off-centre overlay.
        val full = NeedleImageOverlayGeometry.destRect(400, 200, 2f, overlay(OverlayGravity.CENTER))
        val content = NeedleImageOverlayGeometry.destRectInBox(
            0f, 0f, 400f, 160f, 2f, overlay(OverlayGravity.CENTER),
        )
        org.junit.Assert.assertTrue(content.top < full.top)
    }

    @Test
    fun `whole-view destRect equals a full-view box`() {
        val direct = NeedleImageOverlayGeometry.destRect(400, 200, 2f, overlay(OverlayGravity.CENTER))
        val viaBox = NeedleImageOverlayGeometry.destRectInBox(0f, 0f, 400f, 200f, 2f, overlay(OverlayGravity.CENTER))
        assertEquals(direct.left, viaBox.left, 0.001f)
        assertEquals(direct.top, viaBox.top, 0.001f)
        assertEquals(direct.right, viaBox.right, 0.001f)
        assertEquals(direct.bottom, viaBox.bottom, 0.001f)
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
