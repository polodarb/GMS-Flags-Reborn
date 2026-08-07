package ua.polodarb.xposed.experimental

import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.io.File

internal object ExperimentalRuntimeHookInstaller {
    fun supports(packageName: String, processName: String?): Boolean = false

    fun install(
        runtimeDirectory: File,
        lpparam: XC_LoadPackage.LoadPackageParam,
        classLoader: ClassLoader,
        moduleApkPath: String?,
    ) = Unit
}
