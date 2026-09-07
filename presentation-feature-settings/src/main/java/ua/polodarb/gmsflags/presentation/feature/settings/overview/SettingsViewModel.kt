package ua.polodarb.gmsflags.presentation.feature.settings.overview

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ua.polodarb.gmsflags.domain.settings.ObserveOverrideControl
import ua.polodarb.gmsflags.domain.settings.RefreshOverrideControl
import ua.polodarb.gmsflags.domain.hookstatus.GetHookStatus
import ua.polodarb.gmsflags.domain.server.content.GetHomeContent
import ua.polodarb.gmsflags.domain.servermode.ObserveServerMode
import ua.polodarb.gmsflags.presentation.core.viewmodel.BaseViewModel
import ua.polodarb.gmsflags.presentation.core.error.ErrorResolver
import ua.polodarb.gmsflags.analytics.AnalyticsEvent
import ua.polodarb.gmsflags.analytics.AnalyticsTracker

internal class SettingsViewModel(
    observeOverrideControl: ObserveOverrideControl,
    private val refreshOverrideControl: RefreshOverrideControl,
    private val getHookStatus: GetHookStatus,
    private val getHomeContent: GetHomeContent,
    private val analytics: AnalyticsTracker,
    private val errorResolver: ErrorResolver,
    private val observeServerMode: ObserveServerMode,
) : BaseViewModel<SettingsEvent, SettingsState, SettingsEffect>() {
    override fun initialState() = SettingsState()

    init {
        observeServerMode()
            .onEach { mode -> setState { copy(offline = mode.offline, offlineNotice = mode.notice) } }
            .launchIn(viewModelScope)
        observeOverrideControl()
            .onEach { control -> setState { copy(overrideControl = control) } }
            .launchIn(viewModelScope)
        viewModelScope.launch {
            refreshOverrideControl().fold(
                onSuccess = { setState { copy(overrideError = null) } },
                onFailure = { error ->
                    setState { copy(overrideError = errorResolver.resolve(error)) }
                },
            )
        }
        viewModelScope.launch {
            getHookStatus().fold(
                onSuccess = { status ->
                    setState {
                        copy(
                            hookStatus = status,
                            hookStatusLoading = false,
                            hookStatusError = null,
                        )
                    }
                },
                onFailure = { error ->
                    setState {
                        copy(
                            hookStatusLoading = false,
                            hookStatusError = errorResolver.resolve(error),
                        )
                    }
                },
            )
        }
        if (!observeServerMode().value.offline) {
            viewModelScope.launch {
                getHomeContent().fold(
                    onSuccess = {
                        setState { copy(serverConnection = ServerConnectionState.Available) }
                    },
                    onFailure = { error ->
                        setState {
                            copy(
                                serverConnection = ServerConnectionState.Unavailable(
                                    errorResolver.resolve(error),
                                ),
                            )
                        }
                    },
                )
            }
        }
    }

    override fun handleEvent(event: SettingsEvent) {
        when (event) {
            SettingsEvent.HookStatusClicked -> {
                analytics.track(AnalyticsEvent.settingsItemClick("hook_status"))
                setEffect { SettingsEffect.OpenHookStatus }
            }
            SettingsEvent.OverridesClicked -> {
                analytics.track(AnalyticsEvent.settingsItemClick("overrides"))
                setEffect { SettingsEffect.OpenOverrides }
            }
            SettingsEvent.FaqClicked -> {
                analytics.track(AnalyticsEvent.settingsItemClick("faq"))
                setEffect { SettingsEffect.OpenFaq }
            }
            SettingsEvent.SupportClicked -> {
                analytics.track(AnalyticsEvent.settingsItemClick("support"))
            }
        }
    }
}
