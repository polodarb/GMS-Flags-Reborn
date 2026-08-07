package ua.polodarb.xposed.entry

import android.app.Application
import android.content.Context
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.util.concurrent.atomic.AtomicBoolean
import ua.polodarb.xposed.logging.XposedLogger

internal class ApplicationLifecycleHook {
    fun install(
        loadPackage: XC_LoadPackage.LoadPackageParam,
        onCreate: (context: Context, classLoader: ClassLoader) -> Unit,
    ): XC_MethodHook.Unhook? {
        val applicationClass = XposedHelpers.findClass(
            "android.app.Application",
            loadPackage.classLoader,
        )
        val isLaunched = AtomicBoolean(false)

        return runCatching {
            XposedHelpers.findAndHookMethod(
                applicationClass,
                "onCreate",
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        if (!isLaunched.compareAndSet(false, true)) return

                        val application = param.thisObject as Application
                        val classLoader = application.javaClass.classLoader
                            ?: loadPackage.classLoader
                        onCreate(application.applicationContext, classLoader)
                    }
                },
            )
        }.onFailure { error ->
            XposedLogger.logE(
                "Failed to hook Application.onCreate in ${loadPackage.packageName}",
                error,
            )
        }.getOrNull()
    }
}
