package ua.polodarb.gmsflags.presentation.feature.suggestions.ui.components.feed

import ua.polodarb.gmsflags.presentation.feature.suggestions.ui.components.cards.HomeInfoBlockCard
import ua.polodarb.gmsflags.presentation.feature.suggestions.ui.components.cards.RecommendationCard
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardDoubleArrowDown
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.LocalGmsAdaptiveLayout
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Surface
import ua.polodarb.gmsflags.presentation.feature.suggestions.R
import ua.polodarb.gmsflags.presentation.feature.suggestions.mvi.SuggestionsFilters
import ua.polodarb.gmsflags.presentation.feature.suggestions.mvi.filteredBy
import ua.polodarb.gmsflags.presentation.feature.suggestions.mvi.filteredByQuery
import ua.polodarb.gmsflags.presentation.feature.suggestions.ui.model.HomeInfoBlockUiModel
import ua.polodarb.gmsflags.presentation.feature.suggestions.ui.model.RecommendationUiModel
import ua.polodarb.gmsflags.presentation.feature.suggestions.ui.model.groupedByApplication

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SuggestionsFeed(
    infoBlocks: List<HomeInfoBlockUiModel>,
    recommendations: List<RecommendationUiModel>,
    searchQuery: String,
    filters: SuggestionsFilters,
    demotedRecommendationIds: Set<Long>,
    onFiltersReset: () -> Unit,
    onRecommendationSelected: (Long) -> Unit,
    onRecommendationDismissedFromTop: (Long) -> Unit,
    modifier: Modifier = Modifier,
    gridState: LazyGridState = rememberLazyGridState(),
) {
    val adaptiveLayout = LocalGmsAdaptiveLayout.current
    val queryMatchedRecommendations = remember(recommendations, searchQuery) {
        recommendations.filteredByQuery(searchQuery)
    }
    val visibleRecommendations = remember(queryMatchedRecommendations, filters) {
        queryMatchedRecommendations.filteredBy(filters)
    }
    val pinnedRecommendations = remember(visibleRecommendations, demotedRecommendationIds) {
        visibleRecommendations.filter { it.pinned && it.id !in demotedRecommendationIds }
    }
    val unpinnedRecommendations = remember(visibleRecommendations, demotedRecommendationIds) {
        visibleRecommendations.filterNot { it.pinned && it.id !in demotedRecommendationIds }
    }
    val ungroupedRecommendations = remember(unpinnedRecommendations) {
        unpinnedRecommendations.filter { it.applicationPackageName == null }
    }
    val appGroups = remember(unpinnedRecommendations) {
        unpinnedRecommendations.groupedByApplication()
    }
    var expandedPackageNames by rememberSaveable(stateSaver = StringSetSaver) {
        mutableStateOf(emptySet<String>())
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(adaptiveLayout.recommendationCardMinWidth),
        state = gridState,
        modifier = modifier
            .fillMaxSize()
            .widthIn(max = adaptiveLayout.listMaxWidth),
        contentPadding = PaddingValues(adaptiveLayout.contentPadding),
        horizontalArrangement = Arrangement.spacedBy(GmsSpacing.Large),
        verticalArrangement = Arrangement.spacedBy(GmsSpacing.Large),
    ) {
        items(
            items = infoBlocks,
            key = { "info_${it.id}" },
            span = { GridItemSpan(maxLineSpan) },
            contentType = { "info" },
        ) { infoBlock ->
            HomeInfoBlockCard(
                infoBlock = infoBlock,
                modifier = Modifier
                    .fillMaxWidth()
                    .animateItem(),
            )
        }

        if (visibleRecommendations.isNotEmpty()) {
            items(
                items = pinnedRecommendations + ungroupedRecommendations,
                key = RecommendationUiModel::id,
                contentType = { "recommendation" },
            ) { recommendation ->
                RecommendationCard(
                    recommendation = recommendation,
                    onClick = { onRecommendationSelected(recommendation.id) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItem(),
                )
            }
            if ((pinnedRecommendations.isNotEmpty() || ungroupedRecommendations.isNotEmpty()) &&
                appGroups.isNotEmpty()
            ) {
                item(key = "hot_stack_divider", span = { GridItemSpan(maxLineSpan) }) {
                    Spacer(modifier = Modifier.animateItem().height(GmsSpacing.Small))
                    HorizontalDivider(
                        modifier = Modifier
                            .animateItem()
                            .padding(horizontal = 48.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                        thickness = 2.dp,
                    )
                    Spacer(modifier = Modifier.animateItem().height(GmsSpacing.Small))
                }
            }
            items(
                items = appGroups,
                key = { "app_group_${it.packageName}" },
                span = { GridItemSpan(maxLineSpan) },
                contentType = { "app_group" },
            ) { group ->
                RecommendationAppGroupSection(
                    group = group,
                    expanded = group.packageName in expandedPackageNames,
                    onToggle = {
                        expandedPackageNames = if (group.packageName in expandedPackageNames) {
                            expandedPackageNames - group.packageName
                        } else {
                            expandedPackageNames + group.packageName
                        }
                    },
                    onRecommendationSelected = onRecommendationSelected,
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItem(),
                )
            }
        } else {
            fullSpanItem(key = "recommendations_empty") {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.surfaceContainer,
                ) {
                    Column(
                        modifier = Modifier.padding(GmsSpacing.Large),
                        verticalArrangement = Arrangement.spacedBy(GmsSpacing.Small),
                    ) {
                        Text(
                            text = stringResource(
                                if (searchQuery.isNotBlank() || !filters.isDefault) {
                                    R.string.suggestions_search_empty_title
                                } else {
                                    R.string.suggestions_empty_title
                                }
                            ),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = stringResource(
                                if (!filters.isDefault) {
                                    R.string.suggestions_filter_empty_description
                                } else if (searchQuery.isNotBlank()) {
                                    R.string.suggestions_search_empty_description
                                } else {
                                    R.string.suggestions_empty_description
                                }
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        if (!filters.isDefault) {
                            TextButton(onClick = onFiltersReset) {
                                Text(stringResource(R.string.suggestions_filter_reset_all))
                            }
                        }
                    }
                }
            }
        }
    }
}

private val StringSetSaver = listSaver<Set<String>, String>(
    save = { it.toList() },
    restore = { it.toSet() },
)

private fun LazyGridScope.fullSpanItem(
    key: Any,
    content: @Composable () -> Unit,
) {
    item(key = key, span = { GridItemSpan(maxLineSpan) }) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) { content() }
    }
}

/**
 * Wraps a pinned [RecommendationCard] with swipe-to-dismiss: swiping in either direction demotes
 * it into the regular per-app stack, revealing a "moved to stack" hint underneath as it moves.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDemoteRecommendationCard(
    recommendation: RecommendationUiModel,
    onClick: () -> Unit,
    onDemoted: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value != SwipeToDismissBoxValue.Settled) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onDemoted()
            }
            false
        },
    )

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier.fillMaxWidth(),
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(MaterialTheme.shapes.large)
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .padding(horizontal = GmsSpacing.Large),
                contentAlignment = when (dismissState.dismissDirection) {
                    SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                    else -> Alignment.CenterStart
                },
            ) {
                Icon(
                    imageVector = Icons.Outlined.KeyboardDoubleArrowDown,
                    contentDescription = stringResource(
                        R.string.suggestions_recommendation_moved_to_stack_hint,
                    ),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
        },
    ) {
        RecommendationCard(
            recommendation = recommendation,
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
