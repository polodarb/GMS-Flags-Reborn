package ua.polodarb.gmsflags.presentation.feature.suggestions.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntSize
import ua.polodarb.gmsflags.analytics.AnalyticsScreen
import ua.polodarb.gmsflags.analytics.TrackScreenView
import ua.polodarb.gmsflags.presentation.core.ui.overscroll.GmsOverscrollRubberBandCoefficient
import ua.polodarb.gmsflags.presentation.core.ui.overscroll.rememberCupertinoOverscrollFactory
import ua.polodarb.gmsflags.presentation.core.ui.loading.GmsPullToRefreshBox
import ua.polodarb.gmsflags.presentation.core.ui.layout.GmsTopLevelScreen
import ua.polodarb.gmsflags.presentation.core.ui.search.GmsSearchHeaderAction
import ua.polodarb.gmsflags.presentation.feature.suggestions.mvi.SuggestionsContentState
import ua.polodarb.gmsflags.presentation.feature.suggestions.mvi.SuggestionsEvent
import ua.polodarb.gmsflags.presentation.feature.suggestions.mvi.SuggestionsState
import ua.polodarb.gmsflags.presentation.feature.suggestions.ui.components.filters.SuggestionsFilterBar
import ua.polodarb.gmsflags.presentation.feature.suggestions.ui.components.search.SuggestionsSearchBar
import ua.polodarb.gmsflags.presentation.feature.suggestions.ui.components.feed.SuggestionsStateContent
import androidx.compose.material3.ExperimentalMaterial3Api

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalFoundationApi::class,
)
@Composable
fun SuggestionsContent(
    state: SuggestionsState,
    onEvent: (SuggestionsEvent) -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
    gridState: LazyGridState = rememberLazyGridState(),
) {
    TrackScreenView(AnalyticsScreen.Suggestions)

    val searchSizeSpec = MaterialTheme.motionScheme.slowSpatialSpec<IntSize>()
    val searchEffectsSpec = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()

    val secondaryRowVisible by rememberScrollingUp(gridState)

    GmsTopLevelScreen(
        onSettingsClick = onSettingsClick,
        modifier = modifier,
        headerAction = {
            GmsSearchHeaderAction(
                searchVisible = state.searchVisible,
                onClick = { onEvent(SuggestionsEvent.SearchToggled) },
            )
        },
        supportingContent = {
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                AnimatedVisibility(
                    visible = state.searchVisible,
                    enter = expandVertically(animationSpec = searchSizeSpec) +
                        fadeIn(animationSpec = searchEffectsSpec),
                    exit = shrinkVertically(animationSpec = searchSizeSpec) +
                        fadeOut(animationSpec = searchEffectsSpec),
                ) {
                    SuggestionsSearchBar(
                        query = state.query,
                        onQueryChanged = { onEvent(SuggestionsEvent.QueryChanged(it)) },
                    )
                }
                val content = state.content
                if (content is SuggestionsContentState.Content) {
                    SuggestionsFilterBar(
                        filters = state.filters,
                        secondaryRowVisible = secondaryRowVisible,
                        onApplicationStatusSelected = {
                            onEvent(SuggestionsEvent.ApplicationStatusFilterSelected(it))
                        },
                        onFiltersApplied = { onEvent(SuggestionsEvent.FiltersApplied(it)) },
                        onReset = { onEvent(SuggestionsEvent.FiltersReset) },
                    )
                }
            }
        },
    ) {
        CompositionLocalProvider(
            LocalOverscrollFactory provides rememberCupertinoOverscrollFactory(
                rubberBandCoefficient = GmsOverscrollRubberBandCoefficient,
                topOverscrollEnabled = false,
            ),
        ) {
            GmsPullToRefreshBox(
                isRefreshing = state.refreshing,
                onRefresh = { onEvent(SuggestionsEvent.Refresh) },
            ) {
                SuggestionsStateContent(
                    content = state.content,
                    searchQuery = state.effectiveQuery,
                    filters = state.filters,
                    demotedRecommendationIds = state.demotedRecommendationIds,
                    onRetry = { onEvent(SuggestionsEvent.Retry) },
                    onFiltersReset = { onEvent(SuggestionsEvent.FiltersReset) },
                    onRecommendationSelected = {
                        onEvent(SuggestionsEvent.RecommendationSelected(it))
                    },
                    onRecommendationDismissedFromTop = {
                        onEvent(SuggestionsEvent.RecommendationDismissedFromTop(it))
                    },
                    gridState = gridState,
                )
            }
        }
    }
}

@Composable
private fun rememberScrollingUp(gridState: LazyGridState): State<Boolean> = remember(gridState) {
    var lastIndex = gridState.firstVisibleItemIndex
    var lastOffset = gridState.firstVisibleItemScrollOffset
    var visible = true
    derivedStateOf {
        val index = gridState.firstVisibleItemIndex
        val offset = gridState.firstVisibleItemScrollOffset
        when {
            !gridState.canScrollForward && !gridState.canScrollBackward -> visible = true
            index == 0 && offset == 0 -> visible = true
            !gridState.canScrollForward -> visible = false
            index != lastIndex -> visible = index < lastIndex
            offset - lastOffset > SCROLL_JITTER_THRESHOLD_PX -> visible = false
            lastOffset - offset > SCROLL_JITTER_THRESHOLD_PX -> visible = true
        }
        lastIndex = index
        lastOffset = offset
        visible
    }
}

private const val SCROLL_JITTER_THRESHOLD_PX = 6
