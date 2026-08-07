package ua.polodarb.gmsflags.presentation.feature.hookstatus.details

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import ua.polodarb.gmsflags.analytics.AnalyticsEvent
import ua.polodarb.gmsflags.analytics.AnalyticsTracker
import ua.polodarb.gmsflags.domain.hookstatus.GetHookStatus
import ua.polodarb.gmsflags.domain.hookstatus.RestartHookTarget
import ua.polodarb.gmsflags.domain.settings.DeleteApplicationOverrides
import ua.polodarb.gmsflags.presentation.core.viewmodel.BaseViewModel
import ua.polodarb.gmsflags.presentation.core.error.ErrorResolver
import ua.polodarb.gmsflags.presentation.core.error.UiError

internal class HookStatusDetailsViewModel(
    private val androidPackageName: String,
    private val getHookStatus: GetHookStatus,
    private val restartHookTarget: RestartHookTarget,
    private val deleteApplicationOverrides: DeleteApplicationOverrides,
    private val errorResolver: ErrorResolver,
    private val analytics: AnalyticsTracker,
) : BaseViewModel<HookStatusDetailsEvent, HookStatusDetailsState, HookStatusDetailsEffect>() {
    private var loadJob: Job? = null

    override fun initialState() = HookStatusDetailsState()

    init { load() }

    override fun handleEvent(event: HookStatusDetailsEvent) {
        when (event) {
            HookStatusDetailsEvent.Refresh,
            HookStatusDetailsEvent.Retry -> load()
            HookStatusDetailsEvent.RestartAndCheck -> restartAndCheck()
            HookStatusDetailsEvent.ShareDiagnostics -> viewState.value.status?.let { status ->
                setEffect { HookStatusDetailsEffect.ShareDiagnostics(status) }
            }
            HookStatusDetailsEvent.ToggleTechnicalDetails -> setState {
                copy(technicalDetailsExpanded = !technicalDetailsExpanded)
            }
            HookStatusDetailsEvent.DeleteOverridesClicked -> setState {
                copy(deleteOverridesConfirmVisible = true)
            }
            HookStatusDetailsEvent.DeleteOverridesDismissed -> setState {
                copy(deleteOverridesConfirmVisible = false)
            }
            HookStatusDetailsEvent.DeleteOverridesConfirmed -> deleteOverrides()
        }
    }

    private fun deleteOverrides() {
        if (viewState.value.deletingOverrides) return
        analytics.track(AnalyticsEvent.hookOverridesDeleted(androidPackageName))
        viewModelScope.launch {
            setState { copy(deleteOverridesConfirmVisible = false, deletingOverrides = true) }
            deleteApplicationOverrides(androidPackageName).fold(
                onSuccess = {
                    setState { copy(deletingOverrides = false) }
                    setEffect { HookStatusDetailsEffect.OverridesDeleted }
                    load()
                },
                onFailure = { failure ->
                    setState { copy(deletingOverrides = false) }
                    setEffect {
                        HookStatusDetailsEffect.ShowError(errorResolver.resolve(failure))
                    }
                },
            )
        }
    }

    private fun load() {
        if (loadJob?.isActive == true) return
        loadJob = viewModelScope.launch {
            setState { copy(loading = status == null, error = null) }
            getHookStatus().fold(
                onSuccess = { overview ->
                    val result = overview.applications.firstOrNull {
                        it.androidPackageName == androidPackageName
                    }
                    if (result != null && viewState.value.status == null) {
                        analytics.track(
                            AnalyticsEvent.hookDetailsOpened(androidPackageName, result.health.name)
                        )
                    }
                    setState {
                        copy(
                            loading = false,
                            status = result,
                            error = if (result == null) UiError.NotFound else null,
                        )
                    }
                },
                onFailure = { failure ->
                    setState {
                        copy(loading = false, error = errorResolver.resolve(failure))
                    }
                },
            )
        }
    }

    private fun restartAndCheck() {
        if (viewState.value.restarting) return
        analytics.track(AnalyticsEvent.hookRestartCheck(androidPackageName))
        viewModelScope.launch {
            setState { copy(restarting = true) }
            restartHookTarget(androidPackageName).fold(
                onSuccess = {
                    setState { copy(restarting = false) }
                    setEffect { HookStatusDetailsEffect.LaunchApplication(androidPackageName) }
                },
                onFailure = { failure ->
                    setState { copy(restarting = false) }
                    setEffect {
                        HookStatusDetailsEffect.ShowError(errorResolver.resolve(failure))
                    }
                },
            )
        }
    }
}
