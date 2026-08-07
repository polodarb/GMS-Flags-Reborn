package ua.polodarb.xposed.hook.strategy.phenotype

import android.content.Context
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.lang.reflect.Proxy
import java.util.concurrent.ConcurrentHashMap
import org.luckypray.dexkit.DexKitBridge
import ua.polodarb.xposed.diagnostics.HookDiagnostics
import ua.polodarb.xposed.hook.RuntimeFlagOverrideStrategy
import ua.polodarb.xposed.hook.strategy.common.ObjectMethodNames
import ua.polodarb.xposed.info.HookDiagnosticContract
import ua.polodarb.xposed.logging.RuntimeFlagProbeLogger
import ua.polodarb.xposed.logging.XposedLogger
import ua.polodarb.xposed.store.RuntimeFlagOverrideStore
import ua.polodarb.xposed.store.RuntimeFlagOverrideValueParser

internal class FlutterPhenotypeFlagOverrideStrategy(
    private val context: Context,
    private val lpparam: XC_LoadPackage.LoadPackageParam,
    private val runtimeClassLoader: ClassLoader,
    private val overrideStore: RuntimeFlagOverrideStore,
    private val diagnostics: HookDiagnostics = HookDiagnostics.None,
) : RuntimeFlagOverrideStrategy {

    override val diagnosticName = HookDiagnosticContract.STRATEGY_FLUTTER_PHENOTYPE

    private val loggedOverrides = ConcurrentHashMap.newKeySet<String>()
    private val debugLogger = RuntimeFlagProbeLogger("Flutter Phenotype probe")

    override fun install(bridge: DexKitBridge) {
        val handler = findMethodCallHandler(bridge) ?: run {
            diagnostics.strategyUnavailable(diagnosticName, "Flutter Phenotype handler not found")
            XposedLogger.logD("Flutter Phenotype method-call handler not found")
            return
        }
        val resultType = handler.parameterTypes[1]

        XposedBridge.hookMethod(handler, object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                val methodCall = param.args.getOrNull(0) ?: return
                val originalResult = param.args.getOrNull(1) ?: return
                val configPackage = readConfigPackage(methodCall) ?: return
                createResultProxy(resultType, originalResult, configPackage)?.let { proxy ->
                    param.args[1] = proxy
                }
            }
        })

        diagnostics.strategyInstalled(diagnosticName)
        XposedLogger.logI("Flutter Phenotype override hook installed: ${handler.declaringClass.name}#${handler.name}")
    }

    private fun findMethodCallHandler(bridge: DexKitBridge): Method? {
        val candidates = bridge.findMethod {
            matcher { usingStrings(*METHOD_CALL_ANCHORS) }
        }.mapNotNull { candidate ->
            sequenceOf(runtimeClassLoader, lpparam.classLoader).firstNotNullOfOrNull { loader ->
                runCatching { candidate.getMethodInstance(loader) }.getOrNull()
            }
        }.filter { method -> method.isFlutterPhenotypeHandler() }
            .distinctBy(Method::toGenericString)

        if (candidates.size > 1) {
            XposedLogger.logW(
                "Flutter Phenotype handler discovery is ambiguous: " +
                    candidates.joinToString { method ->
                        "${method.declaringClass.name}#${method.name}"
                    }
            )
        }
        return candidates.singleOrNull()?.apply { isAccessible = true }
    }

    private fun Method.isFlutterPhenotypeHandler(): Boolean {
        if (returnType != Void.TYPE || parameterCount != 2) return false
        if (Modifier.isStatic(modifiers) || Modifier.isAbstract(modifiers)) return false

        val methodCallType = parameterTypes[0]
        val resultType = parameterTypes[1]
        val hasPackageArgumentReader = methodCallType.methods.any { method ->
            method.name == ARGUMENT_METHOD_NAME &&
                    method.parameterTypes.contentEquals(arrayOf(String::class.java))
        }
        val hasSuccessCallback = resultType.isInterface && resultType.methods.any { method ->
            method.name == SUCCESS_METHOD_NAME && method.parameterCount == 1
        }
        return hasPackageArgumentReader && hasSuccessCallback
    }

    private fun readConfigPackage(methodCall: Any): String? {
        val argumentMethod = methodCall.javaClass.methods.firstOrNull { method ->
            method.name == ARGUMENT_METHOD_NAME &&
                method.parameterTypes.contentEquals(arrayOf(String::class.java))
        } ?: return null
        return runCatching {
            argumentMethod.invoke(methodCall, PACKAGE_ARGUMENT_NAME) as? String
        }.getOrNull()
    }

    private fun createResultProxy(
        resultType: Class<*>,
        originalResult: Any,
        configPackage: String,
    ): Any? {
        if (!resultType.isInterface || !resultType.isInstance(originalResult)) return null

        return runCatching {
            Proxy.newProxyInstance(
                resultType.classLoader ?: runtimeClassLoader,
                arrayOf(resultType),
            ) { proxy, method, args ->
                @Suppress("IntroduceWhenSubject")
                when {
                    method.name == ObjectMethodNames.TO_STRING && method.parameterCount == 0 ->
                        "GMS Flags Flutter Phenotype result($configPackage)"
                    method.name == ObjectMethodNames.HASH_CODE && method.parameterCount == 0 ->
                        System.identityHashCode(proxy)
                    method.name == ObjectMethodNames.EQUALS && method.parameterCount == 1 ->
                        proxy === args?.firstOrNull()
                    method.name == SUCCESS_METHOD_NAME && method.parameterCount == 1 -> {
                        val result = overrideConfigurationMap(
                            configPackage,
                            args?.firstOrNull(),
                        )
                        invokeDelegate(originalResult, method, arrayOf(result))
                    }
                    else -> invokeDelegate(originalResult, method, args)
                }
            }
        }.getOrElse { error ->
            XposedLogger.logW("Failed to wrap Flutter Phenotype result for $configPackage: ${error.message}")
            null
        }
    }

    private fun overrideConfigurationMap(
        configPackage: String,
        value: Any?,
    ): Any? {
        val configuration = value as? Map<*, *> ?: return value
        val flags = configuration[FLAGS_KEY] as? Map<*, *> ?: return value
        val replacementFlags = LinkedHashMap<Any?, Any?>(flags.size)
        replacementFlags.putAll(flags)
        var changed = false

        flags.forEach { (rawName, original) ->
            val flagName = rawName as? String ?: return@forEach
            val match = overrideStore.findBestMatch(
                configPackage,
                context.packageName,
                flagName,
            )
            val override = match.override ?: run {
                debugLogger.log(
                    "override-miss identity=$configPackage/$flagName, " +
                        "context=${context.packageName}"
                )
                return@forEach
            }
            val parsed = RuntimeFlagOverrideValueParser.parse(original, override) ?: run {
                XposedLogger.logW(
                    "Flutter Phenotype override could not be applied for " +
                        "$configPackage/$flagName, value: $original -> ${override.value} " +
                        "(source=${match.source}, " +
                        "originalType=${original?.javaClass?.name ?: "null"}, " +
                        "storedType=${override.flagType})"
                )
                return@forEach
            }

            replacementFlags[flagName] = parsed
            changed = true
            val identity = "$configPackage/$flagName"
            diagnostics.overrideAppliedAndConsumed(diagnosticName, identity)
            if (loggedOverrides.add(identity)) {
                XposedLogger.logD("Flutter Phenotype override applied: $identity, value: $original -> $parsed (source=${match.source})")
            }
        }

        if (!changed) return value
        return LinkedHashMap<Any?, Any?>(configuration.size).apply {
            putAll(configuration)
            put(FLAGS_KEY, replacementFlags)
        }
    }

    private fun invokeDelegate(
        target: Any,
        method: Method,
        args: Array<out Any?>?,
    ): Any? {
        return try {
            method.invoke(target, *(args ?: emptyArray()))
        } catch (error: InvocationTargetException) {
            throw error.targetException
        }
    }

    private companion object {
        val METHOD_CALL_ANCHORS = arrayOf(
            "registerSync",
            "registerSyncWithoutCommitting",
            "setFlagOverride",
        )
        const val ARGUMENT_METHOD_NAME = "argument"
        const val SUCCESS_METHOD_NAME = "success"
        const val PACKAGE_ARGUMENT_NAME = "package"
        const val FLAGS_KEY = "flags"
    }
}
