package ua.polodarb.gmsflags.presentation.feature.hookstatus.details

import ua.polodarb.gmsflags.domain.hookstatus.HookApplicationStatus
import ua.polodarb.gmsflags.presentation.core.mvi.ViewEvent
import ua.polodarb.gmsflags.presentation.core.mvi.ViewSideEffect
import ua.polodarb.gmsflags.presentation.core.mvi.ViewState
import ua.polodarb.gmsflags.presentation.core.error.UiError

data class HookStatusDetailsState(
    val loading: Boolean = true,
    val status: HookApplicationStatus? = null,
    val error: UiError? = null,
    val restarting: Boolean = false,
    val technicalDetailsExpanded: Boolean = false,
    val deleteOverridesConfirmVisible: Boolean = false,
    val deletingOverrides: Boolean = false,
) : ViewState

sealed interface HookStatusDetailsEvent : ViewEvent {
    data object Refresh : HookStatusDetailsEvent
    data object Retry : HookStatusDetailsEvent
    data object RestartAndCheck : HookStatusDetailsEvent
    data object ShareDiagnostics : HookStatusDetailsEvent
    data object ToggleTechnicalDetails : HookStatusDetailsEvent
    data object DeleteOverridesClicked : HookStatusDetailsEvent
    data object DeleteOverridesDismissed : HookStatusDetailsEvent
    data object DeleteOverridesConfirmed : HookStatusDetailsEvent
}

sealed interface HookStatusDetailsEffect : ViewSideEffect {
    data class LaunchApplication(val androidPackageName: String) : HookStatusDetailsEffect
    data class ShareDiagnostics(val status: HookApplicationStatus) : HookStatusDetailsEffect
    data class ShowError(val error: UiError) : HookStatusDetailsEffect
    data object OverridesDeleted : HookStatusDetailsEffect
}
