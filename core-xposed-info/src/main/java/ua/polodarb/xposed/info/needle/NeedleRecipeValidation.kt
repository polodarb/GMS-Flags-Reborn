package ua.polodarb.xposed.info.needle

import kotlinx.serialization.json.JsonElement

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
        }
    }

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

        collectSelectorTypeNames(selector).firstOrNull { !NeedleTypeNames.isKnown(it) }?.let { unknown ->
            return NeedleRecipeValidationResult.Invalid(
                "type name '$unknown' is not in this engine's allowlist ${NeedleTypeNames.ALL}",
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

        EffectKind.STRING_RESULT -> NeedleRecipeValidationResult.Invalid(
            "effect kind STRING_RESULT is only supported with selector type ANDROID_RESOURCE_STRING",
        )
    }

    private fun validateV2ArgumentReplace(
        selector: MicroHookSelector,
        effect: MicroHookEffect,
    ): NeedleRecipeValidationResult {
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
