package ua.polodarb.xposed.hook.strategy.googlecamera

import android.content.Context
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.util.concurrent.ConcurrentHashMap
import org.luckypray.dexkit.DexKitBridge
import org.luckypray.dexkit.result.MethodData
import ua.polodarb.xposed.diagnostics.HookDiagnostics
import ua.polodarb.xposed.hook.RuntimeFlagOverrideStrategy
import ua.polodarb.xposed.info.HookDiagnosticContract
import ua.polodarb.xposed.info.XposedTargets
import ua.polodarb.xposed.logging.RuntimeFlagProbeLogger
import ua.polodarb.xposed.logging.XposedLogger
import ua.polodarb.xposed.store.RuntimeFlagOverrideStore
import ua.polodarb.xposed.store.RuntimeFlagOverrideValueParser

internal class GoogleCameraConfigFlagOverrideStrategy(
    private val context: Context,
    private val lpparam: XC_LoadPackage.LoadPackageParam,
    private val runtimeClassLoader: ClassLoader,
    private val overrideStore: RuntimeFlagOverrideStore,
    private val diagnostics: HookDiagnostics = HookDiagnostics.None,
) : RuntimeFlagOverrideStrategy {

    override val diagnosticName = HookDiagnosticContract.STRATEGY_GOOGLE_CAMERA_CONFIG

    private val debugLogger = RuntimeFlagProbeLogger("GoogleCamera config probe")
    private val loggedOverrides = ConcurrentHashMap.newKeySet<String>()

    override fun install(bridge: DexKitBridge) {
        val resolved = resolve(bridge) ?: run {
            diagnostics.strategyUnavailable(diagnosticName, "GcaConfig reader not found")
            XposedLogger.logW("GoogleCamera config reader not found")
            return
        }

        val keyField = resolved.keyField
        resolved.getters.forEach { getter ->
            XposedBridge.hookMethod(getter, object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    overrideResult(param, keyField)
                }
            })
            XposedLogger.logI(
                "GoogleCamera config override hook installed: " +
                    "${getter.declaringClass.name}#${getter.name}",
            )
        }

        diagnostics.strategyInstalled(diagnosticName)
    }

    private fun overrideResult(param: XC_MethodHook.MethodHookParam, keyField: Field) {
        if (param.hasThrowable()) return
        val descriptor = param.args.getOrNull(0) ?: return
        val flagName = runCatching { keyField.get(descriptor) as? String }.getOrNull() ?: return
        val original = param.result

        val match = overrideStore.findBestMatch(
            identityPackageName = XposedTargets.GOOGLE_CAMERA_FLAGS_PACKAGE_NAME,
            contextPackageName = context.packageName,
            flagName = flagName,
        )
        val override = match.override ?: run {
            debugLogger.log(
                "override-miss identity=${XposedTargets.GOOGLE_CAMERA_FLAGS_PACKAGE_NAME}/$flagName, " +
                    "resultType=${original?.javaClass?.name ?: "null"}, " +
                    overrideStore.describeForLog(),
            )
            return
        }

        val parsed = RuntimeFlagOverrideValueParser.parse(original, override) ?: return
        param.result = parsed

        val identity = "${XposedTargets.GOOGLE_CAMERA_FLAGS_PACKAGE_NAME}/$flagName"
        diagnostics.overrideAppliedAndConsumed(diagnosticName, identity)
        if (loggedOverrides.add(identity)) {
            XposedLogger.logD(
                "GoogleCamera config override applied: $identity=$parsed " +
                    "(source=${match.source}, original=$original)",
            )
        }
    }

    private fun resolve(bridge: DexKitBridge): Resolved? {
        val candidates = findDescriptorCandidates(bridge)
        for (descriptor in candidates) {
            val keyField = findKeyField(descriptor) ?: continue
            val getters = findBooleanGetters(bridge, descriptor)
            if (getters.isEmpty()) continue
            keyField.isAccessible = true
            return Resolved(keyField, getters)
        }
        return null
    }

    private fun findBooleanGetters(bridge: DexKitBridge, descriptor: Class<*>): List<Method> =
        bridge.findMethod {
            matcher { paramTypes(descriptor) }
        }.mapNotNull { candidate ->
            val loader = loadClassLoaderFor(candidate.className) ?: return@mapNotNull null
            runCatching { candidate.getMethodInstance(loader) }.getOrNull()
        }.filter { method ->
            method.parameterCount == 1 &&
                method.returnType == java.lang.Boolean.TYPE &&
                !Modifier.isStatic(method.modifiers)
        }.distinctBy(Method::toGenericString)
            .onEach { it.isAccessible = true }

    private fun findDescriptorCandidates(bridge: DexKitBridge): List<Class<*>> {
        val definingClassNames = LinkedHashSet<String>()
        for (anchor in FLAG_KEY_ANCHORS) {
            bridge.findMethod { matcher { usingStrings(anchor) } }
                .map(MethodData::className)
                .forEach(definingClassNames::add)
            bridge.findClass { matcher { usingStrings(anchor) } }
                .map { it.name }
                .forEach(definingClassNames::add)
        }

        val descriptors = LinkedHashSet<Class<*>>()
        for (className in definingClassNames) {
            val defining = loadClass(className.replace('/', '.')) ?: continue
            defining.declaredFields.asSequence()
                .filter { Modifier.isStatic(it.modifiers) }
                .map { it.type }
                .filter { type -> !type.isPrimitive && findKeyField(type) != null }
                .forEach(descriptors::add)
        }
        return descriptors.toList()
    }

    private fun findKeyField(type: Class<*>): Field? {
        var current: Class<*>? = type
        while (current != null && current != Any::class.java) {
            val field = current.declaredFields.firstOrNull { field ->
                field.type == String::class.java && !Modifier.isStatic(field.modifiers)
            }
            if (field != null) return field
            current = current.superclass
        }
        return null
    }

    private fun loadClass(className: String): Class<*>? =
        sequenceOf(runtimeClassLoader, lpparam.classLoader).firstNotNullOfOrNull { loader ->
            runCatching { Class.forName(className, false, loader) }.getOrNull()
        }

    private fun loadClassLoaderFor(className: String): ClassLoader? {
        val normalized = className.replace('/', '.')
        return sequenceOf(runtimeClassLoader, lpparam.classLoader).firstOrNull { loader ->
            runCatching { Class.forName(normalized, false, loader) }.isSuccess
        }
    }

    private class Resolved(
        val keyField: Field,
        val getters: List<Method>,
    )

    private companion object {
        val FLAG_KEY_ANCHORS = arrayOf(
            "camera.enable_centaur_setting",
            "camera.boba_jelly_eligible",
        )
    }
}
