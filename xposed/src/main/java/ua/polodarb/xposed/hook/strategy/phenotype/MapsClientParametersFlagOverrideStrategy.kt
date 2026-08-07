package ua.polodarb.xposed.hook.strategy.phenotype

import android.content.Context
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.lang.reflect.Modifier
import java.util.Collections
import java.util.WeakHashMap
import java.util.concurrent.atomic.AtomicBoolean
import org.luckypray.dexkit.DexKitBridge
import org.luckypray.dexkit.result.MethodData
import ua.polodarb.xposed.diagnostics.HookDiagnostics
import ua.polodarb.xposed.hook.RuntimeFlagOverrideStrategy
import ua.polodarb.xposed.info.HookDiagnosticContract
import ua.polodarb.xposed.info.XposedTargets
import ua.polodarb.xposed.logging.XposedLogger
import ua.polodarb.xposed.store.RuntimeFlagOverrideStore

internal class MapsClientParametersFlagOverrideStrategy(
    private val context: Context,
    private val lpparam: XC_LoadPackage.LoadPackageParam,
    private val overrideStore: RuntimeFlagOverrideStore,
    private val diagnostics: HookDiagnostics = HookDiagnostics.None,
) : RuntimeFlagOverrideStrategy {

    override val diagnosticName = HookDiagnosticContract.STRATEGY_MAPS_CLIENT_PARAMETERS

    private val reportedOverrides = AtomicBoolean()
    private val runtimeFailed = AtomicBoolean()
    private val patchedInstances = Collections.synchronizedSet(Collections.newSetFromMap(WeakHashMap<Any, Boolean>()),)

    @Volatile
    private var cachedOverlays: MapsClientParametersReflection.OverlayResult? = null

    override fun install(bridge: DexKitBridge) {
        val updateMethodData = findClientParametersUpdate(bridge) ?: run {
            unavailable("Maps ClientParameters update method not found")
            return
        }
        val updateMethod = runCatching {
            updateMethodData.getMethodInstance(lpparam.classLoader).apply { isAccessible = true }
        }.getOrElse { error ->
            unavailable("Maps ClientParameters update method could not be loaded: ${error.message}")
            return
        }
        val reflection = MapsClientParametersReflection.discover(
            bridge = bridge,
            classLoader = lpparam.classLoader,
            updateMethod = updateMethod,
        ) ?: run {
            unavailable("Maps ClientParameters converter shape not found")
            return
        }

        XposedBridge.hookMethod(updateMethod, object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                runRuntimeGuarded {
                    val originalGroups = param.args.lastOrNull() as? List<*>
                        ?: return@runRuntimeGuarded
                    val overlays = getOrBuildOverlays(reflection)
                    if (overlays.changedGroups.isEmpty()) return@runRuntimeGuarded

                    val merge = reflection.mergeGroups(originalGroups, overlays)
                    if (merge.stats.mergedGroupCount == 0) return@runRuntimeGuarded
                    param.args[param.args.lastIndex] = merge.groups
                    reportApplied(overlays, merge.stats)
                }
            }
        })
        XposedBridge.hookMethod(reflection.groupAccessor, object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                val instance = param.thisObject ?: return
                if (!patchedInstances.add(instance)) return

                runRuntimeGuarded {
                    val overlays = getOrBuildOverlays(reflection)
                    if (overlays.changedGroups.isEmpty()) return@runRuntimeGuarded
                    val stats = reflection.mergeIntoInstance(instance, overlays)
                    if (stats.mergedGroupCount > 0) {
                        reportApplied(overlays, stats)
                    }
                }
            }
        })

        diagnostics.strategyInstalled(diagnosticName)
        XposedLogger.logI(
            "Maps ClientParameters override hook installed: " +
                "${updateMethod.declaringClass.name}#${updateMethod.name}; " +
                "live=${reflection.groupAccessor.declaringClass.name}#" +
                "${reflection.groupAccessor.name}; converter=${reflection.converterDescription}"
        )
    }

    private fun getOrBuildOverlays(
        reflection: MapsClientParametersReflection,
    ): MapsClientParametersReflection.OverlayResult =
        cachedOverlays ?: synchronized(this) {
            cachedOverlays ?: buildOverlays(reflection).also { cachedOverlays = it }
        }

    private fun runRuntimeGuarded(block: () -> Unit) {
        if (runtimeFailed.get()) return
        runCatching(block).onFailure { error ->
            if (runtimeFailed.compareAndSet(false, true)) {
                val reason = error.message ?: error.javaClass.simpleName
                diagnostics.strategyFailed(diagnosticName, reason)
                XposedLogger.logE("Maps ClientParameters override failed", error)
            }
        }
    }

    private fun findClientParametersUpdate(bridge: DexKitBridge): MethodData? {
        val candidates = bridge.findMethod {
            matcher { usingStrings(*CLIENT_PARAMETERS_UPDATE_ANCHORS) }
        }.filter { candidate ->
            val modifiers = candidate.modifiers
            !Modifier.isStatic(modifiers) &&
                !Modifier.isAbstract(modifiers) &&
                candidate.paramTypes.size == CLIENT_PARAMETERS_UPDATE_PARAMETER_COUNT
        }.distinctBy(MethodData::descriptor)

        if (candidates.size > 1) {
            XposedLogger.logW(
                "Maps ClientParameters update discovery is ambiguous: " +
                    candidates.joinToString { "${it.className}#${it.name}" }
            )
        }
        return candidates.singleOrNull()
    }

    private fun buildOverlays(
        reflection: MapsClientParametersReflection,
    ): MapsClientParametersReflection.OverlayResult {
        val phenotypePackageName = XposedTargets.MAPS_FLAGS_PACKAGE_NAME.substringBefore('#')
        val matches = overrideStore.findBestMatches(
            identityPackageName = phenotypePackageName,
            contextPackageName = context.packageName,
        )
        return reflection.buildOverlays(matches, phenotypePackageName).also { result ->
            XposedLogger.logD(
                "Maps ClientParameters overlays built: stored=${matches.size}, " +
                    "numeric=${result.numericOverrides.size}, " +
                    "changedGroups=${result.changedGroups.size}"
            )
        }
    }

    private fun reportApplied(
        overlays: MapsClientParametersReflection.OverlayResult,
        stats: MapsClientParametersReflection.MergeStats,
    ) {
        if (!reportedOverrides.compareAndSet(false, true)) return

        overlays.numericOverrides.forEach { applied ->
            diagnostics.overrideAppliedAndConsumed(diagnosticName, applied.identity)
        }
        XposedLogger.logI(
            "Maps ClientParameters overrides applied: numeric=${overlays.numericOverrides.size}, " +
                "changedGroups=${overlays.changedGroups.size}, " +
                "mergedGroups=${stats.mergedGroupCount}, " +
                "appendedGroups=${stats.appendedGroupCount}"
        )
    }

    private fun unavailable(reason: String) {
        diagnostics.strategyUnavailable(diagnosticName, reason)
        XposedLogger.logW(reason)
    }

    private companion object {
        val CLIENT_PARAMETERS_UPDATE_ANCHORS = arrayOf(
            "ClientParametersImpl.updateParametersInternal",
            "ClientParametersImpl.updateParameterGroup",
        )
        const val CLIENT_PARAMETERS_UPDATE_PARAMETER_COUNT = 5
    }
}
