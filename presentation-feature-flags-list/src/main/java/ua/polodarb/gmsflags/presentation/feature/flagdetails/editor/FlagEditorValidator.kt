package ua.polodarb.gmsflags.presentation.feature.flagdetails.editor

import ua.polodarb.gmsflags.domain.flags.FlagOverride
import ua.polodarb.gmsflags.domain.flags.FlagType
import ua.polodarb.gmsflags.presentation.feature.flagdetails.mvi.FlagDetailsDialog
import ua.polodarb.gmsflags.presentation.feature.flagdetails.mvi.isEnabledBoolean
import ua.polodarb.gmsflags.presentation.feature.flagdetails.R

internal class FlagEditorValidationException(
    val messageRes: Int,
) : IllegalArgumentException()

internal object FlagEditorValidator {
    fun validate(editor: FlagDetailsDialog.Editor): Result<FlagOverride> = validate(
        name = editor.name,
        type = editor.type,
        value = editor.value,
    )

    fun validate(
        name: String,
        type: FlagType,
        value: String,
    ): Result<FlagOverride> = runCatching {
        val normalizedName = name.trim()
        if (normalizedName.isEmpty()) {
            throw FlagEditorValidationException(R.string.message_flag_name_empty)
        }
        val normalizedValue = value.normalizedFor(type)
            ?: throw FlagEditorValidationException(R.string.message_invalid_flag_value)
        FlagOverride(name = normalizedName, type = type, value = normalizedValue)
    }
}

private fun String.normalizedFor(type: FlagType): String? = when (type) {
    FlagType.Boolean -> when {
        isEnabledBoolean() -> "1"
        this == "0" || equals("false", ignoreCase = true) -> "0"
        else -> null
    }
    FlagType.Integer -> trim().takeIf { it.toLongOrNull() != null }
    FlagType.Float -> trim().takeIf { it.toDoubleOrNull() != null }
    FlagType.String -> this
}
