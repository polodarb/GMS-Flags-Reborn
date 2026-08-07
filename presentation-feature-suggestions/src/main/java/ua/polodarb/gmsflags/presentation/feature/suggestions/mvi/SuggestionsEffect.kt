package ua.polodarb.gmsflags.presentation.feature.suggestions.mvi

import ua.polodarb.gmsflags.presentation.core.mvi.ViewSideEffect
import ua.polodarb.gmsflags.presentation.core.error.UiError

sealed interface SuggestionsEffect : ViewSideEffect {
    data class OpenRecommendation(val id: Long) : SuggestionsEffect
    data class ShowError(val error: UiError) : SuggestionsEffect
    data class ShowTopDismissUndo(val recommendationId: Long, val title: String) : SuggestionsEffect
}
