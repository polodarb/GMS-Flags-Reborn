package ua.polodarb.gmsflags.presentation.feature.suggestions.mvi

import ua.polodarb.gmsflags.presentation.core.mvi.ViewEvent

sealed interface SuggestionsEvent : ViewEvent {
    data object Retry : SuggestionsEvent
    data object Refresh : SuggestionsEvent
    data object SearchToggled : SuggestionsEvent
    data object FiltersReset : SuggestionsEvent
    data class QueryChanged(val query: String) : SuggestionsEvent
    data class QueryDebounced(val query: String) : SuggestionsEvent
    data class FiltersApplied(val filters: SuggestionsFilters) : SuggestionsEvent
    data class ApplicationStatusFilterSelected(
        val filter: SuggestionsApplicationStatusFilter,
    ) : SuggestionsEvent
    data class RecommendationSelected(val id: Long) : SuggestionsEvent
    data class RecommendationDismissedFromTop(val id: Long) : SuggestionsEvent
    data class RecommendationTopUndoClicked(val id: Long) : SuggestionsEvent
}
