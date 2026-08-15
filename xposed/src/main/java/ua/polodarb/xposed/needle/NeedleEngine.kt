package ua.polodarb.xposed.needle

import android.content.Context
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage
import org.luckypray.dexkit.DexKitBridge
import ua.polodarb.xposed.info.XposedConstants
import ua.polodarb.xposed.info.needle.BooleanExpression
import ua.polodarb.xposed.info.needle.EffectKind
import ua.polodarb.xposed.info.needle.HookPoint
import ua.polodarb.xposed.info.needle.NeedleEffectTypeCheck
import ua.polodarb.xposed.info.needle.NeedleEffectTypeRules
import ua.polodarb.xposed.info.needle.NeedleEnvelope
import ua.polodarb.xposed.info.needle.NeedleExpressionEvaluator
import ua.polodarb.xposed.info.needle.NeedleJson
import ua.polodarb.xposed.info.needle.NeedleRecipePayload
import ua.polodarb.xposed.info.needle.NeedleRecipeValidation
import ua.polodarb.xposed.info.needle.NeedleRecipeValidationResult
import ua.polodarb.xposed.info.needle.NeedleSignature
import ua.polodarb.xposed.info.needle.SelectorKind
import ua.polodarb.xposed.info.needle.ValueExpression
import ua.polodarb.xposed.logging.XposedLogger
import ua.polodarb.xposed.runtime.ModuleNativeLibraryLoader
import ua.polodarb.xposed.store.RuntimeFlagOverrideStore
import java.io.File
import java.lang.reflect.Method
import java.util.Base64
import java.util.concurrent.atomic.AtomicBoolean

internal object NeedleEngine {

    fun tryInstall(
        lpparam: XC_LoadPackage.LoadPackageParam,
        classLoader: ClassLoader,
        moduleApkPath: String?,
        trustedPublicKeyBase64: String?,
    ) {
        if (trustedPublicKeyBase64.isNullOrBlank()) {
            XposedLogger.logW(
                "NeedleEngine: no trusted public key configured; skipping all micro-hook recipes",
            )
            return
        }

        val runtimeDirectory = File(lpparam.appInfo.dataDir, XposedConstants.XPOSED_DIR)
        val envelopes = NeedleRecipeStore(runtimeDirectory).findForPackage(lpparam.packageName)
        if (envelopes.isEmpty()) return

        val apkPath = lpparam.appInfo.sourceDir ?: return
        val overrideStore = RuntimeFlagOverrideStore(
            File(runtimeDirectory, XposedConstants.RUNTIME_OVERRIDES_DB_FILE_NAME),
        )
        val appContext = resolveSystemContext()
        val versionCode = resolveVersionCode(appContext, lpparam.packageName)

        val payloads = envelopes.mapNotNull { envelope ->
            decodeAndValidate(envelope, lpparam, trustedPublicKeyBase64, versionCode)
        }
        if (payloads.isEmpty()) return

        val (resourceStringPayloads, dexMethodPayloads) = payloads.partition {
            it.selector.type == SelectorKind.ANDROID_RESOURCE_STRING
        }

        runCatching {
            if (dexMethodPayloads.isNotEmpty()) {
                ModuleNativeLibraryLoader.load(
                    moduleApkPath = requireNotNull(moduleApkPath) { "Xposed module path unavailable; initZygote was not called" },
                    libraryName = XposedConstants.DEXKIT_LIBRARY_NAME,
                )
                DexKitBridge.create(apkPath).use { bridge ->
                    dexMethodPayloads.forEach { payload ->
                        installDexMethodHook(payload, bridge, lpparam, classLoader, overrideStore, appContext)
                    }
                }
            }
            if (resourceStringPayloads.isNotEmpty()) {
                installResourceStringDispatcher(resourceStringPayloads, lpparam.packageName, overrideStore)
            }
        }.onFailure { error ->
            XposedLogger.logE("NeedleEngine: failed for ${lpparam.packageName}", error)
        }
    }

    private fun decodeAndValidate(
        envelope: NeedleEnvelope,
        lpparam: XC_LoadPackage.LoadPackageParam,
        trustedPublicKeyBase64: String,
        versionCode: Long?,
    ): NeedleRecipePayload? {
        val payloadBytes = runCatching { Base64.getDecoder().decode(envelope.payloadBase64) }
            .getOrElse {
                XposedLogger.logW("NeedleEngine: envelope payload is not valid base64")
                return null
            }

        val verifiedSchemaVersion = NeedleSignature.verifiedSchemaVersion(
            payload = payloadBytes,
            signatureBase64 = envelope.signatureBase64,
            publicKeyBase64 = trustedPublicKeyBase64,
        )
        if (verifiedSchemaVersion == null) {
            XposedLogger.logW("NeedleEngine: signature verification failed, skipping recipe")
            return null
        }

        val payload = runCatching {
            NeedleJson.decodeFromString(NeedleRecipePayload.serializer(), String(payloadBytes, Charsets.UTF_8))
        }.getOrElse { error ->
            XposedLogger.logE("NeedleEngine: failed to parse recipe payload", error)
            return null
        }

        if (payload.schemaVersion != verifiedSchemaVersion) {
            XposedLogger.logW(
                "NeedleEngine: recipe declares schema_version ${payload.schemaVersion} but its signature is " +
                    "bound to schema_version $verifiedSchemaVersion, skipping",
            )
            return null
        }

        if (payload.appPackageName != lpparam.packageName) {
            XposedLogger.logW("NeedleEngine: recipe target ${payload.appPackageName} != loaded package ${lpparam.packageName}, skipping")
            return null
        }
        if (payload.processName != null && payload.processName != lpparam.processName) {
            XposedLogger.logW("NeedleEngine: recipe targets process ${payload.processName}, loaded process is ${lpparam.processName}, skipping")
            return null
        }

        val constraint = payload.versionConstraint
        if (constraint != null) {
            if (versionCode == null) {
                XposedLogger.logW(
                    "NeedleEngine: recipe '${payload.codename}' is version-constrained but the target's " +
                        "versionCode could not be determined, skipping",
                )
                return null
            }
            if (!constraint.allows(versionCode)) {
                XposedLogger.logW(
                    "NeedleEngine: recipe '${payload.codename}' does not apply to ${lpparam.packageName} " +
                        "versionCode $versionCode, skipping",
                )
                return null
            }
        }

        val validation = NeedleRecipeValidation.validate(payload)
        if (validation is NeedleRecipeValidationResult.Invalid) {
            XposedLogger.logW("NeedleEngine: recipe '${payload.codename}' failed validation: ${validation.reason}")
            return null
        }

        return payload
    }

    private fun installDexMethodHook(
        payload: NeedleRecipePayload,
        bridge: DexKitBridge,
        lpparam: XC_LoadPackage.LoadPackageParam,
        classLoader: ClassLoader,
        overrideStore: RuntimeFlagOverrideStore,
        appContext: Context?,
    ) {
        val method = when (val resolution = NeedleSelectorResolver.resolve(bridge, payload, classLoader)) {
            is NeedleResolution.Resolved -> resolution.method
            NeedleResolution.NoMatch -> {
                XposedLogger.logW("Needle: recipe '${payload.codename}' matched no method, skipping")
                return
            }
            is NeedleResolution.Ambiguous -> {
                XposedLogger.logW(
                    "Needle: recipe '${payload.codename}' matched ${resolution.count} methods " +
                        "(${resolution.descriptors.take(5)}), refusing to guess",
                )
                return
            }
            is NeedleResolution.InvalidSelector -> {
                XposedLogger.logW("Needle: recipe '${payload.codename}' has an invalid selector: ${resolution.reason}")
                return
            }
            is NeedleResolution.BindFailed -> {
                XposedLogger.logW("Needle: recipe '${payload.codename}' failed to bind: ${resolution.reason}")
                return
            }
        }

        when (val gate = checkResolvedMethod(method, payload)) {
            NeedleEffectTypeCheck.Allowed -> Unit
            is NeedleEffectTypeCheck.Rejected -> {
                XposedLogger.logW(
                    "Needle: recipe '${payload.codename}' resolved to " +
                        "${method.declaringClass.name}#${method.name} but its effect is not type-safe there: " +
                        "${gate.reason}",
                )
                return
            }
        }

        val disabled = AtomicBoolean(false)
        val guardBoxedBoolean = payload.effect.kind == EffectKind.BOOLEAN_RESULT &&
            NeedleEffectTypeRules.requiresBoxedBooleanGuard(method.returnType.name)
        val nullsArgument = payload.effect.kind == EffectKind.ARGUMENT_NULL

        val hook = object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                if (payload.effect.hookPoint != HookPoint.BEFORE) return
                applyEffect(param, payload, lpparam.packageName, overrideStore, appContext, disabled, guardBoxedBoolean)
            }

            override fun afterHookedMethod(param: MethodHookParam) {
                if (nullsArgument && !disabled.get() && param.hasThrowable()) {
                    disabled.set(true)
                    XposedLogger.logW(
                        "Needle: recipe '${payload.codename}' nulled argument ${payload.effect.argumentIndex} and the " +
                            "target method then threw ${param.throwable?.javaClass?.name}; disabling this recipe until " +
                            "the process restarts to avoid a crash loop",
                    )
                    return
                }
                if (payload.effect.hookPoint != HookPoint.AFTER) return
                applyEffect(param, payload, lpparam.packageName, overrideStore, appContext, disabled, guardBoxedBoolean)
            }
        }
        XposedBridge.hookMethod(method, hook)
        XposedLogger.logI("Needle: installed recipe '${payload.codename}' on ${method.declaringClass.name}#${method.name}")
    }

    private fun checkResolvedMethod(method: Method, payload: NeedleRecipePayload): NeedleEffectTypeCheck {
        val returnTypeName = method.returnType.name
        return when (payload.effect.kind) {
            EffectKind.BOOLEAN_RESULT -> NeedleEffectTypeRules.booleanResult(
                returnTypeName = returnTypeName,
                semanticResultType = payload.selector.semanticResultType,
                hookPoint = payload.effect.hookPoint,
            )

            EffectKind.NUMERIC_RESULT -> NeedleEffectTypeRules.numericResult(returnTypeName)

            EffectKind.ARGUMENT_REPLACE -> {
                val index = payload.effect.argumentIndex
                    ?: return NeedleEffectTypeCheck.Rejected("ARGUMENT_REPLACE requires an argument_index")
                val parameterTypes = method.parameterTypes
                if (index < 0 || index >= parameterTypes.size) {
                    return NeedleEffectTypeCheck.Rejected(
                        "argument_index $index is outside the resolved method's ${parameterTypes.size} parameter(s)",
                    )
                }
                val replacement = runCatching {
                    NeedleJson.decodeFromJsonElement(ValueExpression.serializer(), payload.effect.expression)
                }.getOrNull()?.value
                    ?: return NeedleEffectTypeCheck.Rejected("ARGUMENT_REPLACE expression is not a value expression")
                NeedleEffectTypeRules.argumentReplace(parameterTypes[index].name, replacement.valueType)
            }

            EffectKind.ARGUMENT_NULL -> {
                val index = payload.effect.argumentIndex
                    ?: return NeedleEffectTypeCheck.Rejected("ARGUMENT_NULL requires an argument_index")
                val parameterTypes = method.parameterTypes
                if (index < 0 || index >= parameterTypes.size) {
                    return NeedleEffectTypeCheck.Rejected(
                        "argument_index $index is outside the resolved method's ${parameterTypes.size} parameter(s)",
                    )
                }
                NeedleEffectTypeRules.argumentNull(parameterTypes[index].name)
            }

            EffectKind.STRING_RESULT -> NeedleEffectTypeCheck.Rejected(
                "STRING_RESULT is not valid for a DEX_METHOD recipe",
            )
        }
    }

    private fun installResourceStringDispatcher(
        payloads: List<NeedleRecipePayload>,
        packageName: String,
        overrideStore: RuntimeFlagOverrideStore,
    ) {
        val method = NeedleSelectorResolver.resolveResourceGetString() ?: return
        XposedBridge.hookMethod(method, ResourceStringDispatcher(payloads, packageName, overrideStore))
        XposedLogger.logI("Needle: installed resource-string dispatcher for ${payloads.size} recipe(s) in $packageName")
    }

    private fun applyEffect(
        param: XC_MethodHook.MethodHookParam,
        payload: NeedleRecipePayload,
        contextPackageName: String,
        overrideStore: RuntimeFlagOverrideStore,
        appContext: Context?,
        disabled: AtomicBoolean,
        guardBoxedBoolean: Boolean,
    ) {
        if (disabled.get()) return
        runCatching {
            val context = NeedleHookContext(param, contextPackageName, overrideStore, appContext)
            val condition = payload.effect.`when`
            if (condition != null && !NeedleExpressionEvaluator.evaluateBoolean(condition, context)) return@runCatching

            when (payload.effect.kind) {
                EffectKind.BOOLEAN_RESULT -> {
                    if (guardBoxedBoolean && !isBoxedBooleanCompatible(param.result)) {
                        disableAfterTypeMismatch(payload, param.result, disabled)
                        return@runCatching
                    }
                    val expression = NeedleJson.decodeFromJsonElement(BooleanExpression.serializer(), payload.effect.expression)
                    param.result = NeedleExpressionEvaluator.evaluateBoolean(expression, context)
                }
                EffectKind.NUMERIC_RESULT -> {
                    val expression = NeedleJson.decodeFromJsonElement(ValueExpression.serializer(), payload.effect.expression)
                    param.result = NeedleExpressionEvaluator.evaluateValue(expression.value, context)
                }
                EffectKind.ARGUMENT_REPLACE -> {
                    val expression = NeedleJson.decodeFromJsonElement(ValueExpression.serializer(), payload.effect.expression)
                    val index = payload.effect.argumentIndex ?: return@runCatching
                    val args = param.args
                    if (args != null && index < args.size) {
                        args[index] = NeedleExpressionEvaluator.evaluateValue(expression.value, context)
                    }
                }
                EffectKind.ARGUMENT_NULL -> {
                    val index = payload.effect.argumentIndex ?: return@runCatching
                    val args = param.args
                    if (args != null && index < args.size) {
                        args[index] = null
                    }
                }
                EffectKind.STRING_RESULT -> {
                }
            }
        }.onFailure { error ->
            disabled.set(true)
            XposedLogger.logE(
                "Needle: effect evaluation failed for '${payload.codename}', leaving original behavior and " +
                    "disabling this recipe until the process restarts",
                error,
            )
        }
    }

    private fun isBoxedBooleanCompatible(originalResult: Any?): Boolean =
        originalResult == null || originalResult is Boolean

    private fun disableAfterTypeMismatch(
        payload: NeedleRecipePayload,
        originalResult: Any?,
        disabled: AtomicBoolean,
    ) {
        disabled.set(true)
        XposedLogger.logW(
            "Needle: recipe '${payload.codename}' expected a Boolean-carrying result but the target returned " +
                "${originalResult?.javaClass?.name}; leaving the original result and disabling this recipe " +
                "until the process restarts",
        )
    }

    private fun resolveSystemContext(): Context? = runCatching {
        val activityThreadClass = Class.forName("android.app.ActivityThread")
        val activityThread = activityThreadClass.getMethod("currentActivityThread").invoke(null)
        activityThreadClass.getMethod("getSystemContext").invoke(activityThread) as? Context
    }.getOrNull()

    private fun resolveVersionCode(context: Context?, packageName: String): Long? = runCatching {
        context?.packageManager?.getPackageInfo(packageName, 0)?.longVersionCode
    }.getOrNull()
}
