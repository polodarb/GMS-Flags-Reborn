package ua.polodarb.gmsflags.presentation.feature.onboarding.mvi

import ua.polodarb.gmsflags.presentation.core.mvi.ViewEvent

sealed interface OnboardingEvent : ViewEvent {
    data object ContinueFromWelcome : OnboardingEvent
    data object ContinueFromDisclaimer : OnboardingEvent
    data object Back : OnboardingEvent
    data object RequestRoot : OnboardingEvent
    data object RequestNotifications : OnboardingEvent
    data class NotificationPermissionResult(val granted: Boolean) : OnboardingEvent
    data object SkipNotifications : OnboardingEvent
}
