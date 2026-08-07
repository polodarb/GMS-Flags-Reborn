package ua.polodarb.gmsflags.presentation.feature.settings.faq

import ua.polodarb.gmsflags.domain.server.content.FaqEntry
import ua.polodarb.gmsflags.presentation.core.error.UiError
import ua.polodarb.gmsflags.presentation.core.mvi.ViewEvent
import ua.polodarb.gmsflags.presentation.core.mvi.ViewSideEffect
import ua.polodarb.gmsflags.presentation.core.mvi.ViewState

data class FaqState(
    val loading: Boolean = true,
    val entries: List<FaqEntry> = emptyList(),
    val error: UiError? = null,
) : ViewState

sealed interface FaqEvent : ViewEvent {
    data object Retry : FaqEvent
}

sealed interface FaqEffect : ViewSideEffect
