package ua.polodarb.gmsflags.presentation.feature.flagdetails.ui.components.header

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.annotation.StringRes
import ua.polodarb.gmsflags.presentation.core.ui.haptic.rememberHapticClick
import ua.polodarb.gmsflags.domain.flags.FlagType
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.LocalGmsAdaptiveLayout
import ua.polodarb.gmsflags.presentation.feature.flagdetails.mvi.FlagFilter
import ua.polodarb.gmsflags.presentation.feature.flagdetails.R

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun FlagFilterRow(
    selectedType: FlagType,
    selectedFilter: FlagFilter,
    onSelected: (FlagFilter) -> Unit,
) {
    val adaptiveLayout = LocalGmsAdaptiveLayout.current
    val filters = if (selectedType == FlagType.Boolean) {
        FlagFilter.entries
    } else {
        listOf(FlagFilter.All, FlagFilter.Changed)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(
                horizontal = adaptiveLayout.contentPadding,
                vertical = GmsSpacing.Small,
            ),
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
    ) {
        filters.forEachIndexed { index, filter ->
            val checked = selectedFilter == filter
            val onToggle = rememberHapticClick(
                hapticType = if (checked) {
                    HapticFeedbackType.ToggleOff
                } else {
                    HapticFeedbackType.ToggleOn
                },
            ) {
                onSelected(filter)
            }
            ToggleButton(
                checked = checked,
                onCheckedChange = { onToggle() },
                shapes = when (index) {
                    0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                    filters.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                },
                modifier = Modifier.semantics { role = Role.RadioButton },
            ) {
                Text(stringResource(filter.labelRes))
            }
        }
    }
}

private val FlagFilter.labelRes: Int
    @StringRes get() = when (this) {
    FlagFilter.All -> R.string.flag_filter_all
    FlagFilter.Changed -> R.string.flag_filter_changed
    FlagFilter.Disabled -> R.string.flag_filter_disabled
    FlagFilter.Enabled -> R.string.flag_filter_enabled
}
