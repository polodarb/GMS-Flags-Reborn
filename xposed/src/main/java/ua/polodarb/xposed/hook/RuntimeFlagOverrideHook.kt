package ua.polodarb.xposed.hook

import android.content.Context
import de.robv.android.xposed.callbacks.XC_LoadPackage
import org.luckypray.dexkit.DexKitBridge
import ua.polodarb.xposed.info.XposedConstants
import ua.polodarb.xposed.info.XposedTargets
import java.io.File
import ua.polodarb.xposed.hook.strategy.phenotype.FlutterPhenotypeFlagOverrideStrategy
import ua.polodarb.xposed.hook.strategy.phenotype.MapsClientParametersFlagOverrideStrategy
import ua.polodarb.xposed.hook.strategy.phenotype.PhenotypeRegistryFlagOverrideStrategy
import ua.polodarb.xposed.hook.strategy.phenotype.PhenotypeRuntimeFlagOverrideStrategy
import ua.polodarb.xposed.hook.strategy.vending.FinskyExperimentFlagOverrideStrategy
import ua.polodarb.xposed.hook.strategy.inputmethod.InputMethodFlagOverrideStrategy
import ua.polodarb.xposed.hook.strategy.deviceconfig.DeviceConfigFlagOverrideStrategy
import ua.polodarb.xposed.hook.strategy.googlecamera.GoogleCameraConfigFlagOverrideStrategy
import ua.polodarb.xposed.logging.XposedLogger
import ua.polodarb.xposed.store.RuntimeFlagOverrideStore
import ua.polodarb.xposed.diagnostics.HookDiagnostics

internal class RuntimeFlagOverrideHook(
    private val context: Context,
    private val lpparam: XC_LoadPackage.LoadPackageParam,
    private val runtimeClassLoader: ClassLoader,
    private val diagnostics: HookDiagnostics = HookDiagnostics.None,
) {

    private val overrideStore = RuntimeFlagOverrideStore(
        File(File(context.dataDir, XposedConstants.XPOSED_DIR), XposedConstants.RUNTIME_OVERRIDES_DB_FILE_NAME)
    )

    val overridesPaused: Boolean
        get() = File(
            File(context.dataDir, XposedConstants.XPOSED_DIR),
            XposedConstants.OVERRIDES_PAUSED_FILE_NAME,
        ).isFile

    fun hasOverrides(): Boolean = overrideStore.hasOverrides()
    fun overrideCount(): Int = overrideStore.overrideCount()

    fun install() {
        val apkPath = lpparam.appInfo.sourceDir ?: run {
            XposedLogger.logE("No sourceDir for ${lpparam.packageName}")
            return
        }

        DexKitBridge.create(apkPath).use { bridge ->
            strategies().forEach { strategy ->
                runCatching { strategy.install(bridge) }.onFailure { failure ->
                    val reason = failure.message ?: failure.javaClass.simpleName
                    diagnostics.strategyFailed(strategy.diagnosticName, reason)
                    XposedLogger.logE(
                        "Failed to install ${strategy.diagnosticName} strategy",
                        failure,
                    )
                }
            }
        }
    }

    private fun strategies(): List<RuntimeFlagOverrideStrategy> {
        return buildList {
            add(
                PhenotypeRuntimeFlagOverrideStrategy(
                    context = context,
                    lpparam = lpparam,
                    runtimeClassLoader = runtimeClassLoader,
                    overrideStore = overrideStore,
                    diagnostics = diagnostics,
                )
            )
            add(
                PhenotypeRegistryFlagOverrideStrategy(
                    context = context,
                    lpparam = lpparam,
                    runtimeClassLoader = runtimeClassLoader,
                    overrideStore = overrideStore,
                    diagnostics = diagnostics,
                )
            )
            add(
                InputMethodFlagOverrideStrategy(
                    context = context,
                    lpparam = lpparam,
                    runtimeClassLoader = runtimeClassLoader,
                    overrideStore = overrideStore,
                    diagnostics = diagnostics,
                )
            )
            add(
                DeviceConfigFlagOverrideStrategy(
                    context = context,
                    lpparam = lpparam,
                    runtimeClassLoader = runtimeClassLoader,
                    overrideStore = overrideStore,
                    diagnostics = diagnostics,
                )
            )
            add(
                FlutterPhenotypeFlagOverrideStrategy(
                    context = context,
                    lpparam = lpparam,
                    runtimeClassLoader = runtimeClassLoader,
                    overrideStore = overrideStore,
                    diagnostics = diagnostics,
                )
            )

            if (lpparam.packageName == XposedTargets.VENDING_PACKAGE_NAME) {
                add(
                    FinskyExperimentFlagOverrideStrategy(
                        lpparam = lpparam,
                        runtimeClassLoader = runtimeClassLoader,
                        overrideStore = overrideStore,
                        diagnostics = diagnostics,
                    )
                )
            }

            if (lpparam.packageName == XposedTargets.GOOGLE_CAMERA_PACKAGE_NAME) {
                add(
                    GoogleCameraConfigFlagOverrideStrategy(
                        context = context,
                        lpparam = lpparam,
                        runtimeClassLoader = runtimeClassLoader,
                        overrideStore = overrideStore,
                        diagnostics = diagnostics,
                    )
                )
            }

            if (lpparam.packageName == XposedTargets.MAPS_PACKAGE_NAME) {
                add(
                    MapsClientParametersFlagOverrideStrategy(
                        context = context,
                        lpparam = lpparam,
                        overrideStore = overrideStore,
                        diagnostics = diagnostics,
                    )
                )
            }
        }
    }
}
