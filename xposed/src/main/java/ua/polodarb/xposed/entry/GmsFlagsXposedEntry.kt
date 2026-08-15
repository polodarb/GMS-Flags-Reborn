package ua.polodarb.xposed.entry

import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.callbacks.XC_LoadPackage
import ua.polodarb.xposed.BuildConfig
import ua.polodarb.xposed.hook.RuntimeFlagOverrideHook
import ua.polodarb.xposed.info.XposedTargets
import ua.polodarb.xposed.logging.XposedLogger
import ua.polodarb.xposed.runtime.ModuleNativeLibraryLoader
import ua.polodarb.xposed.runtime.XposedRuntimeDirectory
import java.io.File
import ua.polodarb.xposed.diagnostics.SqliteHookDiagnostics
import ua.polodarb.xposed.info.XposedConstants
import ua.polodarb.xposed.info.BuildConfig as XposedInfoBuildConfig
import ua.polodarb.xposed.hook.strategy.mendel.MendelRuntimeHookInstaller
import ua.polodarb.xposed.needle.NeedleEngine

@Suppress("unused")
class GmsFlagsXposedEntry : IXposedHookLoadPackage, IXposedHookZygoteInit {
    private val runtimeDirectory = XposedRuntimeDirectory()
    private val applicationLifecycleHook = ApplicationLifecycleHook()

    init {
        XposedLogger.logD("Module initialized")
    }

    override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam?) {
        moduleApkPath = startupParam?.modulePath
        XposedLogger.logD("Module path received: $moduleApkPath")
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam?) {
        if (lpparam == null || !lpparam.isSupportedTarget()) return

        XposedLogger.logD("Received ${lpparam.packageName} (${lpparam.processName})")

        NeedleEngine.tryInstall(
            lpparam = lpparam,
            classLoader = lpparam.classLoader,
            moduleApkPath = moduleApkPath,
            trustedPublicKeyBase64 = XposedInfoBuildConfig.NEEDLE_TRUSTED_PUBLIC_KEY_BASE64,
        )

        if (MendelRuntimeHookInstaller.supports(lpparam.packageName, lpparam.processName)) {
            val targetRuntimeDirectory = File(
                lpparam.appInfo.dataDir,
                XposedConstants.XPOSED_DIR,
            )
            XposedLogger.initFileLogging(targetRuntimeDirectory, lpparam)
            XposedLogger.logI(
                "Installing Mendel runtime hook before Application and ContentProvider startup"
            )
            MendelRuntimeHookInstaller.install(
                runtimeDirectory = targetRuntimeDirectory,
                lpparam = lpparam,
                classLoader = lpparam.classLoader,
                moduleApkPath = moduleApkPath,
            )
            return
        }

        applicationLifecycleHook.install(lpparam) { context, classLoader ->
            val targetRuntimeDirectory = runtimeDirectory.resolve(context)
            XposedLogger.initFileLogging(targetRuntimeDirectory, lpparam)
            XposedLogger.logI("Context received, installing runtime flag override hook")

            val versionCode = runCatching {
                context.packageManager.getPackageInfo(context.packageName, 0).longVersionCode
            }.getOrDefault(0L)
            val diagnostics = SqliteHookDiagnostics(
                databaseFile = File(
                    targetRuntimeDirectory,
                    XposedConstants.HOOK_DIAGNOSTICS_DB_FILE_NAME,
                ),
                packageName = lpparam.packageName,
                processName = lpparam.processName,
                versionCode = versionCode,
            )

            runCatching {
                val runtimeHook = RuntimeFlagOverrideHook(
                    context = context,
                    lpparam = lpparam,
                    runtimeClassLoader = classLoader,
                    diagnostics = diagnostics,
                )
                diagnostics.start(runtimeHook.overrideCount())
                if (runtimeHook.overridesPaused) {
                    diagnostics.paused()
                    XposedLogger.logI(
                        "Skipping runtime flag override hook: overrides are paused for ${lpparam.packageName}"
                    )
                    return@install
                }
                if (!runtimeHook.hasOverrides()) {
                    diagnostics.noOverrides()
                    XposedLogger.logI(
                        "Skipping runtime flag override hook: no overrides for ${lpparam.packageName}"
                    )
                    return@install
                }

                ModuleNativeLibraryLoader.load(
                    moduleApkPath = requireNotNull(moduleApkPath) {
                        "Xposed module path is unavailable; initZygote was not called"
                    },
                    libraryName = XposedConstants.DEXKIT_LIBRARY_NAME,
                )
                runtimeHook.install()
            }.onFailure { error ->
                diagnostics.failure(error.message ?: error.javaClass.simpleName)
                XposedLogger.logE("Failed to install runtime flag override hook", error)
            }
        }
    }

    private fun XC_LoadPackage.LoadPackageParam.isSupportedTarget(): Boolean =
        packageName != BuildConfig.MAIN_APPLICATION_ID &&
            XposedTargets.isSupportedProcess(packageName, processName)

    private companion object {
        @Volatile
        var moduleApkPath: String? = null
    }
}
