package ua.polodarb.gmsflags.presentation.feature.flagdetails.addmultiple.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ua.polodarb.gmsflags.domain.flags.FlagType
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.LocalGmsAdaptiveLayout
import ua.polodarb.gmsflags.presentation.feature.flagdetails.R
import ua.polodarb.gmsflags.presentation.feature.flagdetails.addmultiple.mvi.AddMultipleFlagsEvent
import ua.polodarb.gmsflags.presentation.feature.flagdetails.addmultiple.mvi.AddMultipleFlagsState
import ua.polodarb.gmsflags.presentation.feature.flagdetails.ui.components.header.FlagTypeTabs
import ua.polodarb.gmsflags.presentation.feature.flagdetails.ui.model.inputExampleRes

@Composable
internal fun AddMultipleFlagForm(
    state: AddMultipleFlagsState,
    onEvent: (AddMultipleFlagsEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val adaptiveLayout = LocalGmsAdaptiveLayout.current
    val input = state.inputs[state.selectedType].orEmpty()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(GmsSpacing.Large),
    ) {
        FlagTypeTabs(
            selectedType = state.selectedType,
            enabled = !state.saving,
            onTypeSelected = { onEvent(AddMultipleFlagsEvent.TypeSelected(it)) },
        )
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = adaptiveLayout.contentPadding),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainer,
        ) {
            Column(
                modifier = Modifier.padding(GmsSpacing.Large),
                verticalArrangement = Arrangement.spacedBy(GmsSpacing.Medium),
            ) {
                Text(
                    text = stringResource(R.string.add_multiple_enter_flags),
                    style = MaterialTheme.typography.titleMedium,
                )
                OutlinedTextField(
                    value = input,
                    onValueChange = {
                        onEvent(AddMultipleFlagsEvent.InputChanged(state.selectedType, it))
                    },
                    enabled = !state.saving,
                    placeholder = {
                        Text(
                            text = if (state.selectedType == FlagType.Boolean) {
                                stringResource(R.string.add_multiple_boolean_hint)
                            } else {
                                stringResource(R.string.add_multiple_value_hint)
                            },
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                .copy(alpha = 0.5f),
                        )
                    },
                    supportingText = {
                        Text(stringResource(state.selectedType.inputExampleRes))
                    },
                    minLines = 5,
                    shape = RoundedCornerShape(GmsSpacing.Huge - GmsSpacing.ExtraSmall),
                    modifier = Modifier.fillMaxWidth(),
                )
                if (state.selectedType == FlagType.Boolean) {
                    Text(
                        text = stringResource(R.string.add_multiple_boolean_value),
                        style = MaterialTheme.typography.titleSmall,
                    )
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        listOf(true, false).forEachIndexed { index, enabled ->
                            SegmentedButton(
                                selected = state.booleanValue == enabled,
                                onClick = {
                                    onEvent(AddMultipleFlagsEvent.BooleanValueChanged(enabled))
                                },
                                enabled = !state.saving,
                                shape = SegmentedButtonDefaults.itemShape(index, 2),
                                label = {
                                    Text(
                                        stringResource(
                                            if (enabled) R.string.add_multiple_enabled
                                            else R.string.add_multiple_disabled
                                        )
                                    )
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}
