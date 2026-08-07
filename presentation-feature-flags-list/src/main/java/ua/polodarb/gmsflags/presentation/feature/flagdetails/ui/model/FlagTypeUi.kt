package ua.polodarb.gmsflags.presentation.feature.flagdetails.ui.model

import androidx.annotation.StringRes
import ua.polodarb.gmsflags.domain.flags.FlagType
import ua.polodarb.gmsflags.presentation.feature.flagdetails.R

@get:StringRes
internal val FlagType.labelRes: Int
    get() = when (this) {
        FlagType.Boolean -> R.string.flag_type_boolean
        FlagType.Integer -> R.string.flag_type_integer
        FlagType.Float -> R.string.flag_type_float
        FlagType.String -> R.string.flag_type_string
    }

@get:StringRes
internal val FlagType.inputExampleRes: Int
    get() = when (this) {
        FlagType.Boolean -> R.string.flag_type_boolean_example
        FlagType.Integer -> R.string.flag_type_integer_example
        FlagType.Float -> R.string.flag_type_float_example
        FlagType.String -> R.string.flag_type_string_example
    }
