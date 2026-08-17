package ua.polodarb.xposed.needle

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import ua.polodarb.xposed.info.needle.NeedleImageOverlayLimits

internal object NeedleImageOverlayDecoder {

    sealed interface Result {
        data class Success(val bitmap: Bitmap) : Result
        data class Failure(val reason: String) : Result
    }

    fun decode(bytes: ByteArray): Result {
        if (bytes.isEmpty()) return Result.Failure("empty image")
        if (bytes.size > NeedleImageOverlayLimits.MAX_IMAGE_BYTES) {
            return Result.Failure("image is ${bytes.size} bytes, exceeds ${NeedleImageOverlayLimits.MAX_IMAGE_BYTES}")
        }
        if (NeedleImageOverlayLimits.detectFormat(bytes) == null) {
            return Result.Failure("image is not PNG or WEBP")
        }

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        val boundsWidth = bounds.outWidth
        val boundsHeight = bounds.outHeight
        if (boundsWidth <= 0 || boundsHeight <= 0) {
            return Result.Failure("image bounds could not be read")
        }
        if (!NeedleImageOverlayLimits.isSourceDimensionInRange(boundsWidth) ||
            !NeedleImageOverlayLimits.isSourceDimensionInRange(boundsHeight)
        ) {
            return Result.Failure(
                "image is ${boundsWidth}x$boundsHeight, exceeds ${NeedleImageOverlayLimits.MAX_SOURCE_DIMENSION_PX}px per side",
            )
        }

        val options = BitmapFactory.Options().apply { inPreferredConfig = Bitmap.Config.ARGB_8888 }
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
            ?: return Result.Failure("image could not be decoded")
        if (!NeedleImageOverlayLimits.isSourceDimensionInRange(bitmap.width) ||
            !NeedleImageOverlayLimits.isSourceDimensionInRange(bitmap.height)
        ) {
            bitmap.recycle()
            return Result.Failure(
                "decoded bitmap is ${bitmap.width}x${bitmap.height}, exceeds ${NeedleImageOverlayLimits.MAX_SOURCE_DIMENSION_PX}px per side",
            )
        }
        return Result.Success(bitmap)
    }
}
