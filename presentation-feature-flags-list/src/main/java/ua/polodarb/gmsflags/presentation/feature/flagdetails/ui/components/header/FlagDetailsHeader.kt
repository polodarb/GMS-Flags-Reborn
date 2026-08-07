package ua.polodarb.gmsflags.presentation.feature.flagdetails.ui.components.header

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ua.polodarb.gmsflags.presentation.feature.flagdetails.R
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.LocalGmsAdaptiveLayout
import ua.polodarb.gmsflags.presentation.core.ui.search.GmsSearchField
import ua.polodarb.gmsflags.presentation.feature.flagdetails.mvi.FlagDetailsEvent
import ua.polodarb.gmsflags.presentation.feature.flagdetails.mvi.FlagDetailsState

@Composable
internal fun FlagDetailsHeader(
    state: FlagDetailsState,
    onEvent: (FlagDetailsEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val adaptiveLayout = LocalGmsAdaptiveLayout.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceBright)
            .windowInsetsPadding(WindowInsets.statusBars),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        FlagDetailsToolbar(state = state, onEvent = onEvent)
        AnimatedVisibility(visible = state.filtersVisible && !state.selectionMode) {
            FlagFilterRow(
                selectedType = state.selectedType,
                selectedFilter = state.filter,
                onSelected = { onEvent(FlagDetailsEvent.FilterSelected(it)) },
            )
        }
        AnimatedVisibility(visible = state.searchVisible && !state.selectionMode) {
            GmsSearchField(
                query = state.query,
                onQueryChange = { onEvent(FlagDetailsEvent.QueryChanged(it)) },
                placeholder = stringResource(R.string.flag_details_search),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = adaptiveLayout.contentPadding,
                        vertical = GmsSpacing.Small,
                    ),
            )
        }
    }
}
