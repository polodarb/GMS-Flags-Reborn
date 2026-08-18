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
import ua.polodarb.xposed.info.needle.HideViewTarget
import ua.polodarb.xposed.info.needle.NeedleHideView
import ua.polodarb.xposed.info.needle.NeedleImageOverlay
import ua.polodarb.xposed.info.needle.NeedleImageOverlayGeometry
import ua.polodarb.xposed.info.needle.NeedleOverlayTint
import ua.polodarb.xposed.info.needle.OverlayTintKind
import ua.polodarb.xposed.logging.XposedLogger
import java.util.WeakHashMap
import java.util.concurrent.atomic.AtomicBoolean

private const val MAX_DESCENDANTS_SCANNED = 64

/** One thing a recipe does to a resolved [android.view.View] each time it draws - draw an overlay,
 * hide descendants, etc. Keyed into the dispatcher by the anchor view's resource id. */
internal interface ResolvedViewEffect {
    val recipeId: String
    val resourceName: String
    val resourcePackage: String
    fun applyOn(canvas: Canvas, view: View, systemContext: Context?)
}

internal class ResolvedImageOverlay(
    override val recipeId: String,
    override val resourceName: String,
    override val resourcePackage: String,
    private val bitmap: Bitmap,
    private val overlay: NeedleImageOverlay,
) : ResolvedViewEffect {
    private val disabled = AtomicBoolean(false)
    private var tintApplied = false
    private var appliedTintColor: Int? = null
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
        alpha = (overlay.alpha * 255f).toInt().coerceIn(0, 255)
    }
    private val dest = RectF()

    override fun applyOn(canvas: Canvas, view: View, systemContext: Context?) {
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
        while (queue.isNotEmpty() && scanned < MAX_DESCENDANTS_SCANNED) {
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

/** Sets matching visible descendants of the anchor view to [View.INVISIBLE] while live, caching each
 * one's original visibility so a disabled recipe restores it. Re-checked every draw so a descendant
 * re-shown on view reuse (e.g. a language switch) is hidden again. */
internal class ResolvedHideView(
    override val recipeId: String,
    override val resourceName: String,
    override val resourcePackage: String,
    private val hideView: NeedleHideView,
) : ResolvedViewEffect {
    private val disabled = AtomicBoolean(false)
    private val hidden = WeakHashMap<View, Int>()
    private var targetIds: IntArray? = null
    private var idsResolved = false

    override fun applyOn(canvas: Canvas, view: View, systemContext: Context?) {
        if (disabled.get()) return
        try {
            when (hideView.target) {
                HideViewTarget.TEXT_LABELS ->
                    hideMatching(view) { it is TextView && !it.text.isNullOrEmpty() }
                HideViewTarget.RESOURCE_IDS -> {
                    val ids = resolvedIds(view)
                    if (ids.isEmpty()) return
                    hideMatching(view) { it.id != View.NO_ID && ids.contains(it.id) }
                }
            }
        } catch (t: Throwable) {
            disabled.set(true)
            restore()
            XposedLogger.logE("Needle: hide-view '$recipeId' failed; disabling it until the process restarts", t)
        }
    }

    private fun resolvedIds(view: View): IntArray {
        if (!idsResolved) {
            idsResolved = true
            val resources = view.resources
            val ids = hideView.resourceNames.mapNotNull { entry ->
                val pkg = entry.packageName?.takeIf { it.isNotBlank() } ?: view.context.packageName
                runCatching { resources.getIdentifier(entry.name, "id", pkg) }.getOrDefault(0).takeIf { it != 0 }
            }
            if (ids.isEmpty()) {
                XposedLogger.logW(
                    "Needle: hide-view '$recipeId' resolved no view ids from ${hideView.resourceNames.map { it.name }}",
                )
            }
            targetIds = ids.toIntArray()
        }
        return targetIds ?: IntArray(0)
    }

    private inline fun hideMatching(root: View, crossinline match: (View) -> Boolean) {
        if (root !is ViewGroup) return
        val queue = ArrayDeque<View>()
        for (i in 0 until root.childCount) queue.add(root.getChildAt(i))
        var scanned = 0
        while (queue.isNotEmpty() && scanned < MAX_DESCENDANTS_SCANNED) {
            val next = queue.removeFirst()
            scanned++
            if (next.visibility != View.VISIBLE) continue
            if (match(next)) {
                hidden[next] = next.visibility
                next.visibility = View.INVISIBLE
                continue
            }
            if (next is ViewGroup) for (i in 0 until next.childCount) queue.add(next.getChildAt(i))
        }
    }

    private fun restore() {
        for ((view, visibility) in hidden) runCatching { view.visibility = visibility }
        hidden.clear()
    }
}

internal class NeedleViewEffectDispatcher(
    private val effects: List<ResolvedViewEffect>,
    private val systemContext: Context?,
) : XC_MethodHook() {

    private val effectsByViewId = SparseArray<MutableList<ResolvedViewEffect>>()
    private val resolved = AtomicBoolean(false)

    override fun afterHookedMethod(param: MethodHookParam) {
        val view = param.thisObject as? View ?: return
        if (resolved.compareAndSet(false, true)) resolveIds(view.resources)
        val id = view.id
        if (id == View.NO_ID) return
        val bucket = effectsByViewId.get(id) ?: return
        val canvas = param.args.getOrNull(0) as? Canvas ?: return
        for (index in bucket.indices) bucket[index].applyOn(canvas, view, systemContext)
    }

    private fun resolveIds(resources: Resources) {
        effects.forEach { effect ->
            val id = runCatching {
                resources.getIdentifier(effect.resourceName, "id", effect.resourcePackage)
            }.getOrDefault(0)
            if (id == 0) {
                XposedLogger.logW(
                    "Needle: recipe '${effect.recipeId}' resource ${effect.resourcePackage}:id/" +
                        "${effect.resourceName} not found in the loaded app, skipping",
                )
                return@forEach
            }
            val bucket = effectsByViewId.get(id)
                ?: mutableListOf<ResolvedViewEffect>().also { effectsByViewId.put(id, it) }
            bucket.add(effect)
        }
        for (index in 0 until effectsByViewId.size()) {
            effectsByViewId.valueAt(index).sortBy { it.recipeId }
        }
    }
}
