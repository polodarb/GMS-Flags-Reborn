package ua.polodarb.gmsflags.domain.flags

import ua.polodarb.gmsflags.domain.server.content.DecodeHookRecipe
import ua.polodarb.gmsflags.domain.server.content.HookCompareOp
import ua.polodarb.gmsflags.domain.server.content.HookCondition
import ua.polodarb.gmsflags.domain.server.content.HookConditionExpression
import ua.polodarb.gmsflags.domain.server.content.HookConditionGroup
import ua.polodarb.gmsflags.domain.server.content.HookEffectDetails
import ua.polodarb.gmsflags.domain.server.content.HookEffectExpression
import ua.polodarb.gmsflags.domain.server.content.HookEffectKind
import ua.polodarb.gmsflags.domain.server.content.HookRecipeDetails
import ua.polodarb.gmsflags.domain.server.content.HookRuntimePoint
import ua.polodarb.gmsflags.domain.server.content.HookSelectorDetails
import ua.polodarb.gmsflags.domain.server.content.HookSelectorKind
import ua.polodarb.gmsflags.domain.server.content.HookValueSource
import ua.polodarb.gmsflags.domain.server.content.HookValueSourceKind
import ua.polodarb.gmsflags.domain.server.content.RecommendationVariantHook
import ua.polodarb.xposed.info.needle.BooleanExpression
import ua.polodarb.xposed.info.needle.Condition
import ua.polodarb.xposed.info.needle.EffectKind
import ua.polodarb.xposed.info.needle.MicroHookEffect
import ua.polodarb.xposed.info.needle.MicroHookSelector
import ua.polodarb.xposed.info.needle.NeedleJson
import ua.polodarb.xposed.info.needle.NeedleRecipePayload
import ua.polodarb.xposed.info.needle.SelectorKind
import ua.polodarb.xposed.info.needle.SourceKind
import ua.polodarb.xposed.info.needle.TypedValueSource
import ua.polodarb.xposed.info.needle.ValueExpression
import java.util.Base64
import ua.polodarb.xposed.info.needle.CompareOp as NeedleCompareOp
import ua.polodarb.xposed.info.needle.HookPoint as NeedleHookPoint

/**
 * Decodes a hook's signed envelope into [HookRecipeDetails] for display, without checking its
 * signature (see [ua.polodarb.gmsflags.domain.server.content.VerifyHookTrust] for that) - this is
 * a read-only "what does this recipe say" view, deliberately separate from trust.
 */
class DecodeHookRecipeUseCase : DecodeHookRecipe {
    override fun invoke(hook: RecommendationVariantHook): Result<HookRecipeDetails> = runCatching {
        val envelope = hook.envelope ?: error("Hook has no signed envelope to decode")
        val payloadBytes = Base64.getDecoder().decode(envelope.payloadBase64)
        val payload = NeedleJson.decodeFromString(
            NeedleRecipePayload.serializer(),
            String(payloadBytes, Charsets.UTF_8),
        )
        payload.toDomain()
    }
}

private fun NeedleRecipePayload.toDomain(): HookRecipeDetails = HookRecipeDetails(
    appPackageName = appPackageName,
    processName = processName,
    selector = selector.toDomain(),
    effect = effect.toDomain(),
)

private fun MicroHookSelector.toDomain(): HookSelectorDetails = HookSelectorDetails(
    kind = when (type) {
        SelectorKind.DEX_METHOD -> HookSelectorKind.DEX_METHOD
        SelectorKind.ANDROID_RESOURCE_STRING -> HookSelectorKind.ANDROID_RESOURCE_STRING
    },
    classUsingStringsAll = classUsingStringsAll,
    classUsingStringsAny = classUsingStringsAny,
    methodReturnType = methodReturnType,
    methodParameterTypes = methodParameterTypes,
    methodModifiersAll = methodModifiersAll,
    methodUsingStringsAll = methodUsingStringsAll,
    methodUsingStringsAny = methodUsingStringsAny,
)

private fun MicroHookEffect.toDomain(): HookEffectDetails = HookEffectDetails(
    hookPoint = when (hookPoint) {
        NeedleHookPoint.BEFORE -> HookRuntimePoint.BEFORE
        NeedleHookPoint.AFTER -> HookRuntimePoint.AFTER
    },
    kind = when (kind) {
        EffectKind.BOOLEAN_RESULT -> HookEffectKind.BOOLEAN_RESULT
        EffectKind.NUMERIC_RESULT -> HookEffectKind.NUMERIC_RESULT
        EffectKind.ARGUMENT_REPLACE -> HookEffectKind.ARGUMENT_REPLACE
        EffectKind.STRING_RESULT -> HookEffectKind.STRING_RESULT
    },
    argumentIndex = argumentIndex,
    expression = when (kind) {
        EffectKind.BOOLEAN_RESULT -> HookEffectExpression.Conditional(
            NeedleJson.decodeFromJsonElement(BooleanExpression.serializer(), expression).toDomain()
        )
        else -> HookEffectExpression.ValueOnly(
            NeedleJson.decodeFromJsonElement(ValueExpression.serializer(), expression).value.toDomain()
        )
    },
    whenExpression = `when`?.toDomain(),
)

private fun BooleanExpression.toDomain(): HookConditionExpression = HookConditionExpression(
    orGroups = orGroups.map { group ->
        HookConditionGroup(group.andConditions.map { it.toDomain() })
    },
)

private fun Condition.toDomain(): HookCondition =
    HookCondition(left = left.toDomain(), compare = compare.toDomain(), right = right.toDomain())

private fun TypedValueSource.toDomain(): HookValueSource = HookValueSource(
    kind = source.toDomain(),
    index = index,
    constantValue = value,
    packageName = packageName,
    flagName = flagName,
    name = name,
    resourceType = resourceType,
    resourceName = resourceName,
)

private fun SourceKind.toDomain(): HookValueSourceKind = when (this) {
    SourceKind.ORIGINAL_RESULT -> HookValueSourceKind.ORIGINAL_RESULT
    SourceKind.ARGUMENT -> HookValueSourceKind.ARGUMENT
    SourceKind.CONSTANT -> HookValueSourceKind.CONSTANT
    SourceKind.FLAG_OVERRIDE -> HookValueSourceKind.FLAG_OVERRIDE
    SourceKind.SYSTEM_FEATURE -> HookValueSourceKind.SYSTEM_FEATURE
    SourceKind.SDK_INT -> HookValueSourceKind.SDK_INT
    SourceKind.RESOURCE_ID -> HookValueSourceKind.RESOURCE_ID
}

private fun NeedleCompareOp.toDomain(): HookCompareOp = when (this) {
    NeedleCompareOp.EQ -> HookCompareOp.EQ
    NeedleCompareOp.NEQ -> HookCompareOp.NEQ
    NeedleCompareOp.LT -> HookCompareOp.LT
    NeedleCompareOp.LTE -> HookCompareOp.LTE
    NeedleCompareOp.GT -> HookCompareOp.GT
    NeedleCompareOp.GTE -> HookCompareOp.GTE
}
