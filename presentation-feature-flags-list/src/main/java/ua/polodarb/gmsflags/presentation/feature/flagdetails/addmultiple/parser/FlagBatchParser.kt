package ua.polodarb.gmsflags.presentation.feature.flagdetails.addmultiple.parser

import ua.polodarb.gmsflags.domain.flags.FlagOverride
import ua.polodarb.gmsflags.domain.flags.FlagType
import ua.polodarb.gmsflags.presentation.feature.flagdetails.addmultiple.mvi.AddMultipleFlagsState

internal class FlagBatchParser {
    fun preview(state: AddMultipleFlagsState): FlagBatchPreview {
        val parsed = mutableListOf<FlagOverride>()
        var invalidTokenCount = 0

        state.inputs.forEach { (type, input) ->
            input.tokens().forEach { token ->
                token.toOverrideOrNull(type, state.booleanValue)
                    ?.let(parsed::add)
                    ?: invalidTokenCount++
            }
        }

        return FlagBatchPreview(
            overrides = parsed.associateBy { it.type to it.name }.values.toList(),
            invalidTokenCount = invalidTokenCount,
        )
    }

    fun parse(state: AddMultipleFlagsState): List<FlagOverride> = preview(state).let { preview ->
        require(preview.invalidTokenCount == 0)
        preview.overrides
    }

    private fun String.tokens(): List<String> =
        split(WHITESPACE).filter(String::isNotBlank)

    private fun String.toOverrideOrNull(
        type: FlagType,
        booleanValue: Boolean,
    ): FlagOverride? = runCatching {
        if (type == FlagType.Boolean) {
            val flagName = substringBefore('=').trim()
            require(flagName.isNotEmpty())
            val value = if (contains('=')) {
                when (substringAfter('=').trim().lowercase()) {
                    "1", "true" -> "1"
                    "0", "false" -> "0"
                    else -> throw IllegalArgumentException("$flagName is not a boolean")
                }
            } else {
                if (booleanValue) "1" else "0"
            }
            FlagOverride(flagName, type, value)
        } else {
            val parts = split('=', limit = 2)
            require(parts.size == 2 && parts[0].isNotBlank()) {
                "Use name=value for ${type.name.lowercase()} flags"
            }
            val flagName = parts[0].trim()
            val flagValue = parts[1]
            when (type) {
                FlagType.Integer -> require(flagValue.toLongOrNull() != null) {
                    "$flagName is not an integer"
                }
                FlagType.Float -> require(flagValue.toDoubleOrNull() != null) {
                    "$flagName is not a float"
                }
                else -> Unit
            }
            FlagOverride(flagName, type, flagValue)
        }
    }.getOrNull()

    private companion object {
        val WHITESPACE = Regex("\\s+")
    }
}
