package ua.polodarb.gmsflags.presentation.feature.suggestions.mvi

import ua.polodarb.gmsflags.presentation.core.mvi.ViewState
import ua.polodarb.gmsflags.presentation.core.error.UiError
import ua.polodarb.gmsflags.presentation.feature.suggestions.ui.model.RecommendationApplicationUiStatus
import ua.polodarb.gmsflags.presentation.feature.suggestions.ui.model.RecommendationSupportUiModel
import ua.polodarb.gmsflags.presentation.feature.suggestions.ui.model.RecommendationUiModel
import ua.polodarb.gmsflags.presentation.feature.suggestions.ui.model.HomeInfoBlockUiModel

data class SuggestionsState(
    val content: SuggestionsContentState = SuggestionsContentState.Loading,
    val refreshing: Boolean = false,
    val query: String = "",
    val effectiveQuery: String = "",
    val searchVisible: Boolean = false,
    val filters: SuggestionsFilters = SuggestionsFilters(),
    val demotedRecommendationIds: Set<Long> = emptySet(),
) : ViewState

data class SuggestionsFilters(
    val applicationStatus: SuggestionsApplicationStatusFilter =
        SuggestionsApplicationStatusFilter.All,
    val supportStatuses: Set<RecommendationSupportUiModel> = emptySet(),
    val sort: SuggestionsSort = SuggestionsSort.Recommended,
) {
    val isDefault: Boolean
        get() = applicationStatus == SuggestionsApplicationStatusFilter.All &&
            supportStatuses.isEmpty() &&
            sort == SuggestionsSort.Recommended

    val advancedFilterCount: Int
        get() = listOf(
            applicationStatus != SuggestionsApplicationStatusFilter.All,
            supportStatuses.isNotEmpty(),
            sort != SuggestionsSort.Recommended,
        ).count { it }
}

enum class SuggestionsApplicationStatusFilter {
    All,
    NotEnabled,
    Enabled,
    DifferentSetup,
    Unknown,
}

enum class SuggestionsSort {
    Recommended,
    Application,
}

internal fun List<RecommendationUiModel>.filteredByQuery(
    query: String,
): List<RecommendationUiModel> {
    val terms = query.trim()
        .split(Regex("\\s+"))
        .filter(String::isNotBlank)
    if (terms.isEmpty()) return this

    return filter { recommendation ->
        terms.all { term ->
            recommendation.title.contains(term, ignoreCase = true) ||
                recommendation.description?.contains(term, ignoreCase = true) == true ||
                recommendation.applicationName?.contains(term, ignoreCase = true) == true ||
                recommendation.applicationPackageName?.contains(term, ignoreCase = true) == true ||
                recommendation.flagNames.any { flagName ->
                    flagName.contains(term, ignoreCase = true)
                }
        }
    }
}

internal fun List<RecommendationUiModel>.filteredBy(
    filters: SuggestionsFilters,
): List<RecommendationUiModel> {
    val filtered = filter { recommendation ->
        val applicationStatusMatches = when (filters.applicationStatus) {
            SuggestionsApplicationStatusFilter.All -> true
            SuggestionsApplicationStatusFilter.NotEnabled ->
                recommendation.applicationStatus == RecommendationApplicationUiStatus.NotApplied
            SuggestionsApplicationStatusFilter.Enabled ->
                recommendation.applicationStatus == RecommendationApplicationUiStatus.Applied
            SuggestionsApplicationStatusFilter.DifferentSetup ->
                recommendation.applicationStatus ==
                    RecommendationApplicationUiStatus.PartiallyApplied
            SuggestionsApplicationStatusFilter.Unknown ->
                recommendation.applicationStatus == RecommendationApplicationUiStatus.Unavailable
        }
        val supportMatches = filters.supportStatuses.isEmpty() ||
            recommendation.supportStatus in filters.supportStatuses
        applicationStatusMatches && supportMatches
    }

    return when (filters.sort) {
        SuggestionsSort.Recommended -> filtered
        SuggestionsSort.Application -> filtered.sortedWith(
            compareBy<RecommendationUiModel> { it.applicationName == null }
                .thenBy(String.CASE_INSENSITIVE_ORDER) { it.applicationName.orEmpty() }
                .thenBy(String.CASE_INSENSITIVE_ORDER, RecommendationUiModel::title)
        )
    }
}

sealed interface SuggestionsContentState {
    data object Loading : SuggestionsContentState
    data object Empty : SuggestionsContentState
    data class Error(val error: UiError) : SuggestionsContentState
    data class Content(
        val infoBlocks: List<HomeInfoBlockUiModel>,
        val recommendations: List<RecommendationUiModel>,
    ) : SuggestionsContentState
}
