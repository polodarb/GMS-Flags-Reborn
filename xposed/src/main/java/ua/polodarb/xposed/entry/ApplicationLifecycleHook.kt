package ua.polodarb.xposed.entry

import android.content.Context
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.util.concurrent.atomic.AtomicBoolean
import ua.polodarb.xposed.logging.XposedLogger

internal class ApplicationLifecycleHook {
    fun install(
        loadPackage: XC_LoadPackage.LoadPackageParam,
        onLoad: (context: Context, classLoader: ClassLoader) -> Unit,
    ): XC_MethodHook.Unhook? = runCatching {
        val isLaunched = AtomicBoolean(false)

        XposedHelpers.findAndHookMethod(
            "android.app.ContextImpl",
            null,
            "createAppContext",
            "android.app.ActivityThread",
            "android.app.LoadedApk",
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    if (!isLaunched.compareAndSet(false, true)) return

                    val context = param.result as? Context ?: return
                    val classLoader = context.classLoader ?: loadPackage.classLoader

                    onLoad(context, classLoader)
                }
            }
        )
    }.onFailure { error ->
        XposedLogger.logE(
            "Failed to hook ContextImpl.createAppContext in ${loadPackage.packageName}",
            error,
        )
    }.getOrNull()
}
