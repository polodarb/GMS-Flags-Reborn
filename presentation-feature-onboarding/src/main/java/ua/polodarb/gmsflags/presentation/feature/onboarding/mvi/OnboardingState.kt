package ua.polodarb.gmsflags.presentation.feature.onboarding.mvi

import ua.polodarb.gmsflags.presentation.core.mvi.ViewState

data class OnboardingState(
    val step: OnboardingStep = OnboardingStep.Welcome,
    val requestingRoot: Boolean = false,
    val rootRequestFailed: Boolean = false,
    val notificationRequestDenied: Boolean = false,
    val completing: Boolean = false,
    val completionFailed: Boolean = false,
) : ViewState
