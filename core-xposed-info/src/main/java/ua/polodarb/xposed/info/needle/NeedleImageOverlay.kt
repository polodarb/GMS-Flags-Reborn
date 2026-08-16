package ua.polodarb.xposed.info.needle

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class OverlayGravity {
    TOP_LEFT, TOP, TOP_RIGHT,
    LEFT, CENTER, RIGHT,
    BOTTOM_LEFT, BOTTOM, BOTTOM_RIGHT,
}

enum class OverlayImageFormat { PNG, WEBP }

@Serializable
data class NeedleImageOverlay(
    @SerialName("image_base64") val imageBase64: String,
    val gravity: OverlayGravity = OverlayGravity.CENTER,
    @SerialName("offset_x_dp") val offsetXDp: Int = 0,
    @SerialName("offset_y_dp") val offsetYDp: Int = 0,
    @SerialName("width_dp") val widthDp: Int,
    @SerialName("height_dp") val heightDp: Int,
    val alpha: Float = 1f,
)

object NeedleImageOverlayLimits {
    const val MAX_IMAGE_BYTES = 96 * 1024
    const val MAX_PAYLOAD_BYTES = 160 * 1024
    const val MIN_SOURCE_DIMENSION_PX = 1
    const val MAX_SOURCE_DIMENSION_PX = 512
    const val MIN_RENDER_DP = 1
    const val MAX_RENDER_DP = 400
    const val MAX_OFFSET_DP = 400

    private val PNG_MAGIC = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)

    fun detectFormat(bytes: ByteArray): OverlayImageFormat? = when {
        hasPngMagic(bytes) -> OverlayImageFormat.PNG
        hasWebpMagic(bytes) -> OverlayImageFormat.WEBP
        else -> null
    }

    private fun hasPngMagic(bytes: ByteArray): Boolean =
        bytes.size >= PNG_MAGIC.size && PNG_MAGIC.indices.all { bytes[it] == PNG_MAGIC[it] }

    private fun hasWebpMagic(bytes: ByteArray): Boolean =
        bytes.size >= 12 &&
            bytes[0] == 'R'.code.toByte() && bytes[1] == 'I'.code.toByte() &&
            bytes[2] == 'F'.code.toByte() && bytes[3] == 'F'.code.toByte() &&
            bytes[8] == 'W'.code.toByte() && bytes[9] == 'E'.code.toByte() &&
            bytes[10] == 'B'.code.toByte() && bytes[11] == 'P'.code.toByte()

    fun isRenderDpInRange(value: Int): Boolean = value in MIN_RENDER_DP..MAX_RENDER_DP

    fun isOffsetDpInRange(value: Int): Boolean = value in -MAX_OFFSET_DP..MAX_OFFSET_DP

    fun isSourceDimensionInRange(value: Int): Boolean =
        value in MIN_SOURCE_DIMENSION_PX..MAX_SOURCE_DIMENSION_PX

    fun isAlphaInRange(value: Float): Boolean = value.isFinite() && value in 0f..1f
}

data class OverlayRect(val left: Float, val top: Float, val right: Float, val bottom: Float)

object NeedleImageOverlayGeometry {
    fun destRect(
        viewWidthPx: Int,
        viewHeightPx: Int,
        density: Float,
        overlay: NeedleImageOverlay,
    ): OverlayRect {
        val widthPx = overlay.widthDp * density
        val heightPx = overlay.heightDp * density
        val offsetXPx = overlay.offsetXDp * density
        val offsetYPx = overlay.offsetYDp * density

        val left = when (overlay.gravity) {
            OverlayGravity.TOP_LEFT, OverlayGravity.LEFT, OverlayGravity.BOTTOM_LEFT -> 0f
            OverlayGravity.TOP, OverlayGravity.CENTER, OverlayGravity.BOTTOM -> (viewWidthPx - widthPx) / 2f
            OverlayGravity.TOP_RIGHT, OverlayGravity.RIGHT, OverlayGravity.BOTTOM_RIGHT -> viewWidthPx - widthPx
        } + offsetXPx

        val top = when (overlay.gravity) {
            OverlayGravity.TOP_LEFT, OverlayGravity.TOP, OverlayGravity.TOP_RIGHT -> 0f
            OverlayGravity.LEFT, OverlayGravity.CENTER, OverlayGravity.RIGHT -> (viewHeightPx - heightPx) / 2f
            OverlayGravity.BOTTOM_LEFT, OverlayGravity.BOTTOM, OverlayGravity.BOTTOM_RIGHT -> viewHeightPx - heightPx
        } + offsetYPx

        return OverlayRect(left, top, left + widthPx, top + heightPx)
    }
}
