package ua.polodarb.gmsflags.presentation.feature.suggestions.ui.components.feed

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ua.polodarb.gmsflags.presentation.core.ui.animation.GmsInitialContentAnimation
import ua.polodarb.gmsflags.presentation.core.ui.loading.GmsLoadingIndicator
import ua.polodarb.gmsflags.presentation.core.ui.state.GmsEmptyContent
import ua.polodarb.gmsflags.presentation.core.ui.state.GmsErrorContent
import ua.polodarb.gmsflags.presentation.feature.suggestions.R
import ua.polodarb.gmsflags.presentation.feature.suggestions.mvi.SuggestionsContentState
import ua.polodarb.gmsflags.presentation.feature.suggestions.mvi.SuggestionsFilters

@Composable
internal fun SuggestionsStateContent(
    content: SuggestionsContentState,
    searchQuery: String,
    filters: SuggestionsFilters,
    demotedRecommendationIds: Set<Long>,
    onRetry: () -> Unit,
    onFiltersReset: () -> Unit,
    onRecommendationSelected: (Long) -> Unit,
    onRecommendationDismissedFromTop: (Long) -> Unit,
    modifier: Modifier = Modifier,
    gridState: LazyGridState = rememberLazyGridState(),
) {
    AnimatedContent(
        targetState = content,
        modifier = modifier.fillMaxSize(),
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        contentKey = { it::class },
        label = "suggestions_state",
    ) { target ->
        when (target) {
            SuggestionsContentState.Loading -> GmsLoadingIndicator(Modifier.fillMaxSize())
            SuggestionsContentState.Empty -> GmsEmptyContent(
                title = stringResource(R.string.suggestions_empty_title),
                description = stringResource(R.string.suggestions_empty_description),
            )
            is SuggestionsContentState.Error -> GmsErrorContent(
                error = target.error,
                onRetry = onRetry,
            )
            is SuggestionsContentState.Content -> {
                GmsInitialContentAnimation(modifier = Modifier.fillMaxSize()) {
                    SuggestionsFeed(
                        infoBlocks = target.infoBlocks,
                        recommendations = target.recommendations,
                        searchQuery = searchQuery,
                        filters = filters,
                        demotedRecommendationIds = demotedRecommendationIds,
                        onFiltersReset = onFiltersReset,
                        onRecommendationSelected = onRecommendationSelected,
                        onRecommendationDismissedFromTop = onRecommendationDismissedFromTop,
                        gridState = gridState,
                    )
                }
            }
        }
    }
}
