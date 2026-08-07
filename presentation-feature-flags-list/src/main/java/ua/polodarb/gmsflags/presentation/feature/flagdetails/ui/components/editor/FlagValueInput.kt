package ua.polodarb.gmsflags.presentation.feature.flagdetails.ui.components.editor

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import ua.polodarb.gmsflags.domain.flags.FlagType
import ua.polodarb.gmsflags.presentation.feature.flagdetails.R

@Composable
internal fun FlagValueInput(
    type: FlagType,
    value: String,
    onValueChanged: (String) -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    require(type != FlagType.Boolean)
    val keyboardController = LocalSoftwareKeyboardController.current
    val keyboardType = when (type) {
        FlagType.Integer -> KeyboardType.Number
        FlagType.Float -> KeyboardType.Decimal
        FlagType.String -> KeyboardType.Text
        FlagType.Boolean -> error("Boolean values use a switch")
    }

    OutlinedTextField(
        value = value,
        onValueChange = { candidate ->
            sanitizeFlagValueInput(type, candidate)?.let(onValueChanged)
        },
        modifier = modifier,
        enabled = enabled,
        label = { Text(stringResource(R.string.flag_editor_value)) },
        shape = MaterialTheme.shapes.extraLarge,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = ImeAction.Done,
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                keyboardController?.hide()
                onDone()
            },
        ),
        singleLine = type != FlagType.String,
        maxLines = if (type == FlagType.String) STRING_MAX_LINES else 1,
    )
}

internal fun sanitizeFlagValueInput(type: FlagType, candidate: String): String? = when (type) {
    FlagType.Integer -> candidate.takeIf(INTEGER_INPUT::matches)
    FlagType.Float -> candidate.replace(',', '.').takeIf(FLOAT_INPUT::matches)
    FlagType.String -> candidate
    FlagType.Boolean -> null
}

private val INTEGER_INPUT = Regex("^-?\\d*$")
private val FLOAT_INPUT = Regex("^-?\\d*(\\.\\d*)?$")
private const val STRING_MAX_LINES = 3
