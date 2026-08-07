package ua.polodarb.xposed.hook.strategy.inputmethod

import android.content.Context
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage
import org.luckypray.dexkit.DexKitBridge
import java.lang.reflect.Method
import ua.polodarb.xposed.hook.RuntimeFlagOverrideStrategy
import ua.polodarb.xposed.logging.RuntimeFlagProbeLogger
import ua.polodarb.xposed.logging.XposedLogger
import ua.polodarb.xposed.store.RuntimeFlagOverrideStore
import ua.polodarb.xposed.store.RuntimeFlagOverrideValueParser
import ua.polodarb.xposed.diagnostics.HookDiagnostics
import ua.polodarb.xposed.info.HookDiagnosticContract

internal class InputMethodFlagOverrideStrategy(
    private val context: Context,
    private val lpparam: XC_LoadPackage.LoadPackageParam,
    private val runtimeClassLoader: ClassLoader,
    private val overrideStore: RuntimeFlagOverrideStore,
    private val diagnostics: HookDiagnostics = HookDiagnostics.None,
) : RuntimeFlagOverrideStrategy {

    override val diagnosticName = HookDiagnosticContract.STRATEGY_INPUT_METHOD_FLAG

    private val debugLogger = RuntimeFlagProbeLogger("InputMethod flag probe")

    override fun install(bridge: DexKitBridge) {
        val getters = findFlagGetters(bridge)
        if (getters.isEmpty()) {
            diagnostics.strategyUnavailable(diagnosticName, "Flag getter not found")
            XposedLogger.logW("InputMethod flag getter not found")
            return
        }

        getters.forEach { getter ->
            val nameAccessor = getter.nameAccessor
            XposedBridge.hookMethod(getter.method, object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    overrideResult(param, nameAccessor)
                }
            })
            XposedLogger.logI(
                "InputMethod flag override hook installed: " +
                    "${getter.method.declaringClass.name}#${getter.method.name}",
            )
        }

        diagnostics.strategyInstalled(diagnosticName)
    }

    private fun findFlagGetters(bridge: DexKitBridge): List<FlagGetter> {
        val result = linkedMapOf<String, FlagGetter>()

        val candidates = bridge.findMethod {
            matcher { usingStrings(GETTER_ANCHOR) }
        }

        for (candidate in candidates) {
            val className = candidate.className.replace('/', '.')
            val loader = sequenceOf(runtimeClassLoader, lpparam.classLoader)
                .firstOrNull { classLoader ->
                    runCatching { Class.forName(className, false, classLoader) }.isSuccess
                } ?: continue

            val method = runCatching { candidate.getMethodInstance(loader) }.getOrNull() ?: continue
            if (method.parameterCount != 0 || method.returnType != Any::class.java) continue

            val nameAccessor = InputMethodFlagReflection.findNameAccessor(method) ?: continue

            method.isAccessible = true
            nameAccessor.isAccessible = true
            result.putIfAbsent(method.toGenericString(), FlagGetter(method, nameAccessor))
        }

        return result.values.toList()
    }

    private fun overrideResult(param: XC_MethodHook.MethodHookParam, nameAccessor: Method) {
        val flag = param.thisObject ?: return
        val flagName = runCatching { nameAccessor.invoke(flag) as? String }.getOrNull() ?: return
        val original = param.result

        val match = overrideStore.findBestMatch(
            identityPackageName = context.packageName,
            contextPackageName = context.packageName,
            flagName = flagName,
        )
        val override = match.override ?: run {
            debugLogger.log(
                "override-miss identity=${context.packageName}/$flagName, " +
                    "resultType=${original?.javaClass?.name ?: "null"}, " +
                    overrideStore.describeForLog(),
            )
            return
        }

        val parsed = RuntimeFlagOverrideValueParser.parse(original, override) ?: run {
            debugLogger.log(
                "parse-miss identity=${context.packageName}/$flagName, source=${match.source}, " +
                    "originalType=${original?.javaClass?.name ?: "null"}, " +
                    "storedType=${override.flagType}, value=${override.value}",
            )
            return
        }

        param.result = parsed
        val diagnosticIdentity = "${context.packageName}/$flagName"
        diagnostics.overrideApplied(diagnosticName, diagnosticIdentity)
        diagnostics.overrideConsumed(diagnosticName, diagnosticIdentity)
        XposedLogger.logD(
            "InputMethod flag override applied: $diagnosticIdentity=$parsed " +
                "(source=${match.source}, original=$original)",
        )
    }

    private data class FlagGetter(
        val method: Method,
        val nameAccessor: Method,
    )

    private companion object {
        const val GETTER_ANCHOR = "Invalid flag: "
    }
}
