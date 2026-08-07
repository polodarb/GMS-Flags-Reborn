package ua.polodarb.gmsflags.presentation.feature.onboarding

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ua.polodarb.gmsflags.domain.onboarding.CompleteOnboarding
import ua.polodarb.gmsflags.domain.onboarding.RequestRootAccess
import ua.polodarb.gmsflags.presentation.core.viewmodel.BaseViewModel
import ua.polodarb.gmsflags.presentation.feature.onboarding.mvi.OnboardingEffect
import ua.polodarb.gmsflags.presentation.feature.onboarding.mvi.OnboardingEvent
import ua.polodarb.gmsflags.presentation.feature.onboarding.mvi.OnboardingState
import ua.polodarb.gmsflags.presentation.feature.onboarding.mvi.OnboardingStep

internal class OnboardingViewModel(
    private val requestRootAccess: RequestRootAccess,
    private val completeOnboarding: CompleteOnboarding,
) : BaseViewModel<OnboardingEvent, OnboardingState, OnboardingEffect>() {
    override fun initialState() = OnboardingState()

    override fun handleEvent(event: OnboardingEvent) {
        when (event) {
            OnboardingEvent.ContinueFromWelcome -> setState {
                copy(step = OnboardingStep.Disclaimer)
            }
            OnboardingEvent.ContinueFromDisclaimer -> setState {
                copy(step = OnboardingStep.RootAccess)
            }
            OnboardingEvent.Back -> navigateBack()
            OnboardingEvent.RequestRoot -> requestRoot()
            OnboardingEvent.RequestNotifications -> setEffect {
                OnboardingEffect.RequestNotificationPermission
            }
            is OnboardingEvent.NotificationPermissionResult -> {
                if (event.granted) finishOnboarding()
                else setState { copy(notificationRequestDenied = true) }
            }
            OnboardingEvent.SkipNotifications -> finishOnboarding()
        }
    }

    private fun navigateBack() {
        setState {
            when (step) {
                OnboardingStep.Welcome -> this
                OnboardingStep.Disclaimer -> copy(step = OnboardingStep.Welcome)
                OnboardingStep.RootAccess -> copy(
                    step = OnboardingStep.Disclaimer,
                    rootRequestFailed = false,
                )
                OnboardingStep.Notifications -> copy(
                    step = OnboardingStep.RootAccess,
                    notificationRequestDenied = false,
                )
            }
        }
    }

    private fun requestRoot() {
        if (viewState.value.requestingRoot) return
        setState { copy(requestingRoot = true, rootRequestFailed = false) }
        viewModelScope.launch {
            requestRootAccess().fold(
                onSuccess = {
                    setState {
                        copy(
                            step = OnboardingStep.Notifications,
                            requestingRoot = false,
                        )
                    }
                },
                onFailure = {
                    setState { copy(requestingRoot = false, rootRequestFailed = true) }
                },
            )
        }
    }

    private fun finishOnboarding() {
        if (viewState.value.completing) return
        setState { copy(completing = true, completionFailed = false) }
        viewModelScope.launch {
            completeOnboarding().fold(
                onSuccess = { Unit },
                onFailure = {
                    setState { copy(completing = false, completionFailed = true) }
                },
            )
        }
    }
}
