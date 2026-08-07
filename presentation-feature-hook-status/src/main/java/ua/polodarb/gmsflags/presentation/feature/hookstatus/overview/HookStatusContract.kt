package ua.polodarb.gmsflags.presentation.feature.hookstatus.overview

import ua.polodarb.gmsflags.domain.hookstatus.HookStatusOverview
import ua.polodarb.gmsflags.presentation.core.mvi.ViewEvent
import ua.polodarb.gmsflags.presentation.core.mvi.ViewSideEffect
import ua.polodarb.gmsflags.presentation.core.mvi.ViewState
import ua.polodarb.gmsflags.presentation.core.error.UiError

data class HookStatusState(
    val loading: Boolean = true,
    val overview: HookStatusOverview? = null,
    val error: UiError? = null,
) : ViewState

sealed interface HookStatusEvent : ViewEvent {
    data object Refresh : HookStatusEvent
    data object Retry : HookStatusEvent
    data class ApplicationClicked(val androidPackageName: String) : HookStatusEvent
}

sealed interface HookStatusEffect : ViewSideEffect {
    data class OpenApplicationDetails(val androidPackageName: String) : HookStatusEffect
}
