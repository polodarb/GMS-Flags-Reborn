package ua.polodarb.xposed.hook.strategy.mendel

import android.content.pm.ApplicationInfo
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.io.File
import org.luckypray.dexkit.DexKitBridge
import ua.polodarb.xposed.diagnostics.SqliteHookDiagnostics
import ua.polodarb.xposed.info.HookDiagnosticContract
import ua.polodarb.xposed.info.XposedConstants
import ua.polodarb.xposed.info.XposedTargets
import ua.polodarb.xposed.logging.XposedLogger
import ua.polodarb.xposed.runtime.ModuleNativeLibraryLoader
import ua.polodarb.xposed.store.RuntimeFlagOverrideStore

internal object MendelRuntimeHookInstaller {
    fun supports(packageName: String, processName: String?): Boolean =
        packageName == processName && XposedTargets.isMendelApplication(packageName)

    fun install(
        runtimeDirectory: File,
        lpparam: XC_LoadPackage.LoadPackageParam,
        classLoader: ClassLoader,
        moduleApkPath: String?,
    ): SqliteHookDiagnostics? {
        val apkPath = lpparam.appInfo?.sourceDir ?: run {
            XposedLogger.logE("No sourceDir for ${lpparam.packageName}")
            return null
        }

        val overrideStore = RuntimeFlagOverrideStore(
            File(runtimeDirectory, XposedConstants.RUNTIME_OVERRIDES_DB_FILE_NAME),
        )
        val diagnostics = SqliteHookDiagnostics(
            databaseFile = File(
                runtimeDirectory,
                XposedConstants.HOOK_DIAGNOSTICS_DB_FILE_NAME,
            ),
            packageName = lpparam.packageName,
            processName = lpparam.processName,
            versionCode = readLongVersionCode(lpparam.appInfo),
        )
        diagnostics.start(overrideStore.overrideCount())

        if (File(runtimeDirectory, XposedConstants.OVERRIDES_PAUSED_FILE_NAME).isFile) {
            diagnostics.paused()
            XposedLogger.logI(
                "Skipping Mendel flag override hook: overrides are paused for ${lpparam.packageName}"
            )
            return diagnostics
        }
        if (!overrideStore.hasOverrides()) {
            diagnostics.noOverrides()
            XposedLogger.logI(
                "Skipping Mendel flag override hook: no overrides for ${lpparam.packageName}"
            )
            return diagnostics
        }

        runCatching {
            ModuleNativeLibraryLoader.load(
                moduleApkPath = requireNotNull(moduleApkPath) {
                    "Xposed module path is unavailable; initZygote was not called"
                },
                libraryName = XposedConstants.DEXKIT_LIBRARY_NAME,
            )
            val strategy = MendelFlagOverrideStrategy(
                packageName = lpparam.packageName,
                runtimeClassLoader = classLoader,
                overrideStore = overrideStore,
                diagnostics = diagnostics,
            )
            DexKitBridge.create(apkPath).use(strategy::install)
        }.onFailure { error ->
            val reason = error.message ?: error.javaClass.simpleName
            diagnostics.strategyFailed(HookDiagnosticContract.STRATEGY_MENDEL, reason)
            diagnostics.failure(reason)
            XposedLogger.logE(
                "Failed to install Mendel flag override hook for ${lpparam.packageName}",
                error,
            )
        }

        return diagnostics
    }

    private fun readLongVersionCode(appInfo: ApplicationInfo): Long = runCatching {
        XposedHelpers.getLongField(appInfo, LONG_VERSION_CODE_FIELD)
    }.getOrDefault(0L)

    private const val LONG_VERSION_CODE_FIELD = "longVersionCode"
}
