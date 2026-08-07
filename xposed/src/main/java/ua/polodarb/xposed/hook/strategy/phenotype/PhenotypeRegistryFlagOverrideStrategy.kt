package ua.polodarb.xposed.hook.strategy.phenotype

import android.content.Context
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage
import org.luckypray.dexkit.DexKitBridge
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.util.concurrent.ConcurrentHashMap
import ua.polodarb.xposed.hook.RuntimeFlagOverrideStrategy
import ua.polodarb.xposed.hook.strategy.common.ObjectMethodNames
import ua.polodarb.xposed.logging.RuntimeFlagProbeLogger
import ua.polodarb.xposed.logging.XposedLogger
import ua.polodarb.xposed.store.RuntimeFlagOverrideStore
import ua.polodarb.xposed.store.RuntimeFlagOverrideValueParser
import ua.polodarb.xposed.diagnostics.HookDiagnostics
import ua.polodarb.xposed.info.HookDiagnosticContract

internal class PhenotypeRegistryFlagOverrideStrategy(
    private val context: Context,
    private val lpparam: XC_LoadPackage.LoadPackageParam,
    private val runtimeClassLoader: ClassLoader,
    private val overrideStore: RuntimeFlagOverrideStore,
    private val diagnostics: HookDiagnostics = HookDiagnostics.None,
) : RuntimeFlagOverrideStrategy {

    override val diagnosticName = HookDiagnosticContract.STRATEGY_PHENOTYPE_REGISTRY

    private val replacementCache = ConcurrentHashMap<ReplacementKey, Any>()
    private val valueConstructorCache = ConcurrentHashMap<Class<*>, PhenotypeRegistryReflection.ValueConstructor>()
    private val loggedOverrides = ConcurrentHashMap.newKeySet<String>()
    private val loggedConsumedOverrides = ConcurrentHashMap.newKeySet<String>()
    private val debugLogger = RuntimeFlagProbeLogger("Phenotype registry probe")

    override fun install(bridge: DexKitBridge) {
        val lookupMethods = findRegistryLookupMethods(bridge)
        if (lookupMethods.isEmpty()) {
            diagnostics.strategyUnavailable(
                diagnosticName,
                "Registry lookup not found",
            )
            XposedLogger.logD("Phenotype registry lookup not found")
            return
        }

        lookupMethods.forEach { discovered ->
            XposedBridge.hookMethod(discovered.method, object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    overrideRegistryValue(param, discovered.identitySource)
                }
            })

            XposedLogger.logI(
                "Phenotype registry override hook installed: " +
                    "${discovered.method.declaringClass.name}#${discovered.method.name} " +
                    "(${discovered.identitySource.description})"
            )
        }

        diagnostics.strategyInstalled(diagnosticName)
    }

    private fun findRegistryLookupMethods(bridge: DexKitBridge): List<DiscoveredLookup> {
        val registryClasses = REGISTRY_DISCOVERY_ANCHORS
            .flatMap { anchors ->
                bridge.findMethod {
                    matcher { usingStrings(*anchors) }
                }
            }
            .mapNotNull { candidate -> loadClass(candidate.className.replace('/', '.')) }
            .distinct()

        val explicitCandidates = registryClasses.mapNotNull { registryClass ->
            PhenotypeRegistryReflection.findLookupMethod(registryClass)
        }.distinctBy(Method::toGenericString)
        val singleArgumentCandidates = registryClasses.mapNotNull { registryClass ->
            PhenotypeRegistryReflection.findBoundLookupMethod(registryClass)
        }.distinctBy(Method::toGenericString)
        val combinedCandidates = singleArgumentCandidates.filter { method ->
            method.usesStrings(bridge, COMBINED_REGISTRY_ANCHORS)
        }
        val combinedMethodNames = combinedCandidates.mapTo(mutableSetOf(), Method::toGenericString)
        val boundCandidates = singleArgumentCandidates.filter { method ->
            method.toGenericString() !in combinedMethodNames &&
                method.usesStrings(bridge, BOUND_REGISTRY_ANCHORS)
        }

        return buildList {
            uniqueCandidate("explicit", explicitCandidates)?.let { method ->
                method.isAccessible = true
                add(DiscoveredLookup(method, IdentitySource.ExplicitArguments))
            }
            uniqueCandidate("combined", combinedCandidates)?.let { method ->
                method.isAccessible = true
                add(DiscoveredLookup(method, IdentitySource.CombinedArgument))
            }
            uniqueCandidate("bound", boundCandidates)?.let { method ->
                method.isAccessible = true
                add(DiscoveredLookup(method, IdentitySource.BoundRegistry))
            }
        }
    }

    private fun Method.usesStrings(
        bridge: DexKitBridge,
        strings: Array<String>,
    ): Boolean {
        val targetClass = declaringClass
        val targetName = name
        val targetParameterCount = parameterCount
        return bridge.findMethod {
            matcher {
                declaredClass(targetClass)
                usingStrings(*strings)
            }
        }.any { candidate ->
            candidate.name == targetName &&
                candidate.paramTypes.size == targetParameterCount
        }
    }

    private fun uniqueCandidate(label: String, candidates: List<Method>): Method? {
        if (candidates.size > 1) {
            XposedLogger.logW(
                "Phenotype registry $label discovery is ambiguous: " +
                    candidates.joinToString { method ->
                        "${method.declaringClass.name}#${method.name}"
                    }
            )
        }
        return candidates.singleOrNull()
    }

    private fun loadClass(className: String): Class<*>? =
        sequenceOf(runtimeClassLoader, lpparam.classLoader).firstNotNullOfOrNull { loader ->
            runCatching { Class.forName(className, false, loader) }.getOrNull()
        }

    private fun overrideRegistryValue(
        param: XC_MethodHook.MethodHookParam,
        identitySource: IdentitySource,
    ) {
        if (param.hasThrowable()) return
        val (packageName, flagName) = when (identitySource) {
            IdentitySource.ExplicitArguments -> {
                val packageName = param.args.getOrNull(0) as? String ?: return
                val flagName = param.args.getOrNull(1) as? String ?: return
                packageName to flagName
            }

            IdentitySource.CombinedArgument -> {
                val identity = param.args.getOrNull(0) as? String ?: return
                val separatorIndex = identity.indexOf(COMBINED_IDENTITY_SEPARATOR)
                if (separatorIndex <= 0 || separatorIndex == identity.lastIndex) return
                identity.substring(0, separatorIndex) to identity.substring(separatorIndex + 1)
            }

            IdentitySource.BoundRegistry -> {
                val receiver = param.thisObject ?: return
                val packageName = BoundPhenotypeRegistryIdentityResolver
                    .findPackageName(receiver)
                    ?: return
                val flagName = param.args.getOrNull(0) as? String ?: return
                packageName to flagName
            }
        }
        val match = overrideStore.findBestMatch(packageName, context.packageName, flagName)
        val override = match.override ?: run {
            debugLogger.log(
                "override-miss identity=$packageName/$flagName, " +
                    "context=${context.packageName}, ${overrideStore.describeForLog()}"
            )
            return
        }
        val value = RuntimeFlagOverrideValueParser.parse(null, override) ?: return
        val result = param.result ?: return
        val valueConstructor = valueConstructorCache[result.javaClass]
            ?: PhenotypeRegistryReflection.findValueConstructor(result.javaClass)
                ?.let { found ->
                    valueConstructorCache.putIfAbsent(result.javaClass, found) ?: found
                }
            ?: return
        val booleanMetadata = PhenotypeRegistryReflection.readBooleanMetadata(result)
        val replacementKey = ReplacementKey(
            valueClass = result.javaClass,
            packageName = packageName,
            flagName = flagName,
            flagType = override.flagType,
            value = override.value,
            booleanMetadata = booleanMetadata,
        )
        val replacement = replacementCache[replacementKey]
            ?: createReplacement(
                valueConstructor,
                packageName,
                flagName,
                override,
                value,
                booleanMetadata,
            )
                ?.let { created -> replacementCache.putIfAbsent(replacementKey, created) ?: created }
            ?: return

        param.result = replacement
        diagnostics.overrideApplied(
            diagnosticName,
            "$packageName/$flagName",
        )
        if (loggedOverrides.add("$packageName/$flagName")) {
            XposedLogger.logD(
                "Phenotype registry override applied: $packageName/$flagName=$value " +
                    "(source=${match.source})"
            )
        }
    }

    private fun createReplacement(
        valueConstructor: PhenotypeRegistryReflection.ValueConstructor,
        packageName: String,
        flagName: String,
        override: RuntimeFlagOverrideStore.Override,
        value: Any,
        booleanMetadata: Boolean,
    ): Any? {
        val constructor = valueConstructor.constructor
        val supplierType = constructor.parameterTypes[0]
        val supplierMethod = PhenotypeRegistryReflection.findSupplierMethod(supplierType) ?: return null
        val supplier = Proxy.newProxyInstance(
            supplierType.classLoader,
            arrayOf(supplierType),
        ) { proxy, method, args ->
            when (method.name) {
                ObjectMethodNames.TO_STRING -> "GMS Flags registry override($packageName/$flagName)"
                ObjectMethodNames.HASH_CODE -> System.identityHashCode(proxy)
                ObjectMethodNames.EQUALS -> proxy === args?.firstOrNull()
                else -> if (method == supplierMethod) {
                    if (loggedConsumedOverrides.add("$packageName/$flagName")) {
                        diagnostics.overrideConsumed(
                            diagnosticName,
                            "$packageName/$flagName",
                        )
                        XposedLogger.logD(
                            "Phenotype registry override consumed: " +
                                "$packageName/$flagName=$value"
                        )
                    }
                    value
                } else {
                    null
                }
            }
        }

        return runCatching {
            PhenotypeRegistryReflection.createValue(
                valueConstructor,
                supplier,
                override.flagType,
                booleanMetadata,
            )
        }.getOrElse { error ->
            XposedLogger.logW(
                "Failed to create Phenotype registry override for $packageName/$flagName: " +
                    error.message
            )
            null
        }
    }

    private companion object {
        val REGISTRY_DISCOVERY_ANCHORS = listOf(
            arrayOf("No known flag ", ", known flags: "),
            arrayOf("Unknown package "),
            arrayOf("Bad flag format for ", "No known flag "),
        )
        val COMBINED_REGISTRY_ANCHORS = arrayOf(
            "Bad flag format for ",
            "No known flag ",
            ", known flags: ",
            "Unknown package ",
        )
        val BOUND_REGISTRY_ANCHORS = arrayOf(
            "No known flag ",
            ", known flags: ",
            "tiktok.directboot",
        )
        const val COMBINED_IDENTITY_SEPARATOR = ' '
    }

    private data class DiscoveredLookup(
        val method: Method,
        val identitySource: IdentitySource,
    )

    private enum class IdentitySource(val description: String) {
        ExplicitArguments("explicit package/flag"),
        CombinedArgument("combined package/flag argument"),
        BoundRegistry("bound package + explicit flag"),
    }

    private data class ReplacementKey(
        val valueClass: Class<*>,
        val packageName: String,
        val flagName: String,
        val flagType: Int,
        val value: String,
        val booleanMetadata: Boolean,
    )
}
