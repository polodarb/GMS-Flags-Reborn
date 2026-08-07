package ua.polodarb.xposed.needle

import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage
import org.luckypray.dexkit.DexKitBridge
import ua.polodarb.xposed.info.XposedConstants
import ua.polodarb.xposed.info.needle.BooleanExpression
import ua.polodarb.xposed.info.needle.EffectKind
import ua.polodarb.xposed.info.needle.HookPoint
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
import java.util.Base64

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

        val payloads = envelopes.mapNotNull { envelope ->
            decodeAndValidate(envelope, lpparam, trustedPublicKeyBase64)
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
                        installDexMethodHook(payload, bridge, lpparam, classLoader, overrideStore)
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
    ): NeedleRecipePayload? {
        val payloadBytes = runCatching { Base64.getDecoder().decode(envelope.payloadBase64) }
            .getOrElse {
                XposedLogger.logW("NeedleEngine: envelope payload is not valid base64")
                return null
            }

        if (!NeedleSignature.verify(payloadBytes, envelope.signatureBase64, trustedPublicKeyBase64)) {
            XposedLogger.logW("NeedleEngine: signature verification failed, skipping recipe")
            return null
        }

        val payload = runCatching {
            NeedleJson.decodeFromString(NeedleRecipePayload.serializer(), String(payloadBytes, Charsets.UTF_8))
        }.getOrElse { error ->
            XposedLogger.logE("NeedleEngine: failed to parse recipe payload", error)
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
    ) {
        val method = NeedleSelectorResolver.resolve(bridge, payload.selector, classLoader) ?: return

        val hook = object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                if (payload.effect.hookPoint != HookPoint.BEFORE) return
                applyEffect(param, payload, lpparam.packageName, overrideStore)
            }
            override fun afterHookedMethod(param: MethodHookParam) {
                if (payload.effect.hookPoint != HookPoint.AFTER) return
                applyEffect(param, payload, lpparam.packageName, overrideStore)
            }
        }
        XposedBridge.hookMethod(method, hook)
        XposedLogger.logI("Needle: installed recipe '${payload.codename}' on ${method.declaringClass.name}#${method.name}")
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
    ) {
        runCatching {
            val context = NeedleHookContext(param, contextPackageName, overrideStore, appContext = null)
            val condition = payload.effect.`when`
            if (condition != null && !NeedleExpressionEvaluator.evaluateBoolean(condition, context)) return@runCatching

            when (payload.effect.kind) {
                EffectKind.BOOLEAN_RESULT -> {
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
                EffectKind.STRING_RESULT -> {
                }
            }
        }.onFailure { error ->
            XposedLogger.logE("Needle: effect evaluation failed for '${payload.codename}', leaving original behavior", error)
        }
    }
}
