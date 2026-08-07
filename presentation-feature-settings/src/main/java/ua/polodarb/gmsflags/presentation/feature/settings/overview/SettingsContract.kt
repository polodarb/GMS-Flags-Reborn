package ua.polodarb.gmsflags.presentation.feature.settings.overview

import ua.polodarb.gmsflags.domain.settings.OverrideControlState
import ua.polodarb.gmsflags.domain.hookstatus.HookStatusOverview
import ua.polodarb.gmsflags.presentation.core.mvi.ViewEvent
import ua.polodarb.gmsflags.presentation.core.mvi.ViewSideEffect
import ua.polodarb.gmsflags.presentation.core.mvi.ViewState
import ua.polodarb.gmsflags.presentation.core.error.UiError

data class SettingsState(
    val overrideControl: OverrideControlState = OverrideControlState(),
    val overrideError: UiError? = null,
    val hookStatus: HookStatusOverview? = null,
    val hookStatusLoading: Boolean = true,
    val hookStatusError: UiError? = null,
    val serverConnection: ServerConnectionState = ServerConnectionState.Checking,
) : ViewState

sealed interface ServerConnectionState {
    data object Checking : ServerConnectionState
    data object Available : ServerConnectionState
    data class Unavailable(val error: UiError) : ServerConnectionState
}

sealed interface SettingsEvent : ViewEvent {
    data object HookStatusClicked : SettingsEvent
    data object OverridesClicked : SettingsEvent
    data object FaqClicked : SettingsEvent
    data object SupportClicked : SettingsEvent
}

sealed interface SettingsEffect : ViewSideEffect {
    data object OpenHookStatus : SettingsEffect
    data object OpenOverrides : SettingsEffect
    data object OpenFaq : SettingsEffect
}
