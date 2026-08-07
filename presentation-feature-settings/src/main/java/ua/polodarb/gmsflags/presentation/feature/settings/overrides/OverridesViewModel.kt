package ua.polodarb.gmsflags.presentation.feature.settings.overrides

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ua.polodarb.gmsflags.domain.settings.DeleteAllOverrides
import ua.polodarb.gmsflags.domain.settings.ObserveOverrideControl
import ua.polodarb.gmsflags.domain.settings.RefreshOverrideControl
import ua.polodarb.gmsflags.domain.settings.SetOverridesPaused
import ua.polodarb.gmsflags.domain.settings.ObserveAnalyticsConsent
import ua.polodarb.gmsflags.domain.settings.SetAnalyticsConsent
import ua.polodarb.gmsflags.presentation.core.viewmodel.BaseViewModel
import ua.polodarb.gmsflags.presentation.core.error.ErrorResolver
import ua.polodarb.gmsflags.analytics.AnalyticsEvent
import ua.polodarb.gmsflags.analytics.AnalyticsTracker

internal class OverridesViewModel(
    observeOverrideControl: ObserveOverrideControl,
    private val refreshOverrideControl: RefreshOverrideControl,
    private val setOverridesPaused: SetOverridesPaused,
    private val deleteAllOverrides: DeleteAllOverrides,
    observeAnalyticsConsent: ObserveAnalyticsConsent,
    private val setAnalyticsConsent: SetAnalyticsConsent,
    private val analytics: AnalyticsTracker,
    private val errorResolver: ErrorResolver,
) : BaseViewModel<OverridesEvent, OverridesState, OverridesEffect>() {
    override fun initialState() = OverridesState()

    init {
        observeOverrideControl()
            .onEach { control -> setState { copy(control = control) } }
            .launchIn(viewModelScope)
        observeAnalyticsConsent()
            .onEach { enabled -> setState { copy(analyticsEnabled = enabled) } }
            .launchIn(viewModelScope)
        viewModelScope.launch { refreshOverrideControl() }
    }

    override fun handleEvent(event: OverridesEvent) {
        when (event) {
            OverridesEvent.PauseClicked -> setState {
                copy(
                    confirmation = if (control.paused) {
                        OverrideConfirmation.Resume
                    } else {
                        OverrideConfirmation.Pause
                    }
                )
            }
            OverridesEvent.DeleteAllClicked -> setState {
                copy(confirmation = OverrideConfirmation.DeleteAll)
            }
            OverridesEvent.ConfirmationDismissed -> setState { copy(confirmation = null) }
            OverridesEvent.ConfirmationAccepted -> confirm()
            OverridesEvent.ImportClicked -> setEffect { OverridesEffect.OpenImport }
            is OverridesEvent.AnalyticsConsentToggled -> setAnalyticsConsent(event.enabled)
        }
    }

    private fun confirm() {
        if (viewState.value.busy) return
        val confirmation = viewState.value.confirmation ?: return
        viewModelScope.launch {
            setState { copy(busy = true, confirmation = null) }
            val result = when (confirmation) {
                OverrideConfirmation.Pause -> setOverridesPaused(true)
                OverrideConfirmation.Resume -> setOverridesPaused(false)
                OverrideConfirmation.DeleteAll -> deleteAllOverrides()
            }
            setState { copy(busy = false) }
            result.fold(
                onSuccess = {
                    when (confirmation) {
                        OverrideConfirmation.Pause ->
                            analytics.track(AnalyticsEvent.overridesPaused(true))
                        OverrideConfirmation.Resume ->
                            analytics.track(AnalyticsEvent.overridesPaused(false))
                        OverrideConfirmation.DeleteAll ->
                            analytics.track(
                                AnalyticsEvent.overridesRemovedAll(
                                    viewState.value.control.overrideCount,
                                ),
                            )
                    }
                    setEffect {
                        if (confirmation == OverrideConfirmation.DeleteAll) OverridesEffect.Deleted
                        else OverridesEffect.Updated
                    }
                },
                onFailure = { error ->
                    setEffect { OverridesEffect.Failed(errorResolver.resolve(error)) }
                },
            )
        }
    }
}
