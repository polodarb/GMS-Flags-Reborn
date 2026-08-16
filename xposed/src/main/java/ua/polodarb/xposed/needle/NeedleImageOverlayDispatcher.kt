package ua.polodarb.xposed.needle

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.RectF
import android.util.SparseArray
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import de.robv.android.xposed.XC_MethodHook
import ua.polodarb.xposed.info.needle.NeedleImageOverlay
import ua.polodarb.xposed.info.needle.NeedleImageOverlayGeometry
import ua.polodarb.xposed.info.needle.NeedleOverlayTint
import ua.polodarb.xposed.info.needle.OverlayTintKind
import ua.polodarb.xposed.logging.XposedLogger
import java.util.concurrent.atomic.AtomicBoolean

private const val MAX_TINT_VIEWS_SCANNED = 64

internal class ResolvedImageOverlay(
    val recipeId: String,
    val resourceName: String,
    val resourcePackage: String,
    private val bitmap: Bitmap,
    private val overlay: NeedleImageOverlay,
) {
    private val disabled = AtomicBoolean(false)
    private var tintApplied = false
    private var appliedTintColor: Int? = null
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
        alpha = (overlay.alpha * 255f).toInt().coerceIn(0, 255)
    }
    private val dest = RectF()

    fun drawOn(canvas: Canvas, view: View, systemContext: Context?) {
        if (disabled.get()) return
        try {
            if (overlay.tint.isNotEmpty()) {
                val color = resolveTintColor(view, systemContext)
                if (!tintApplied || color != appliedTintColor) {
                    tintApplied = true
                    appliedTintColor = color
                    paint.colorFilter = color?.let { PorterDuffColorFilter(it, PorterDuff.Mode.SRC_IN) }
                    XposedLogger.logD(
                        "Needle: overlay '$recipeId' tint=${color?.let { String.format("#%08X", it) } ?: "none"}",
                    )
                }
            }
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

    private fun resolveTintColor(view: View, systemContext: Context?): Int? {
        overlay.tint.forEach { source ->
            val color = when (source.kind) {
                OverlayTintKind.FIRST_DESCENDANT_TEXT_COLOR -> findTextColor(view)
                OverlayTintKind.THEME_ATTRIBUTE -> resolveThemeAttribute(view, source.themeAttribute)
                OverlayTintKind.FIXED_ARGB -> NeedleOverlayTint.parseArgb(source.argb)
                OverlayTintKind.SYSTEM_NIGHT_MODE -> {
                    val night = isSystemNightMode(view, systemContext)
                    NeedleOverlayTint.parseArgb(if (night) source.darkArgb else source.lightArgb)
                }
            }
            if (color != null) return color
        }
        return null
    }

    private fun isSystemNightMode(view: View, systemContext: Context?): Boolean =
        (systemContext?.resources?.let { isNight(it) }) ?: isNight(view.resources)

    private fun isNight(resources: Resources): Boolean =
        (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

    private fun findTextColor(root: View): Int? {
        if (root is TextView) return root.currentTextColor
        if (root !is ViewGroup) return null
        val queue = ArrayDeque<View>()
        for (i in 0 until root.childCount) queue.add(root.getChildAt(i))
        var scanned = 0
        while (queue.isNotEmpty() && scanned < MAX_TINT_VIEWS_SCANNED) {
            val next = queue.removeFirst()
            scanned++
            if (next.visibility != View.VISIBLE) continue
            if (next is TextView) return next.currentTextColor
            if (next is ViewGroup) for (i in 0 until next.childCount) queue.add(next.getChildAt(i))
        }
        return null
    }

    private fun resolveThemeAttribute(view: View, attrName: String?): Int? {
        if (attrName.isNullOrBlank()) return null
        val context = view.context
        val attrId = runCatching {
            Resources.getSystem().getIdentifier(attrName, "attr", "android").takeIf { it != 0 }
                ?: context.resources.getIdentifier(attrName, "attr", context.packageName).takeIf { it != 0 }
        }.getOrNull() ?: return null
        val typedValue = TypedValue()
        if (!context.theme.resolveAttribute(attrId, typedValue, true)) return null
        return if (typedValue.type in TypedValue.TYPE_FIRST_COLOR_INT..TypedValue.TYPE_LAST_COLOR_INT) {
            typedValue.data
        } else {
            null
        }
    }
}

internal class NeedleImageOverlayDispatcher(
    private val overlays: List<ResolvedImageOverlay>,
    private val systemContext: Context?,
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
        for (index in bucket.indices) bucket[index].drawOn(canvas, view, systemContext)
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
