package ua.polodarb.gmsflags.presentation.feature.suggestions.ui.components.filters

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing
import ua.polodarb.gmsflags.presentation.core.ui.haptic.rememberHapticClick
import ua.polodarb.gmsflags.presentation.feature.suggestions.R
import ua.polodarb.gmsflags.presentation.feature.suggestions.mvi.SuggestionsApplicationStatusFilter
import ua.polodarb.gmsflags.presentation.feature.suggestions.mvi.SuggestionsFilters

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
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(GmsSpacing.Small),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                QuickStatusChip(
                    label = stringResource(R.string.suggestions_filter_not_enabled),
                    filter = SuggestionsApplicationStatusFilter.NotEnabled,
                    selectedFilter = filters.applicationStatus,
                    onSelected = onApplicationStatusSelected,
                )
                QuickStatusChip(
                    label = stringResource(R.string.suggestions_filter_enabled),
                    filter = SuggestionsApplicationStatusFilter.Enabled,
                    selectedFilter = filters.applicationStatus,
                    onSelected = onApplicationStatusSelected,
                )
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

@Composable
private fun QuickStatusChip(
    label: String,
    filter: SuggestionsApplicationStatusFilter,
    selectedFilter: SuggestionsApplicationStatusFilter,
    onSelected: (SuggestionsApplicationStatusFilter) -> Unit,
) {
    val selected = selectedFilter == filter
    FilterChip(
        selected = selected,
        onClick = rememberHapticClick {
            onSelected(if (selected) SuggestionsApplicationStatusFilter.All else filter)
        },
        label = { Text(label) },
    )
}
