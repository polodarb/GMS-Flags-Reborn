package ua.polodarb.xposed.needle

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.SparseArray
import android.view.View
import de.robv.android.xposed.XC_MethodHook
import ua.polodarb.xposed.info.needle.NeedleImageOverlay
import ua.polodarb.xposed.info.needle.NeedleImageOverlayGeometry
import ua.polodarb.xposed.logging.XposedLogger
import java.util.concurrent.atomic.AtomicBoolean

internal class ResolvedImageOverlay(
    val recipeId: String,
    val resourceName: String,
    val resourcePackage: String,
    private val bitmap: Bitmap,
    private val overlay: NeedleImageOverlay,
) {
    private val disabled = AtomicBoolean(false)
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
        alpha = (overlay.alpha * 255f).toInt().coerceIn(0, 255)
    }
    private val dest = RectF()

    fun drawOn(canvas: Canvas, view: View) {
        if (disabled.get()) return
        try {
            val density = view.resources.displayMetrics.density
            val rect = NeedleImageOverlayGeometry.destRect(view.width, view.height, density, overlay)
            dest.set(rect.left, rect.top, rect.right, rect.bottom)
            val checkpoint = canvas.save()
            try {
                canvas.drawBitmap(bitmap, null, dest, paint)
            } finally {
                canvas.restoreToCount(checkpoint)
            }
        } catch (t: Throwable) {
            disabled.set(true)
            XposedLogger.logE("Needle: overlay '$recipeId' draw failed; disabling it until the process restarts", t)
        }
    }
}

internal class NeedleImageOverlayDispatcher(
    private val overlays: List<ResolvedImageOverlay>,
) : XC_MethodHook() {

    private val overlaysByViewId = SparseArray<MutableList<ResolvedImageOverlay>>()
    private val resolved = AtomicBoolean(false)

    override fun afterHookedMethod(param: MethodHookParam) {
        val view = param.thisObject as? View ?: return
        if (resolved.compareAndSet(false, true)) resolveIds(view.resources)
        val id = view.id
        if (id == View.NO_ID) return
        val bucket = overlaysByViewId.get(id) ?: return
        val canvas = param.args.getOrNull(0) as? Canvas ?: return
        for (index in bucket.indices) bucket[index].drawOn(canvas, view)
    }

    private fun resolveIds(resources: Resources) {
        overlays.forEach { overlay ->
            val id = runCatching {
                resources.getIdentifier(overlay.resourceName, "id", overlay.resourcePackage)
            }.getOrDefault(0)
            if (id == 0) {
                XposedLogger.logW(
                    "Needle: overlay '${overlay.recipeId}' resource ${overlay.resourcePackage}:id/" +
                        "${overlay.resourceName} not found in the loaded app, skipping",
                )
                return@forEach
            }
            val bucket = overlaysByViewId.get(id)
                ?: mutableListOf<ResolvedImageOverlay>().also { overlaysByViewId.put(id, it) }
            bucket.add(overlay)
        }
        for (index in 0 until overlaysByViewId.size()) {
            overlaysByViewId.valueAt(index).sortBy { it.recipeId }
        }
    }
}
