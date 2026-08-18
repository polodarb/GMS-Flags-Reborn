package ua.polodarb.xposed.info.needle

import kotlinx.serialization.json.JsonElement
import java.util.Base64

sealed interface NeedleRecipeValidationResult {
    data object Valid : NeedleRecipeValidationResult
    data class Invalid(val reason: String) : NeedleRecipeValidationResult
}

/**
 * Semantic validation beyond what JSON decoding alone guarantees: decoding only proves every enum
 * value is one this build knows about, not that the selector/effect *combination* makes sense
 * (e.g. STRING_RESULT paired with a selector whose return type isn't guaranteed to be String). The
 * engine must fail closed on that instead of installing it anyway.
 */
object NeedleRecipeValidation {
    private const val SUPPORTED_SCHEMA_VERSION = 1
    private const val SCHEMA_VERSION_V2 = 2
    private const val ANDROID_RESOURCE_PACKAGE = "android"

    /** v1 only allows a literal String here - computing it from ARGUMENT/FLAG_OVERRIDE/etc. would
     * reopen the type-confusion risk this effect kind exists to close off. */
    private val SUPPORTED_STRING_RESULT_VALUE_SOURCES = setOf(SourceKind.CONSTANT)
    private const val MAX_STRING_RESULT_LENGTH = 256
    private const val RESOURCE_ID_STRING_TYPE = "string"

    /** The only method_return_type values NeedleSelectorResolver resolves to a concrete Class;
     * everything else falls into its `Any::class.java` catch-all. */
    private val SUPPORTED_DEX_METHOD_RETURN_TYPES = setOf("boolean", "int", "long", "float", "double", "void")

    fun validate(payload: NeedleRecipePayload): NeedleRecipeValidationResult {
        if (payload.schemaVersion !in NeedleProtocol.SUPPORTED_SCHEMA_VERSIONS) {
            return NeedleRecipeValidationResult.Invalid(
                "unsupported schema_version ${payload.schemaVersion}, this engine supports " +
                    "${NeedleProtocol.SUPPORTED_SCHEMA_VERSIONS}",
            )
        }
        if (payload.schemaVersion == SUPPORTED_SCHEMA_VERSION) {
            validateAbsentV2Fields(payload)?.let { return it }
            return when (payload.selector.type) {
                SelectorKind.ANDROID_RESOURCE_STRING -> validateResourceStringRecipe(payload)
                SelectorKind.DEX_METHOD -> validateDexMethodRecipe(payload.selector, payload.effect.kind)
                SelectorKind.VIEW_RESOURCE_ID -> NeedleRecipeValidationResult.Invalid(
                    "selector type VIEW_RESOURCE_ID requires schema_version $SCHEMA_VERSION_V2 and capability " +
                        NeedleCapabilities.VIEW_IMAGE_OVERLAY,
                )
            }
        }
        return validateV2(payload)
    }

    private fun validateAbsentV2Fields(payload: NeedleRecipePayload): NeedleRecipeValidationResult? {
        val selector = payload.selector
        val usesV2Only = selector.classHasMethodsAll.isNotEmpty() ||
            selector.classHasFieldsAll.isNotEmpty() ||
            selector.methodInvokesAll.isNotEmpty() ||
            selector.semanticResultType != null ||
            selector.viewResourceName.isNotBlank() ||
            selector.viewResourcePackage.isNotBlank() ||
            payload.requiredCapabilities.isNotEmpty() ||
            payload.versionConstraint != null ||
            payload.minimumEngineVersion > 1
        return if (usesV2Only) {
            NeedleRecipeValidationResult.Invalid(
                "schema_version $SUPPORTED_SCHEMA_VERSION recipe carries schema_version $SCHEMA_VERSION_V2 fields; " +
                    "a v1-signed payload must not reach v2 behaviour",
            )
        } else {
            null
        }
    }

    private fun validateV2(payload: NeedleRecipePayload): NeedleRecipeValidationResult {
        if (payload.minimumEngineVersion > NeedleProtocol.ENGINE_VERSION) {
            return NeedleRecipeValidationResult.Invalid(
                "recipe requires engine version ${payload.minimumEngineVersion}, this engine is " +
                    "${NeedleProtocol.ENGINE_VERSION}",
            )
        }
        payload.requiredCapabilities.firstOrNull { it !in NeedleCapabilities.SUPPORTED }?.let { unknown ->
            return NeedleRecipeValidationResult.Invalid("recipe requires unknown capability '$unknown'")
        }
        if (payload.revision.isNullOrBlank()) {
            return NeedleRecipeValidationResult.Invalid("schema_version $SCHEMA_VERSION_V2 requires a non-blank revision")
        }
        return when (payload.selector.type) {
            SelectorKind.ANDROID_RESOURCE_STRING -> validateResourceStringRecipe(payload)
            SelectorKind.DEX_METHOD -> validateV2DexMethodRecipe(payload)
            SelectorKind.VIEW_RESOURCE_ID -> validateViewResourceRecipe(payload)
        }
    }

    private fun validateViewResourceRecipe(payload: NeedleRecipePayload): NeedleRecipeValidationResult {
        val selector = payload.selector
        val effect = payload.effect

        if (selectorCarriesMethodShape(selector)) {
            return NeedleRecipeValidationResult.Invalid(
                "a VIEW_RESOURCE_ID selector must carry only view_resource_name/view_resource_package, not dex/method fields",
            )
        }
        if (selector.viewResourceName.isBlank()) {
            return NeedleRecipeValidationResult.Invalid("VIEW_RESOURCE_ID requires a non-blank view_resource_name")
        }
        val pkg = selector.viewResourcePackage
        if (pkg != payload.appPackageName && pkg != ANDROID_RESOURCE_PACKAGE) {
            return NeedleRecipeValidationResult.Invalid(
                "view_resource_package must be the target package '${payload.appPackageName}' or " +
                    "'$ANDROID_RESOURCE_PACKAGE', got '$pkg'",
            )
        }
        if (effect.hookPoint != HookPoint.AFTER) {
            return NeedleRecipeValidationResult.Invalid(
                "a VIEW_RESOURCE_ID effect requires hook_point AFTER; it acts once the view has painted itself",
            )
        }
        if (effect.argumentIndex != null) {
            return NeedleRecipeValidationResult.Invalid("a VIEW_RESOURCE_ID effect does not use argument_index")
        }
        if (effect.`when` != null) {
            return NeedleRecipeValidationResult.Invalid("a VIEW_RESOURCE_ID effect does not support a `when` gate")
        }
        return when (effect.kind) {
            EffectKind.ADD_IMAGE_OVERLAY -> validateImageOverlay(payload, effect)
            EffectKind.HIDE_VIEW -> validateHideView(payload, effect)
            else -> NeedleRecipeValidationResult.Invalid(
                "selector type VIEW_RESOURCE_ID only supports ADD_IMAGE_OVERLAY or HIDE_VIEW, got ${effect.kind}",
            )
        }
    }

    private fun validateImageOverlay(
        payload: NeedleRecipePayload,
        effect: MicroHookEffect,
    ): NeedleRecipeValidationResult {
        if (NeedleCapabilities.VIEW_IMAGE_OVERLAY !in payload.requiredCapabilities) {
            return NeedleRecipeValidationResult.Invalid(
                "ADD_IMAGE_OVERLAY must declare required_capability ${NeedleCapabilities.VIEW_IMAGE_OVERLAY}, " +
                    "so an engine that cannot draw overlays reports app-update-required instead of a silent no-op",
            )
        }
        if (payload.minimumEngineVersion < NeedleCapabilities.IMAGE_OVERLAY_ENGINE_VERSION) {
            return NeedleRecipeValidationResult.Invalid(
                "ADD_IMAGE_OVERLAY must declare minimum_engine_version >= " +
                    "${NeedleCapabilities.IMAGE_OVERLAY_ENGINE_VERSION}, got ${payload.minimumEngineVersion}",
            )
        }
        val overlay = runCatching {
            NeedleJson.decodeFromJsonElement(NeedleImageOverlay.serializer(), effect.expression)
        }.getOrNull()
            ?: return NeedleRecipeValidationResult.Invalid("ADD_IMAGE_OVERLAY expression is not a valid image overlay")
        val imageResult = validateOverlayImage(overlay)
        if (imageResult is NeedleRecipeValidationResult.Invalid) return imageResult
        val tintResult = validateOverlayTint(payload, overlay)
        if (tintResult is NeedleRecipeValidationResult.Invalid) return tintResult
        if (overlay.hideDescendantTextLabels) {
            if (NeedleCapabilities.VIEW_HIDE_DESCENDANT !in payload.requiredCapabilities) {
                return NeedleRecipeValidationResult.Invalid(
                    "an overlay with hide_descendant_text_labels must declare required_capability " +
                        "${NeedleCapabilities.VIEW_HIDE_DESCENDANT}",
                )
            }
            if (payload.minimumEngineVersion < NeedleCapabilities.HIDE_VIEW_ENGINE_VERSION) {
                return NeedleRecipeValidationResult.Invalid(
                    "hide_descendant_text_labels requires minimum_engine_version >= " +
                        "${NeedleCapabilities.HIDE_VIEW_ENGINE_VERSION}, got ${payload.minimumEngineVersion}",
                )
            }
        }
        return NeedleRecipeValidationResult.Valid
    }

    private fun validateHideView(
        payload: NeedleRecipePayload,
        effect: MicroHookEffect,
    ): NeedleRecipeValidationResult {
        if (NeedleCapabilities.VIEW_HIDE_DESCENDANT !in payload.requiredCapabilities) {
            return NeedleRecipeValidationResult.Invalid(
                "HIDE_VIEW must declare required_capability ${NeedleCapabilities.VIEW_HIDE_DESCENDANT}, so an engine " +
                    "that cannot hide views reports app-update-required instead of a silent no-op",
            )
        }
        if (payload.minimumEngineVersion < NeedleCapabilities.HIDE_VIEW_ENGINE_VERSION) {
            return NeedleRecipeValidationResult.Invalid(
                "HIDE_VIEW requires minimum_engine_version >= " +
                    "${NeedleCapabilities.HIDE_VIEW_ENGINE_VERSION}, got ${payload.minimumEngineVersion}",
            )
        }
        val hide = runCatching {
            NeedleJson.decodeFromJsonElement(NeedleHideView.serializer(), effect.expression)
        }.getOrNull()
            ?: return NeedleRecipeValidationResult.Invalid("HIDE_VIEW expression is not a valid hide-view spec")
        if (hide.target == HideViewTarget.RESOURCE_IDS) {
            if (hide.resourceNames.isEmpty()) {
                return NeedleRecipeValidationResult.Invalid(
                    "HIDE_VIEW with target RESOURCE_IDS requires at least one resource_names entry",
                )
            }
            hide.resourceNames.firstOrNull { it.name.isBlank() }?.let {
                return NeedleRecipeValidationResult.Invalid("HIDE_VIEW resource_names entries must have a non-blank name")
            }
        }
        return NeedleRecipeValidationResult.Valid
    }

    private fun validateOverlayTint(
        payload: NeedleRecipePayload,
        overlay: NeedleImageOverlay,
    ): NeedleRecipeValidationResult {
        if (overlay.tint.isEmpty()) return NeedleRecipeValidationResult.Valid
        if (NeedleCapabilities.VIEW_IMAGE_TINT !in payload.requiredCapabilities) {
            return NeedleRecipeValidationResult.Invalid(
                "a tinted overlay must declare required_capability ${NeedleCapabilities.VIEW_IMAGE_TINT}, so an " +
                    "engine that cannot tint reports app-update-required instead of drawing an untinted image",
            )
        }
        if (payload.minimumEngineVersion < NeedleCapabilities.IMAGE_TINT_ENGINE_VERSION) {
            return NeedleRecipeValidationResult.Invalid(
                "a tinted overlay must declare minimum_engine_version >= " +
                    "${NeedleCapabilities.IMAGE_TINT_ENGINE_VERSION}, got ${payload.minimumEngineVersion}",
            )
        }
        if (overlay.tint.any { it.kind == OverlayTintKind.SYSTEM_NIGHT_MODE } &&
            payload.minimumEngineVersion < NeedleCapabilities.IMAGE_TINT_NIGHT_MODE_ENGINE_VERSION
        ) {
            return NeedleRecipeValidationResult.Invalid(
                "a SYSTEM_NIGHT_MODE tint source requires minimum_engine_version >= " +
                    "${NeedleCapabilities.IMAGE_TINT_NIGHT_MODE_ENGINE_VERSION}, got ${payload.minimumEngineVersion}",
            )
        }
        overlay.tint.firstOrNull { !NeedleOverlayTint.isWellFormed(it) }?.let { bad ->
            return NeedleRecipeValidationResult.Invalid(
                "tint source ${bad.kind} is malformed: THEME_ATTRIBUTE must be one of " +
                    "${NeedleOverlayTint.SUPPORTED_THEME_ATTRIBUTES}, FIXED_ARGB must be #AARRGGBB",
            )
        }
        return NeedleRecipeValidationResult.Valid
    }

    private fun validateOverlayImage(overlay: NeedleImageOverlay): NeedleRecipeValidationResult {
        val bytes = runCatching { Base64.getDecoder().decode(overlay.imageBase64) }.getOrNull()
            ?: return NeedleRecipeValidationResult.Invalid("image_base64 is not valid base64")
        if (bytes.isEmpty()) {
            return NeedleRecipeValidationResult.Invalid("image is empty")
        }
        if (bytes.size > NeedleImageOverlayLimits.MAX_IMAGE_BYTES) {
            return NeedleRecipeValidationResult.Invalid(
                "image is ${bytes.size} bytes, exceeds the ${NeedleImageOverlayLimits.MAX_IMAGE_BYTES}-byte limit",
            )
        }
        if (NeedleImageOverlayLimits.detectFormat(bytes) == null) {
            return NeedleRecipeValidationResult.Invalid("image must be PNG or WEBP (checked by magic bytes)")
        }
        if (!NeedleImageOverlayLimits.isRenderDpInRange(overlay.widthDp) ||
            !NeedleImageOverlayLimits.isRenderDpInRange(overlay.heightDp)
        ) {
            return NeedleRecipeValidationResult.Invalid(
                "width_dp/height_dp must be in ${NeedleImageOverlayLimits.MIN_RENDER_DP}.." +
                    "${NeedleImageOverlayLimits.MAX_RENDER_DP}, got ${overlay.widthDp}x${overlay.heightDp}",
            )
        }
        if (!NeedleImageOverlayLimits.isOffsetDpInRange(overlay.offsetXDp) ||
            !NeedleImageOverlayLimits.isOffsetDpInRange(overlay.offsetYDp)
        ) {
            return NeedleRecipeValidationResult.Invalid(
                "offset_x_dp/offset_y_dp must be in -${NeedleImageOverlayLimits.MAX_OFFSET_DP}.." +
                    "${NeedleImageOverlayLimits.MAX_OFFSET_DP}",
            )
        }
        if (!NeedleImageOverlayLimits.isAlphaInRange(overlay.alpha)) {
            return NeedleRecipeValidationResult.Invalid("alpha must be a finite value in 0.0..1.0, got ${overlay.alpha}")
        }
        return NeedleRecipeValidationResult.Valid
    }

    private fun selectorCarriesMethodShape(selector: MicroHookSelector): Boolean =
        selector.classUsingStringsAll.isNotEmpty() ||
            selector.classUsingStringsAny.isNotEmpty() ||
            selector.methodReturnType.isNotBlank() ||
            selector.methodParameterTypes.isNotEmpty() ||
            selector.methodModifiersAll.isNotEmpty() ||
            selector.methodUsingStringsAll.isNotEmpty() ||
            selector.methodUsingStringsAny.isNotEmpty() ||
            selector.classHasMethodsAll.isNotEmpty() ||
            selector.classHasFieldsAll.isNotEmpty() ||
            selector.methodInvokesAll.isNotEmpty() ||
            selector.semanticResultType != null

    private fun validateV2DexMethodRecipe(payload: NeedleRecipePayload): NeedleRecipeValidationResult {
        val selector = payload.selector
        val effect = payload.effect

        val constraint = payload.versionConstraint
            ?: return NeedleRecipeValidationResult.Invalid(
                "schema_version $SCHEMA_VERSION_V2 DEX_METHOD recipes must carry a version_constraint; an " +
                    "obfuscated selector resolved against one target build is not meaningful on another",
            )
        if (constraint.type == VersionConstraintKind.UNBOUNDED) {
            return NeedleRecipeValidationResult.Invalid(
                "version_constraint UNBOUNDED is not allowed for DEX_METHOD recipes",
            )
        }
        val min = constraint.min?.toLongOrNull()
        val max = constraint.max?.toLongOrNull()
        if (min == null || max == null || min > max) {
            return NeedleRecipeValidationResult.Invalid(
                "version_constraint RANGE requires numeric min and max with min <= max, got " +
                    "min=${constraint.min}, max=${constraint.max}",
            )
        }

        if (effect.kind == EffectKind.STRING_RESULT) {
            return NeedleRecipeValidationResult.Invalid(
                "effect kind STRING_RESULT is only supported with selector type ANDROID_RESOURCE_STRING",
            )
        }

        collectSelectorTypeNames(selector).firstOrNull { it.isBlank() || !NeedleTypeNames.isWellFormed(it) }?.let { bad ->
            return NeedleRecipeValidationResult.Invalid(
                "type name '$bad' is not a well-formed type name; effect write-target types are checked " +
                    "separately against the engine's allowlist, but selector match types may be any valid name",
            )
        }
        collectSelectorModifiers(selector).firstOrNull { !NeedleModifierNames.isKnown(it) }?.let { unknown ->
            return NeedleRecipeValidationResult.Invalid(
                "modifier '$unknown' is not one of ${NeedleModifierNames.SUPPORTED}",
            )
        }
        if (selector.classUsingStringsAll.isEmpty() &&
            selector.classUsingStringsAny.isEmpty() &&
            selector.methodUsingStringsAll.isEmpty() &&
            selector.methodUsingStringsAny.isEmpty()
        ) {
            return NeedleRecipeValidationResult.Invalid(
                "a DEX_METHOD selector must carry at least one string anchor; a shape-only selector matches " +
                    "far too much of a large target APK to ever resolve to exactly one method",
            )
        }
        if (selector.methodUsingStringsAll.any { it.isBlank() } || selector.methodUsingStringsAny.any { it.isBlank() } ||
            selector.classUsingStringsAll.any { it.isBlank() } || selector.classUsingStringsAny.any { it.isBlank() }
        ) {
            return NeedleRecipeValidationResult.Invalid("string anchors must not be blank")
        }

        return validateV2Effect(selector, effect)
    }

    private fun validateV2Effect(
        selector: MicroHookSelector,
        effect: MicroHookEffect,
    ): NeedleRecipeValidationResult = when (effect.kind) {
        EffectKind.BOOLEAN_RESULT -> NeedleEffectTypeRules
            .booleanResult(selector.methodReturnType, selector.semanticResultType, effect.hookPoint)
            .asValidationResult()

        EffectKind.NUMERIC_RESULT -> NeedleEffectTypeRules
            .numericResult(selector.methodReturnType)
            .asValidationResult()

        EffectKind.ARGUMENT_REPLACE -> validateV2ArgumentReplace(selector, effect)

        EffectKind.ARGUMENT_NULL -> validateV2ArgumentNull(selector, effect)

        EffectKind.STRING_RESULT -> NeedleRecipeValidationResult.Invalid(
            "effect kind STRING_RESULT is only supported with selector type ANDROID_RESOURCE_STRING",
        )

        EffectKind.ADD_IMAGE_OVERLAY -> NeedleRecipeValidationResult.Invalid(
            "effect kind ADD_IMAGE_OVERLAY is only supported with selector type VIEW_RESOURCE_ID",
        )

        EffectKind.HIDE_VIEW -> NeedleRecipeValidationResult.Invalid(
            "effect kind HIDE_VIEW is only supported with selector type VIEW_RESOURCE_ID",
        )
    }

    private fun validateV2ArgumentNull(
        selector: MicroHookSelector,
        effect: MicroHookEffect,
    ): NeedleRecipeValidationResult {
        if (effect.hookPoint != HookPoint.BEFORE) {
            return NeedleRecipeValidationResult.Invalid(
                "ARGUMENT_NULL requires hook_point BEFORE; nulling an argument after the method has run has no effect",
            )
        }
        val index = effect.argumentIndex
            ?: return NeedleRecipeValidationResult.Invalid("ARGUMENT_NULL requires an argument_index")
        if (index < 0 || index >= selector.methodParameterTypes.size) {
            return NeedleRecipeValidationResult.Invalid(
                "argument_index $index is outside the selector's declared parameter list " +
                    "${selector.methodParameterTypes}",
            )
        }
        return NeedleEffectTypeRules.argumentNull(selector.methodParameterTypes[index]).asValidationResult()
    }

    private fun validateV2ArgumentReplace(
        selector: MicroHookSelector,
        effect: MicroHookEffect,
    ): NeedleRecipeValidationResult {
        if (effect.hookPoint != HookPoint.BEFORE) {
            return NeedleRecipeValidationResult.Invalid(
                "ARGUMENT_REPLACE requires hook_point BEFORE; replacing an argument after the method has run " +
                    "has no effect",
            )
        }
        val index = effect.argumentIndex
            ?: return NeedleRecipeValidationResult.Invalid("ARGUMENT_REPLACE requires an argument_index")
        if (index < 0 || index >= selector.methodParameterTypes.size) {
            return NeedleRecipeValidationResult.Invalid(
                "argument_index $index is outside the selector's declared parameter list " +
                    "${selector.methodParameterTypes}",
            )
        }
        val replacement = runCatching {
            NeedleJson.decodeFromJsonElement(ValueExpression.serializer(), effect.expression)
        }.getOrNull()?.value
            ?: return NeedleRecipeValidationResult.Invalid("ARGUMENT_REPLACE expression is not a value expression")
        if (replacement.source != SourceKind.CONSTANT) {
            return NeedleRecipeValidationResult.Invalid(
                "schema_version $SCHEMA_VERSION_V2 ARGUMENT_REPLACE only accepts a CONSTANT replacement; a value " +
                    "computed at hook time has no statically checkable type and could be written into a " +
                    "parameter it does not fit",
            )
        }
        if (replacement.value == null) {
            return NeedleRecipeValidationResult.Invalid("ARGUMENT_REPLACE replacement value must not be null")
        }
        return NeedleEffectTypeRules
            .argumentReplace(selector.methodParameterTypes[index], replacement.valueType)
            .asValidationResult()
    }

    private fun collectSelectorTypeNames(selector: MicroHookSelector): List<String> = buildList {
        add(selector.methodReturnType)
        addAll(selector.methodParameterTypes)
        selector.semanticResultType?.let(::add)
        selector.classHasMethodsAll.forEach {
            add(it.returnType)
            addAll(it.parameterTypes)
        }
        selector.classHasFieldsAll.forEach { add(it.type) }
        selector.methodInvokesAll.forEach {
            add(it.declaringType)
            add(it.returnType)
            addAll(it.parameterTypes)
        }
    }

    private fun collectSelectorModifiers(selector: MicroHookSelector): List<String> =
        selector.methodModifiersAll + selector.classHasFieldsAll.flatMap { it.modifiersAll }

    private fun NeedleEffectTypeCheck.asValidationResult(): NeedleRecipeValidationResult = when (this) {
        NeedleEffectTypeCheck.Allowed -> NeedleRecipeValidationResult.Valid
        is NeedleEffectTypeCheck.Rejected -> NeedleRecipeValidationResult.Invalid(reason)
    }

    private fun validateResourceStringRecipe(payload: NeedleRecipePayload): NeedleRecipeValidationResult {
        if (payload.effect.kind != EffectKind.STRING_RESULT) {
            return NeedleRecipeValidationResult.Invalid(
                "selector type ANDROID_RESOURCE_STRING only supports effect kind STRING_RESULT, got ${payload.effect.kind}",
            )
        }
        if (payload.effect.hookPoint != HookPoint.AFTER) {
            return NeedleRecipeValidationResult.Invalid(
                "selector type ANDROID_RESOURCE_STRING only supports hook_point AFTER, got ${payload.effect.hookPoint}",
            )
        }
        val condition = payload.effect.`when`
        if (condition == null || !condition.isProperlyResourceAnchored()) {
            return NeedleRecipeValidationResult.Invalid(
                "a STRING_RESULT effect's `when` must have EVERY or_group contain exactly one " +
                    "ARGUMENT[0] EQ RESOURCE_ID(...) condition - an or_group with no such anchor " +
                    "(e.g. a bare FLAG_OVERRIDE check) would, whenever it alone makes the whole " +
                    "expression true, apply to every resource id the process looks up, not just the " +
                    "id(s) the dispatch index tracks for this recipe. Additional AND-conditions are " +
                    "still allowed inside an already-anchored group.",
            )
        }
        condition.dispatchKeyResourceSources().forEach { source ->
            if (source.packageName.isNullOrBlank() || source.resourceName.isNullOrBlank() || source.resourceType != RESOURCE_ID_STRING_TYPE) {
                return NeedleRecipeValidationResult.Invalid(
                    "a RESOURCE_ID dispatch key must have a non-blank package_name and resource_name, " +
                        "and resource_type must be exactly \"$RESOURCE_ID_STRING_TYPE\" " +
                        "(Resources#getString only makes sense for string resources); got " +
                        "package_name=${source.packageName}, resource_type=${source.resourceType}, " +
                        "resource_name=${source.resourceName}",
                )
            }
        }
        return validateStringResultValue(payload.effect.expression)
    }

    private fun validateStringResultValue(expression: JsonElement): NeedleRecipeValidationResult {
        val value = runCatching {
            NeedleJson.decodeFromJsonElement(ValueExpression.serializer(), expression)
        }.getOrNull()?.value
        if (value == null || value.source !in SUPPORTED_STRING_RESULT_VALUE_SOURCES || value.valueType != ConstantValueType.STRING || value.value == null) {
            return NeedleRecipeValidationResult.Invalid(
                "STRING_RESULT's replacement value must be a non-null CONSTANT of value_type STRING " +
                    "(v1 does not allow computing the replacement from ARGUMENT/FLAG_OVERRIDE/etc.)",
            )
        }
        if (value.value.length > MAX_STRING_RESULT_LENGTH) {
            return NeedleRecipeValidationResult.Invalid(
                "STRING_RESULT's replacement value is ${value.value.length} characters, exceeds the $MAX_STRING_RESULT_LENGTH limit",
            )
        }
        return NeedleRecipeValidationResult.Valid
    }

    private fun validateDexMethodRecipe(selector: MicroHookSelector, effectKind: EffectKind): NeedleRecipeValidationResult {
        if (effectKind == EffectKind.STRING_RESULT) {
            return NeedleRecipeValidationResult.Invalid(
                "effect kind STRING_RESULT is only supported with selector type ANDROID_RESOURCE_STRING " +
                    "(a DEX_METHOD selector's resolved return type is not guaranteed to be String)",
            )
        }
        if (effectKind == EffectKind.ARGUMENT_NULL) {
            return NeedleRecipeValidationResult.Invalid(
                "effect kind ARGUMENT_NULL requires schema_version $SCHEMA_VERSION_V2",
            )
        }
        if (effectKind == EffectKind.ADD_IMAGE_OVERLAY) {
            return NeedleRecipeValidationResult.Invalid(
                "effect kind ADD_IMAGE_OVERLAY is only supported with selector type VIEW_RESOURCE_ID",
            )
        }
        if (selector.methodReturnType !in SUPPORTED_DEX_METHOD_RETURN_TYPES) {
            return NeedleRecipeValidationResult.Invalid(
                "selector type DEX_METHOD requires method_return_type to be one of " +
                    "$SUPPORTED_DEX_METHOD_RETURN_TYPES, got '${selector.methodReturnType}'",
            )
        }
        return NeedleRecipeValidationResult.Valid
    }
}

private fun isDispatchKeyCondition(condition: Condition): Boolean =
    condition.compare == CompareOp.EQ &&
        condition.left.source == SourceKind.ARGUMENT &&
        condition.left.index == 0 &&
        condition.right.source == SourceKind.RESOURCE_ID

/** True only if EVERY or_group contains exactly one canonical `ARGUMENT[0] EQ RESOURCE_ID(...)`
 * condition. Without this, an or_group with no such anchor (e.g. a bare FLAG_OVERRIDE check) that
 * alone makes the expression true would apply to every resource id the process looks up, instead
 * of just the id(s) the dispatch index tracks for this recipe. */
fun BooleanExpression.isProperlyResourceAnchored(): Boolean =
    orGroups.isNotEmpty() && orGroups.all { group -> group.andConditions.count(::isDispatchKeyCondition) == 1 }

/** The right-hand side of every canonical `ARGUMENT[0] EQ RESOURCE_ID(...)` condition, one per
 * or_group - callers should check [isProperlyResourceAnchored] first, since this doesn't itself
 * validate well-formedness. */
fun BooleanExpression.dispatchKeyResourceSources(): List<TypedValueSource> =
    orGroups.flatMap { it.andConditions }.filter(::isDispatchKeyCondition).map { it.right }
