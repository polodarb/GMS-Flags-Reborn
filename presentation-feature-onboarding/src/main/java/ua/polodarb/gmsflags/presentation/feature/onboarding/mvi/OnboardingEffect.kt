package ua.polodarb.gmsflags.presentation.feature.onboarding.mvi

import ua.polodarb.gmsflags.presentation.core.mvi.ViewSideEffect

sealed interface OnboardingEffect : ViewSideEffect {
    data object RequestNotificationPermission : OnboardingEffect
}
