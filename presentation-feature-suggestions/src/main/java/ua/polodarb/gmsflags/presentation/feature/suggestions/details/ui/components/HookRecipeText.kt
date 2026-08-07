package ua.polodarb.gmsflags.presentation.feature.suggestions.details.ui.components

import ua.polodarb.gmsflags.domain.server.content.HookCompareOp
import ua.polodarb.gmsflags.domain.server.content.HookCondition
import ua.polodarb.gmsflags.domain.server.content.HookConditionExpression
import ua.polodarb.gmsflags.domain.server.content.HookEffectExpression
import ua.polodarb.gmsflags.domain.server.content.HookValueSource
import ua.polodarb.gmsflags.domain.server.content.HookValueSourceKind

internal fun HookConditionExpression.toRuleSentences(): List<List<Pair<HookValueSourceKind, String>>> =
    orGroups.map { group -> group.conditions.map { it.left.kind to it.toSentence() } }

private fun HookCondition.toSentence(): String =
    "${left.toDescription()} ${compare.toWord()} ${right.toDescription()}".replaceFirstChar(Char::uppercase)

private fun HookValueSource.toDescription(): String = when (kind) {
    HookValueSourceKind.ORIGINAL_RESULT -> "the original result"
    HookValueSourceKind.ARGUMENT -> "argument #${(index ?: 0) + 1}"
    HookValueSourceKind.CONSTANT -> constantValue.let { value ->
        when {
            value == null -> "(no value)"
            value.isEmpty() -> "an empty value"
            else -> "\"$value\""
        }
    }
    HookValueSourceKind.FLAG_OVERRIDE ->
        "flag $flagName on ${packageName.orEmpty()}"
    HookValueSourceKind.SYSTEM_FEATURE -> "system feature \"$name\""
    HookValueSourceKind.SDK_INT -> "the device's Android SDK version"
    HookValueSourceKind.RESOURCE_ID -> "resource $resourceType/$resourceName in ${packageName.orEmpty()}"
}

private fun HookCompareOp.toWord(): String = when (this) {
    HookCompareOp.EQ -> "equals"
    HookCompareOp.NEQ -> "is not"
    HookCompareOp.LT -> "is less than"
    HookCompareOp.LTE -> "is at most"
    HookCompareOp.GT -> "is greater than"
    HookCompareOp.GTE -> "is at least"
}

internal fun HookEffectExpression.ValueOnly.toSentence(): String = value.toDescription()
