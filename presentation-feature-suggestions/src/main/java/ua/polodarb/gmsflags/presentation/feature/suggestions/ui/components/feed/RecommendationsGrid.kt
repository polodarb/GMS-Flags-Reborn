package ua.polodarb.gmsflags.presentation.feature.suggestions.ui.components.feed

import ua.polodarb.gmsflags.presentation.feature.suggestions.ui.components.cards.RecommendationCard
import ua.polodarb.gmsflags.presentation.feature.suggestions.ui.components.common.expandCollapsePlacementSpec
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
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
import ua.polodarb.gmsflags.presentation.feature.suggestions.ui.model.RecommendationUiModel

@Composable
internal fun RecommendationsGrid(
    items: List<RecommendationUiModel>,
    onRecommendationSelected: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val adaptiveLayout = LocalGmsAdaptiveLayout.current
    val gridState = rememberLazyGridState()

    Box(modifier = modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(adaptiveLayout.recommendationCardMinWidth),
            state = gridState,
            modifier = Modifier
                .widthIn(max = adaptiveLayout.listMaxWidth)
                .fillMaxSize()
                .align(Alignment.TopCenter),
            contentPadding = PaddingValues(adaptiveLayout.contentPadding),
            horizontalArrangement = Arrangement.spacedBy(GmsSpacing.Large),
            verticalArrangement = Arrangement.spacedBy(GmsSpacing.Large),
        ) {
            items(
                items = items,
                key = RecommendationUiModel::id,
                contentType = { "recommendation" },
            ) { recommendation ->
                RecommendationCard(
                    recommendation = recommendation,
                    onClick = { onRecommendationSelected(recommendation.id) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItem(placementSpec = expandCollapsePlacementSpec),
                )
            }
        }
    }
}
