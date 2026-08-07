package ua.polodarb.gmsflags.presentation.feature.hookstatus.overview

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import ua.polodarb.gmsflags.domain.hookstatus.GetHookStatus
import ua.polodarb.gmsflags.presentation.core.viewmodel.BaseViewModel
import ua.polodarb.gmsflags.presentation.core.error.ErrorResolver

internal class HookStatusViewModel(
    private val getHookStatus: GetHookStatus,
    private val errorResolver: ErrorResolver,
) : BaseViewModel<HookStatusEvent, HookStatusState, HookStatusEffect>() {
    private var loadJob: Job? = null

    override fun initialState() = HookStatusState()

    init { load() }

    override fun handleEvent(event: HookStatusEvent) {
        when (event) {
            HookStatusEvent.Refresh,
            HookStatusEvent.Retry -> load()
            is HookStatusEvent.ApplicationClicked -> setEffect {
                HookStatusEffect.OpenApplicationDetails(event.androidPackageName)
            }
        }
    }

    private fun load() {
        if (loadJob?.isActive == true) return
        loadJob = viewModelScope.launch {
            setState { copy(loading = overview == null, error = null) }
            getHookStatus().fold(
                onSuccess = { result ->
                    setState { copy(loading = false, overview = result, error = null) }
                },
                onFailure = { failure ->
                    setState {
                        copy(loading = false, error = errorResolver.resolve(failure))
                    }
                },
            )
        }
    }
}
