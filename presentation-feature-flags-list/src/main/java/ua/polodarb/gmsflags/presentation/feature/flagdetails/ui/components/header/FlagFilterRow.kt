package ua.polodarb.gmsflags.presentation.feature.flagdetails.ui.components.header

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.annotation.StringRes
import ua.polodarb.gmsflags.presentation.core.ui.haptic.rememberHapticClick
import ua.polodarb.gmsflags.domain.flags.FlagType
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.LocalGmsAdaptiveLayout
import ua.polodarb.gmsflags.presentation.feature.flagdetails.mvi.FlagFilter
import ua.polodarb.gmsflags.presentation.feature.flagdetails.R

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
        horizontalArrangement = Arrangement.spacedBy(GmsSpacing.Small),
    ) {
        filters.forEach { filter ->
            FilterChip(
                selected = selectedFilter == filter,
                onClick = rememberHapticClick { onSelected(filter) },
                label = { Text(stringResource(filter.labelRes)) },
            )
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
