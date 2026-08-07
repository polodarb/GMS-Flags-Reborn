package ua.polodarb.gmsflags.presentation.feature.apps.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.LocalGmsAdaptiveLayout
import ua.polodarb.gmsflags.presentation.core.ui.scrollbar.GridScrollbar
import ua.polodarb.gmsflags.presentation.feature.apps.ui.model.ApplicationUiModel

@Composable
internal fun ApplicationsGrid(
    applications: List<ApplicationUiModel>,
    onApplicationClick: (ApplicationUiModel) -> Unit,
    onScopeHelpClick: (ApplicationUiModel) -> Unit,
    onPairipHelpClick: (ApplicationUiModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    val adaptiveLayout = LocalGmsAdaptiveLayout.current
    val gridState = rememberLazyGridState()

    GridScrollbar(state = gridState, modifier = modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(adaptiveLayout.applicationCardMinWidth),
            state = gridState,
            modifier = Modifier
                .widthIn(max = adaptiveLayout.listMaxWidth)
                .fillMaxWidth()
                .fillMaxHeight()
                .align(Alignment.Center),
            contentPadding = PaddingValues(adaptiveLayout.contentPadding),
            horizontalArrangement = Arrangement.spacedBy(GmsSpacing.Large),
            verticalArrangement = Arrangement.spacedBy(GmsSpacing.Large),
        ) {
            items(
                items = applications,
                key = ApplicationUiModel::androidPackageName,
                contentType = { "application" },
            ) { application ->
                ApplicationCard(
                    application = application,
                    onClick = { onApplicationClick(application) },
                    onScopeHelpClick = { onScopeHelpClick(application) },
                    onPairipHelpClick = { onPairipHelpClick(application) },
                    modifier = Modifier.animateItem(),
                )
            }
        }
    }
}
