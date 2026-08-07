package ua.polodarb.xposed.hook.strategy.phenotype

import android.content.Context
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage
import org.luckypray.dexkit.DexKitBridge
import java.lang.reflect.Method
import ua.polodarb.xposed.discovery.RuntimeFlagAccessorInspector
import ua.polodarb.xposed.hook.RuntimeFlagOverrideStrategy
import ua.polodarb.xposed.logging.XposedLogger
import ua.polodarb.xposed.store.RuntimeFlagOverrideStore
import ua.polodarb.xposed.store.RuntimeFlagOverrideValueParser
import ua.polodarb.xposed.diagnostics.HookDiagnostics
import ua.polodarb.xposed.info.HookDiagnosticContract
import java.lang.reflect.Modifier

internal class PhenotypeRuntimeFlagOverrideStrategy(
    private val context: Context,
    private val lpparam: XC_LoadPackage.LoadPackageParam,
    private val runtimeClassLoader: ClassLoader,
    private val overrideStore: RuntimeFlagOverrideStore,
    private val diagnostics: HookDiagnostics = HookDiagnostics.None,
) : RuntimeFlagOverrideStrategy {

    override val diagnosticName = HookDiagnosticContract.STRATEGY_PHENOTYPE_RUNTIME

    override fun install(bridge: DexKitBridge) {
        val methods = findFlagReaderMethods(bridge)
        if (methods.isEmpty()) {
            diagnostics.strategyUnavailable(
                diagnosticName,
                "Reader method not found",
            )
            XposedLogger.logW("Runtime flag reader method not found")
            return
        }

        methods.forEach { method ->
            XposedBridge.hookMethod(method, object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    overrideResult(param)
                }
            })

            XposedLogger.logI("Runtime flag override hook installed: ${method.declaringClass.name}#${method.name}")
        }

        diagnostics.strategyInstalled(diagnosticName)
    }

    private fun findFlagReaderMethods(bridge: DexKitBridge): List<Method> {
        return READER_DISCOVERY_ANCHORS
            .flatMap { anchor ->
                bridge.findMethod {
                    matcher {
                        usingStrings(anchor.strings)
                        returnType(Any::class.java)
                    }
                }.filter { methodData ->
                    val mod = methodData.modifiers
                    val isValidModifier = (mod and Modifier.STATIC) == 0 && (mod and Modifier.ABSTRACT) == 0
                    val isValidParamCount = methodData.paramTypes.size in anchor.arguments
                    isValidModifier && isValidParamCount
                }
            }
            .distinctBy { candidate -> candidate.descriptor }
            .map { candidate -> candidate.getMethodInstance(lpparam.classLoader) }
    }

    private val loggedMissingIdentityClasses =
        java.util.Collections.newSetFromMap(java.util.concurrent.ConcurrentHashMap<String, Boolean>())
    private val loggedOutcomes =
        java.util.Collections.newSetFromMap(java.util.concurrent.ConcurrentHashMap<String, Boolean>())

    private fun overrideResult(param: XC_MethodHook.MethodHookParam) {
        val accessor = param.thisObject ?: return
        val identity = RuntimeFlagAccessorInspector.findPhenotypeIdentity(
            accessor = accessor,
            contextPackageName = context.packageName,
        ) ?: run {
            val className = accessor.javaClass.name
            if (loggedMissingIdentityClasses.add(className)) {
                XposedLogger.logW(
                    "Phenotype flag identity not found for accessor $className, " +
                        "fields: ${RuntimeFlagAccessorInspector.describeStringFields(accessor)}"
                )
            }
            return
        }
        val debugIdentity = "${identity.packageName}/${identity.flagName}"

        val match = overrideStore.findBestMatch(identity.packageName, context.packageName, identity.flagName)
        val override = match.override ?: return

        val original = param.result
        val parsed = RuntimeFlagOverrideValueParser.parse(original, override) ?: run {
            if (loggedOutcomes.add("fail:$debugIdentity:$original:${override.value}")) {
                XposedLogger.logW(
                    "Phenotype override could not be applied for $debugIdentity, " +
                        "value: $original -> ${override.value} " +
                        "(source=${match.source}, originalType=${original?.javaClass?.name ?: "null"}, " +
                        "storedType=${override.flagType})"
                )
            }
            return
        }

        param.result = parsed
        diagnostics.overrideAppliedAndConsumed(diagnosticName, debugIdentity)
        if (loggedOutcomes.add("ok:$debugIdentity:$parsed")) {
            XposedLogger.logD(
                "Phenotype override applied for $debugIdentity, " +
                    "value: $original -> $parsed (source=${match.source})"
            )
        }
    }

    private companion object {
        val READER_DISCOVERY_ANCHORS = listOf(
            ReaderDiscoveryAnchor(
                strings = setOf(
                    "Invalid Phenotype flag value for flag ",
                    "FilePhenotypeFlags",
                    "com.android.vending",
                    "com.google.android.gms.measurement#",
                ),
                arguments = setOf(1, 3),
            ),
            ReaderDiscoveryAnchor(
                strings = setOf(
                    "com.android.vending",
                    "com.google.android.wearable.app.cn",
                    "com.google.android.gms.measurement#",
                ),
                arguments = setOf(1, 3),
            ),
            ReaderDiscoveryAnchor(
                strings = setOf(
                    "Must call PhenotypeFlagInitializer.maybeInit() first",
                ),
                arguments = setOf(0, 1),
            ),
            ReaderDiscoveryAnchor(
                strings = setOf(
                    "Must call PhenotypeContext.setContext() first",
                ),
                arguments = setOf(0),
            ),
        )

        data class ReaderDiscoveryAnchor(
            val strings: Set<String>,
            val arguments: Set<Int>,
        )
    }

}
