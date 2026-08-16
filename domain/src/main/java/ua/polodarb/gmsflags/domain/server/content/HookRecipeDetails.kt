package ua.polodarb.gmsflags.domain.server.content

data class HookRecipeDetails(
    val appPackageName: String,
    val processName: String?,
    val selector: HookSelectorDetails,
    val effect: HookEffectDetails,
)

enum class HookSelectorKind { DEX_METHOD, ANDROID_RESOURCE_STRING }

data class HookSelectorDetails(
    val kind: HookSelectorKind,
    val classUsingStringsAll: List<String>,
    val classUsingStringsAny: List<String>,
    val methodReturnType: String,
    val methodParameterTypes: List<String>,
    val methodModifiersAll: List<String>,
    val methodUsingStringsAll: List<String>,
    val methodUsingStringsAny: List<String>,
)

enum class HookRuntimePoint { BEFORE, AFTER }
enum class HookEffectKind { BOOLEAN_RESULT, NUMERIC_RESULT, ARGUMENT_REPLACE, ARGUMENT_NULL, STRING_RESULT }
enum class HookCompareOp { EQ, NEQ, LT, LTE, GT, GTE }
enum class HookValueSourceKind {
    ORIGINAL_RESULT, ARGUMENT, CONSTANT, FLAG_OVERRIDE, SYSTEM_FEATURE, SDK_INT, RESOURCE_ID
}

data class HookValueSource(
    val kind: HookValueSourceKind,
    val index: Int? = null,
    val constantValue: String? = null,
    val packageName: String? = null,
    val flagName: String? = null,
    val name: String? = null,
    val resourceType: String? = null,
    val resourceName: String? = null,
)

data class HookCondition(
    val left: HookValueSource,
    val compare: HookCompareOp,
    val right: HookValueSource,
)

data class HookConditionGroup(val conditions: List<HookCondition>)

data class HookConditionExpression(val orGroups: List<HookConditionGroup>)

/** [Conditional] backs [HookEffectKind.BOOLEAN_RESULT] (the same OR/AND tree decides both whether
 * the effect fires and what boolean it forces); every other kind carries a single [ValueOnly]. */
sealed interface HookEffectExpression {
    data class Conditional(val expression: HookConditionExpression) : HookEffectExpression
    data class ValueOnly(val value: HookValueSource) : HookEffectExpression
    data object None : HookEffectExpression
}

data class HookEffectDetails(
    val hookPoint: HookRuntimePoint,
    val kind: HookEffectKind,
    val argumentIndex: Int?,
    val expression: HookEffectExpression,
    /** Optional extra gate - when present and it evaluates false, the effect must not fire at all. */
    val whenExpression: HookConditionExpression?,
)

/**
 * Decodes a hook's signed envelope into its [HookRecipeDetails], for read-only display (e.g. "view
 * instructions"). This is NOT a substitute for [VerifyHookTrust] - implementations decode whatever
 * the envelope carries regardless of whether its signature is valid, so callers must gate showing
 * the result behind the hook's own [HookTrustStatus] wherever that distinction matters.
 */
fun interface DecodeHookRecipe {
    operator fun invoke(hook: RecommendationVariantHook): Result<HookRecipeDetails>
}
