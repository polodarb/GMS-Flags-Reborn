package ua.polodarb.xposed.info.needle

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class OverlayGravity {
    TOP_LEFT, TOP, TOP_RIGHT,
    LEFT, CENTER, RIGHT,
    BOTTOM_LEFT, BOTTOM, BOTTOM_RIGHT,
}

enum class OverlayImageFormat { PNG, WEBP }

/** How one link in an overlay's tint chain derives a colour. Resolved in list order; the first that
 * yields a colour wins, and a monochrome image is recoloured with it via PorterDuff SRC_IN. */
enum class OverlayTintKind { FIRST_DESCENDANT_TEXT_COLOR, THEME_ATTRIBUTE, FIXED_ARGB, SYSTEM_NIGHT_MODE }

@Serializable
data class OverlayTintSource(
    val kind: OverlayTintKind,
    /** Only for [OverlayTintKind.THEME_ATTRIBUTE] - an Android theme attr name resolved against the
     * target view's own theme (e.g. "colorOnSurface"), NOT a Compose Material token. */
    @SerialName("theme_attribute") val themeAttribute: String? = null,
    /** Only for [OverlayTintKind.FIXED_ARGB] - "#AARRGGBB" or "AARRGGBB". */
    val argb: String? = null,
    /** [OverlayTintKind.SYSTEM_NIGHT_MODE] only - the colour used when the device is in light/day mode. */
    @SerialName("light_argb") val lightArgb: String? = null,
    /** [OverlayTintKind.SYSTEM_NIGHT_MODE] only - the colour used when the device is in dark/night mode. */
    @SerialName("dark_argb") val darkArgb: String? = null,
)

@Serializable
data class NeedleImageOverlay(
    @SerialName("image_base64") val imageBase64: String,
    val gravity: OverlayGravity = OverlayGravity.CENTER,
    @SerialName("offset_x_dp") val offsetXDp: Int = 0,
    @SerialName("offset_y_dp") val offsetYDp: Int = 0,
    @SerialName("width_dp") val widthDp: Int,
    @SerialName("height_dp") val heightDp: Int,
    val alpha: Float = 1f,
    /** Optional recolour chain; empty means the image is drawn as-is. */
    val tint: List<OverlayTintSource> = emptyList(),
    /** Convenience shortcut for the common "draw an icon and blank the view's own text" case: when
     * true the engine also runs a [NeedleHideView] with [HideViewTarget.TEXT_LABELS] on the same view.
     * Equivalent to publishing a separate HIDE_VIEW recipe; requires the same
     * [NeedleCapabilities.VIEW_HIDE_DESCENDANT] capability. */
    @SerialName("hide_descendant_text_labels") val hideDescendantTextLabels: Boolean = false,
)

/** What a [NeedleHideView] effect targets, relative to the resolved [SelectorKind.VIEW_RESOURCE_ID]
 * anchor view whose draw is hooked. */
enum class HideViewTarget {
    /** Every visible descendant TextView with non-empty text (no view id needed - robust to renames). */
    TEXT_LABELS,

    /** Only descendants whose view id matches one of [NeedleHideView.resourceNames]. */
    RESOURCE_IDS,
}

@Serializable
data class HideViewResource(
    val name: String,
    /** Resolved against this package; defaults to the anchor view's own package when null. */
    @SerialName("package") val packageName: String? = null,
)

/** The [EffectKind.HIDE_VIEW] expression: sets matching visible descendants of the anchor view to
 * [android.view.View.INVISIBLE] while the recipe is live (restored if it disables), re-checked each
 * draw so a view re-shown on reuse is hidden again. To hide a whole view, anchor on its parent and
 * name it in [resourceNames]. Requires [NeedleCapabilities.VIEW_HIDE_DESCENDANT]. */
@Serializable
data class NeedleHideView(
    val target: HideViewTarget = HideViewTarget.TEXT_LABELS,
    @SerialName("resource_names") val resourceNames: List<HideViewResource> = emptyList(),
)

object NeedleOverlayTint {
    /** Theme attrs a recipe may resolve - all standard colour attrs, so a non-colour attr can never
     * be coerced into a bogus tint. */
    val SUPPORTED_THEME_ATTRIBUTES: Set<String> = setOf(
        "colorOnSurface",
        "colorOnSurfaceVariant",
        "colorPrimary",
        "colorOnPrimary",
        "colorSecondary",
        "colorOnBackground",
        "textColorPrimary",
        "textColorSecondary",
    )

    /** Parses "#AARRGGBB"/"AARRGGBB" (and "#RRGGBB"/"RRGGBB", assumed opaque) to a colour int, or null. */
    fun parseArgb(value: String?): Int? {
        val hex = value?.trim()?.removePrefix("#") ?: return null
        if (!hex.all { it.isDigit() || it in 'a'..'f' || it in 'A'..'F' }) return null
        val normalized = when (hex.length) {
            6 -> "FF$hex"
            8 -> hex
            else -> return null
        }
        return normalized.toLongOrNull(16)?.toInt()
    }

    fun isWellFormed(source: OverlayTintSource): Boolean = when (source.kind) {
        OverlayTintKind.FIRST_DESCENDANT_TEXT_COLOR -> true
        OverlayTintKind.THEME_ATTRIBUTE -> source.themeAttribute in SUPPORTED_THEME_ATTRIBUTES
        OverlayTintKind.FIXED_ARGB -> parseArgb(source.argb) != null
        OverlayTintKind.SYSTEM_NIGHT_MODE -> parseArgb(source.lightArgb) != null && parseArgb(source.darkArgb) != null
    }
}

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
