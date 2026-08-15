package ua.polodarb.xposed.hook.strategy.mendel

import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap
import org.luckypray.dexkit.DexKitBridge
import ua.polodarb.xposed.diagnostics.HookDiagnostics
import ua.polodarb.xposed.hook.RuntimeFlagOverrideStrategy
import ua.polodarb.xposed.info.HookDiagnosticContract
import ua.polodarb.xposed.info.XposedTargets
import ua.polodarb.xposed.logging.XposedLogger
import ua.polodarb.xposed.store.RuntimeFlagOverrideStore
import ua.polodarb.xposed.store.RuntimeFlagOverrideValueParser

internal class MendelFlagOverrideStrategy(
    private val packageName: String,
    private val runtimeClassLoader: ClassLoader,
    private val overrideStore: RuntimeFlagOverrideStore,
    private val diagnostics: HookDiagnostics = HookDiagnostics.None,
) : RuntimeFlagOverrideStrategy {

    override val diagnosticName = HookDiagnosticContract.STRATEGY_MENDEL

    private val identityPackageName = XposedTargets.mendelFlagPackageName(packageName)
    private val loggedOverrides = ConcurrentHashMap.newKeySet<String>()
    private val loggedIncompatibleOverrides = ConcurrentHashMap.newKeySet<String>()

    override fun install(bridge: DexKitBridge) {
        val utilClass = findExperimentFlagUtilClass(bridge) ?: run {
            diagnostics.strategyUnavailable(
                diagnosticName,
                "Mendel ExperimentFlagUtil was not found uniquely",
            )
            return
        }
        val configMapType = MendelFlagReflection.findConfigMapType(utilClass) ?: run {
            diagnostics.strategyUnavailable(
                diagnosticName,
                "Mendel config map parameter type was not resolved uniquely",
            )
            return
        }
        val readers = MendelFlagReflection.findLeafReaders(utilClass, configMapType)
            .onEach { it.isAccessible = true }
        if (readers.isEmpty()) {
            diagnostics.strategyUnavailable(
                diagnosticName,
                "Mendel reader has no supported typed getters",
            )
            return
        }

        readers.forEach { reader ->
            XposedBridge.hookMethod(reader, object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) = overrideValue(param)
            })
        }

        diagnostics.strategyInstalled(diagnosticName)
        XposedLogger.logI(
            "Mendel flag override hook installed: ${utilClass.name}, getters=${readers.size}"
        )
    }

    private fun findExperimentFlagUtilClass(bridge: DexKitBridge): Class<*>? {
        val candidates = bridge.findMethod {
            matcher { usingStrings(EXPERIMENT_FLAG_UTIL_ANCHOR) }
        }.map { candidate -> candidate.className.replace('/', '.') }
            .distinct()
            .mapNotNull { className ->
                runCatching { Class.forName(className, false, runtimeClassLoader) }.getOrNull()
            }
            .filter { MendelFlagReflection.findConfigMapType(it) != null }

        if (candidates.size != 1) {
            XposedLogger.logW(
                "Mendel ExperimentFlagUtil discovery returned ${candidates.size} structural " +
                    "candidates for $packageName: " +
                    candidates.joinToString { it.name }
            )
            return null
        }
        return candidates.single()
    }

    private fun overrideValue(param: XC_MethodHook.MethodHookParam) {
        if (param.hasThrowable()) return
        val experimentId = param.args.getOrNull(1) as? Long ?: return
        val override = overrideStore.find(identityPackageName, experimentId.toString()) ?: return
        val identity = "$packageName/$experimentId"
        val replacement = RuntimeFlagOverrideValueParser.parse(
            original = param.result,
            override = override,
            declaredType = (param.method as? Method)?.returnType,
        ) ?: run {
            if (loggedIncompatibleOverrides.add(identity)) {
                XposedLogger.logW(
                    "Incompatible Mendel override $identity=" +
                        "${override.value} for ${param.method.name}"
                )
            }
            return
        }

        param.result = replacement
        diagnostics.overrideAppliedAndConsumed(diagnosticName, identity)
        if (loggedOverrides.add(identity)) {
            XposedLogger.logD(
                "Mendel override applied: $identity=$replacement " +
                    "getter=${param.method.declaringClass.name}#${param.method.name}"
            )
        }
    }

    private companion object {
        const val EXPERIMENT_FLAG_UTIL_ANCHOR = "ExperimentFlagUtil"
    }
}
