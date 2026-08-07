package ua.polodarb.gmsflags.presentation.feature.settings.overrides

import ua.polodarb.gmsflags.domain.settings.OverrideControlState
import ua.polodarb.gmsflags.presentation.core.mvi.ViewEvent
import ua.polodarb.gmsflags.presentation.core.mvi.ViewSideEffect
import ua.polodarb.gmsflags.presentation.core.mvi.ViewState
import ua.polodarb.gmsflags.presentation.core.error.UiError

enum class OverrideConfirmation { Pause, Resume, DeleteAll }

data class OverridesState(
    val control: OverrideControlState = OverrideControlState(),
    val busy: Boolean = false,
    val confirmation: OverrideConfirmation? = null,
    val analyticsEnabled: Boolean = true,
) : ViewState

sealed interface OverridesEvent : ViewEvent {
    data object PauseClicked : OverridesEvent
    data object DeleteAllClicked : OverridesEvent
    data object ConfirmationDismissed : OverridesEvent
    data object ConfirmationAccepted : OverridesEvent
    data object ImportClicked : OverridesEvent
    data class AnalyticsConsentToggled(val enabled: Boolean) : OverridesEvent
}

sealed interface OverridesEffect : ViewSideEffect {
    data object OpenImport : OverridesEffect
    data object Updated : OverridesEffect
    data object Deleted : OverridesEffect
    data class Failed(val error: UiError) : OverridesEffect
}
