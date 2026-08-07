package ua.polodarb.gmsflags.presentation.feature.suggestions.ui.components.filters

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsDimensions
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing
import ua.polodarb.gmsflags.presentation.core.ui.haptic.rememberHapticClick
import ua.polodarb.gmsflags.presentation.feature.suggestions.R
import ua.polodarb.gmsflags.presentation.feature.suggestions.mvi.SuggestionsApplicationStatusFilter
import ua.polodarb.gmsflags.presentation.feature.suggestions.mvi.SuggestionsFilters
import ua.polodarb.gmsflags.presentation.feature.suggestions.mvi.SuggestionsSort
import ua.polodarb.gmsflags.presentation.feature.suggestions.ui.model.RecommendationSupportUiModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun SuggestionsFilterSheet(
    filters: SuggestionsFilters,
    onApplicationStatusSelected: (SuggestionsApplicationStatusFilter) -> Unit,
    onFiltersApplied: (SuggestionsFilters) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = MaterialTheme.shapes.extraLarge,
        contentWindowInsets = { WindowInsets.safeDrawing.only(WindowInsetsSides.Top) },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(
                    start = GmsSpacing.ExtraLarge,
                    end = GmsSpacing.ExtraLarge,
                    bottom = GmsSpacing.ExtraLarge,
                ),
            verticalArrangement = Arrangement.spacedBy(GmsSpacing.ExtraLarge),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = GmsDimensions.MinimumTouchTarget),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.suggestions_filter_more),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.weight(1f),
                )
                AnimatedVisibility(
                    visible = !filters.isDefault,
                    enter = scaleIn() + androidx.compose.animation.fadeIn(),
                    exit = scaleOut() + androidx.compose.animation.fadeOut(),
                ) {
                    TextButton(onClick = rememberHapticClick(onClick = onReset)) {
                        Text(stringResource(R.string.suggestions_filter_reset))
                    }
                }
            }

            FilterSection(title = stringResource(R.string.suggestions_filter_support_status)) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(GmsSpacing.Small),
                    verticalArrangement = Arrangement.spacedBy(GmsSpacing.Small),
                ) {
                    RecommendationSupportUiModel.entries.forEach { status ->
                        val selected = status in filters.supportStatuses
                        TonalFilterChip(
                            label = stringResource(status.labelRes),
                            selected = selected,
                            onClick = {
                                val next = if (selected) {
                                    filters.supportStatuses - status
                                } else {
                                    filters.supportStatuses + status
                                }
                                onFiltersApplied(filters.copy(supportStatuses = next))
                            },
                        )
                    }
                }
            }

            FilterSection(title = stringResource(R.string.suggestions_filter_sort)) {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    val sorts = SuggestionsSort.entries
                    sorts.forEachIndexed { index, sort ->
                        SegmentedButton(
                            selected = filters.sort == sort,
                            onClick = rememberHapticClick {
                                onFiltersApplied(filters.copy(sort = sort))
                            },
                            shape = SegmentedButtonDefaults.itemShape(index, sorts.size),
                            label = { Text(stringResource(sort.labelRes)) },
                        )
                    }
                }
            }

            FilterSection(title = stringResource(R.string.suggestions_filter_device_status)) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(GmsSpacing.Small),
                    verticalArrangement = Arrangement.spacedBy(GmsSpacing.Small),
                ) {
                    SuggestionsApplicationStatusFilter.entries.forEach { status ->
                        TonalFilterChip(
                            label = stringResource(status.labelRes),
                            selected = filters.applicationStatus == status,
                            onClick = { onApplicationStatusSelected(status) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterSection(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(GmsSpacing.Small)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
        )
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TonalFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = selected,
        onClick = rememberHapticClick(onClick = onClick),
        label = { Text(label) },
        shape = MaterialTheme.shapes.large,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
            selectedLeadingIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ),
        border = if (selected) null else FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = false,
        ),
        leadingIcon = if (selected) {
            {
                Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(start = GmsSpacing.ExtraSmall)
                        .size(FilterChipDefaults.IconSize),
                )
            }
        } else {
            null
        },
        modifier = Modifier.heightIn(min = GmsDimensions.MinimumTouchTarget),
    )
}
