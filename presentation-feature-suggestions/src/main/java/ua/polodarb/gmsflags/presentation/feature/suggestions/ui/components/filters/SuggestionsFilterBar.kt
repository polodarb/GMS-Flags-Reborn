package ua.polodarb.gmsflags.presentation.feature.suggestions.ui.components.filters

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Badge
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing
import ua.polodarb.gmsflags.presentation.core.ui.haptic.rememberHapticClick
import ua.polodarb.gmsflags.presentation.feature.suggestions.R
import ua.polodarb.gmsflags.presentation.feature.suggestions.mvi.SuggestionsApplicationStatusFilter
import ua.polodarb.gmsflags.presentation.feature.suggestions.mvi.SuggestionsFilters

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun SuggestionsFilterBar(
    filters: SuggestionsFilters,
    secondaryRowVisible: Boolean,
    onApplicationStatusSelected: (SuggestionsApplicationStatusFilter) -> Unit,
    onFiltersApplied: (SuggestionsFilters) -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var sheetVisible by remember { mutableStateOf(false) }

    AnimatedVisibility(
        visible = secondaryRowVisible,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = GmsSpacing.Small),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(
                    ButtonGroupDefaults.ConnectedSpaceBetween,
                ),
            ) {
                QuickStatusOptions.forEachIndexed { index, status ->
                    val checked = filters.applicationStatus == status
                    val onToggle = rememberHapticClick(
                        hapticType = if (checked) {
                            HapticFeedbackType.ToggleOff
                        } else {
                            HapticFeedbackType.ToggleOn
                        },
                    ) {
                        onApplicationStatusSelected(
                            if (checked) SuggestionsApplicationStatusFilter.All else status,
                        )
                    }
                    ToggleButton(
                        checked = checked,
                        onCheckedChange = { onToggle() },
                        shapes = if (index == 0) {
                            ButtonGroupDefaults.connectedLeadingButtonShapes()
                        } else {
                            ButtonGroupDefaults.connectedTrailingButtonShapes()
                        },
                        modifier = Modifier.semantics { role = Role.RadioButton },
                    ) {
                        Text(stringResource(status.labelRes))
                    }
                }
            }
            IconButton(onClick = rememberHapticClick { sheetVisible = true }) {
                BadgedBox(
                    badge = {
                        if (filters.advancedFilterCount > 0) {
                            Badge { Text(filters.advancedFilterCount.toString()) }
                        }
                    },
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Tune,
                        contentDescription = stringResource(R.string.suggestions_filter_more),
                    )
                }
            }
        }
    }

    if (sheetVisible) {
        SuggestionsFilterSheet(
            filters = filters,
            onApplicationStatusSelected = onApplicationStatusSelected,
            onFiltersApplied = onFiltersApplied,
            onReset = onReset,
            onDismiss = { sheetVisible = false },
        )
    }
}

private val QuickStatusOptions = listOf(
    SuggestionsApplicationStatusFilter.NotEnabled,
    SuggestionsApplicationStatusFilter.Enabled,
)
