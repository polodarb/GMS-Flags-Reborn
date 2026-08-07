package ua.polodarb.xposed.hook.strategy.deviceconfig

import android.content.Context
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage
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

internal class DeviceConfigFlagOverrideStrategy(
    private val context: Context,
    private val lpparam: XC_LoadPackage.LoadPackageParam,
    private val runtimeClassLoader: ClassLoader,
    private val overrideStore: RuntimeFlagOverrideStore,
    private val diagnostics: HookDiagnostics = HookDiagnostics.None,
) : RuntimeFlagOverrideStrategy {

    override val diagnosticName = HookDiagnosticContract.STRATEGY_DEVICE_CONFIG_FLAG

    private val loggedOverrides = ConcurrentHashMap.newKeySet<String>()

    override fun install(bridge: DexKitBridge) {
        val reader = findRawReader(bridge) ?: run {
            diagnostics.strategyUnavailable(diagnosticName, "Raw DeviceConfig reader not found")
            return
        }

        XposedBridge.hookMethod(reader, object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                overrideRawValue(param)
            }
        })

        diagnostics.strategyInstalled(diagnosticName)
        XposedLogger.logI(
            "DeviceConfig flag override hook installed: " +
                "${reader.declaringClass.name}#${reader.name}"
        )
    }

    private fun findRawReader(bridge: DexKitBridge): Method? {
        val candidateClassNames = buildList {
            addAll(
                bridge.findMethod {
                    matcher { usingStrings(DEVICE_FLAG_MANAGER_ANCHOR) }
                }.map { candidate -> candidate.className }
            )
            addAll(
                bridge.findMethod {
                    matcher {
                        returnType(String::class.java)
                        paramTypes(String::class.java)
                        addInvoke(DEVICE_CONFIG_GET_PROPERTY_DESCRIPTOR)
                    }
                }.map { candidate -> candidate.className }
            )
        }

        val candidates = candidateClassNames.mapNotNull { className ->
            loadClass(className.replace('/', '.'))
        }.distinct()
            .mapNotNull(DeviceConfigFlagReflection::findRawReaderMethod)
            .distinctBy(Method::toGenericString)

        if (candidates.size > 1) {
            XposedLogger.logW(
                "DeviceConfig reader discovery is ambiguous: " +
                    candidates.joinToString { method ->
                        "${method.declaringClass.name}#${method.name}"
                    }
            )
        }
        return candidates.singleOrNull()?.apply { isAccessible = true }
    }

    private fun loadClass(className: String): Class<*>? =
        sequenceOf(runtimeClassLoader, lpparam.classLoader).firstNotNullOfOrNull { loader ->
            runCatching { Class.forName(className, false, loader) }.getOrNull()
        }

    private fun overrideRawValue(param: XC_MethodHook.MethodHookParam) {
        if (param.hasThrowable()) return
        val manager = param.thisObject ?: return
        val namespace = DeviceConfigFlagReflection.readNamespace(manager) ?: return
        val flagName = param.args.getOrNull(0) as? String ?: return
        val identityPackageName = when {
            lpparam.packageName == XposedTargets.AICORE_PACKAGE_NAME &&
                namespace == AICORE_DEVICE_CONFIG_NAMESPACE ->
                XposedTargets.AICORE_FLAGS_PACKAGE_NAME
            else -> namespace
        }
        val match = overrideStore.findBestMatch(
            identityPackageName,
            context.packageName,
            flagName,
        )
        val override = match.override ?: return
        val parsed = RuntimeFlagOverrideValueParser.parse(null, override) ?: return
        val replacement = when (parsed) {
            is Boolean, is Number, is String -> parsed.toString()
            else -> return
        }

        param.result = replacement
        val identity = "$namespace/$flagName"
        diagnostics.overrideAppliedAndConsumed(diagnosticName, identity)
        if (loggedOverrides.add(identity)) {
            XposedLogger.logD(
                "DeviceConfig override applied: $identity=$replacement " +
                    "(source=${match.source})"
            )
        }
    }

    private companion object {
        const val AICORE_DEVICE_CONFIG_NAMESPACE = "aicore"
        const val DEVICE_FLAG_MANAGER_ANCHOR =
            "com/google/android/apps/miphone/astrea/common/config/impl/DeviceFlagManager"
        const val DEVICE_CONFIG_GET_PROPERTY_DESCRIPTOR =
            "Landroid/provider/DeviceConfig;->getProperty(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;"
    }
}
