package ua.polodarb.xposed.hook.strategy.vending

import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import org.luckypray.dexkit.DexKitBridge
import ua.polodarb.xposed.hook.RuntimeFlagOverrideStrategy
import ua.polodarb.xposed.logging.RuntimeFlagProbeLogger
import ua.polodarb.xposed.logging.XposedLogger
import ua.polodarb.xposed.store.RuntimeFlagOverrideStore
import ua.polodarb.xposed.store.RuntimeFlagOverrideValueParser
import ua.polodarb.xposed.diagnostics.HookDiagnostics
import ua.polodarb.xposed.info.HookDiagnosticContract

internal class FinskyExperimentFlagOverrideStrategy(
    private val lpparam: XC_LoadPackage.LoadPackageParam,
    private val runtimeClassLoader: ClassLoader,
    private val overrideStore: RuntimeFlagOverrideStore,
    private val diagnostics: HookDiagnostics = HookDiagnostics.None,
) : RuntimeFlagOverrideStrategy {
    override val diagnosticName = HookDiagnosticContract.STRATEGY_FINSKY
    private val debugLogger = RuntimeFlagProbeLogger("Finsky flag probe")

    override fun install(bridge: DexKitBridge) {
        val accessorClass = findAccessorClass(bridge) ?: run {
            diagnostics.strategyUnavailable(
                diagnosticName,
                "Accessor class not found",
            )
            XposedLogger.logW("Finsky experiment flag accessor not found")
            return
        }

        val methods = findReaderMethods(accessorClass)
        if (methods.isEmpty()) {
            diagnostics.strategyUnavailable(
                diagnosticName,
                "Reader methods not found",
            )
            XposedLogger.logW("Finsky experiment flag reader methods not found")
            return
        }

        methods.forEach(::installHook)
        diagnostics.strategyInstalled(diagnosticName)
        XposedLogger.logI(
            "Finsky experiment flag hooks installed: " +
                methods.joinToString { "${it.declaringClass.name}#${it.name}" }
        )
    }

    private fun findAccessorClass(bridge: DexKitBridge): Class<*>? {
        val candidate = bridge.findMethod {
            matcher {
                usingStrings(
                    "Found empty / null Phenotype experiment feature name",
                    "Found empty / null Phenotype experiment flag name",
                )
            }
        }.firstOrNull() ?: return null

        val className = candidate.className.replace('/', '.')
        return sequenceOf(runtimeClassLoader, lpparam.classLoader)
            .mapNotNull { classLoader ->
                runCatching { Class.forName(className, false, classLoader) }.getOrNull()
            }
            .firstOrNull()
    }

    private fun findReaderMethods(accessorClass: Class<*>): List<Method> = buildList {
        accessorClass.declaredMethods.firstOrNull { it.isObjectReader() }?.let(::add)
        accessorClass.declaredMethods.firstOrNull { it.isIntegerReader() }?.let(::add)
        accessorClass.declaredMethods.firstOrNull { it.isTelemetryBooleanReader() }?.let(::add)
    }.onEach { it.isAccessible = true }

    private fun installHook(method: Method) {
        XposedBridge.hookMethod(method, object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                overrideResult(param)
            }
        })
    }

    private fun overrideResult(param: XC_MethodHook.MethodHookParam) {
        val identity = FinskyExperimentFlagIdentity.fromArguments(param.args) ?: return
        val packageName = FinskyExperimentPackageResolver.resolve(
            accessor = param.thisObject ?: return,
            readerClass = param.method.declaringClass,
            flagName = identity.flagName,
        ) ?: run {
            debugLogger.log("package-miss flag=${identity.flagName}")
            return
        }
        val override = overrideStore.find(packageName, identity.flagName) ?: run {
            debugLogger.log("override-miss package=$packageName, flag=${identity.flagName}")
            return
        }

        val original = param.result
        val parsed = RuntimeFlagOverrideValueParser.parse(original, override) ?: run {
            debugLogger.log(
                "parse-miss package=$packageName, flag=${identity.flagName}, " +
                    "originalType=${original?.javaClass?.name ?: "null"}, " +
                    "storedType=${override.flagType}, value=${override.value}"
            )
            return
        }

        param.result = parsed
        val diagnosticIdentity = "$packageName/${identity.flagName}"
        diagnostics.overrideApplied(diagnosticName, diagnosticIdentity)
        diagnostics.overrideConsumed(diagnosticName, diagnosticIdentity)
        XposedLogger.logD(
            "Finsky experiment flag override applied: $packageName/${identity.flagName}=$parsed " +
                "(original=$original)"
        )
    }

    private fun Method.isObjectReader(): Boolean =
        !Modifier.isStatic(modifiers) &&
            returnType == Any::class.java &&
            parameterTypes.contentEquals(
                arrayOf(
                    String::class.java,
                    String::class.java,
                    String::class.java,
                    Class::class.java,
                )
            )

    private fun Method.isIntegerReader(): Boolean =
        !Modifier.isStatic(modifiers) &&
            returnType == Int::class.javaPrimitiveType &&
            parameterTypes.contentEquals(
                arrayOf(String::class.java, String::class.java, String::class.java)
            )

    private fun Method.isTelemetryBooleanReader(): Boolean =
        !Modifier.isStatic(modifiers) &&
            returnType == Boolean::class.javaPrimitiveType &&
            parameterTypes.size == 4 &&
            parameterTypes.take(3).all { it == String::class.java } &&
            parameterTypes[3] != Class::class.java

}
