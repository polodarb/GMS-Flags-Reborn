package ua.polodarb.gmsflags.presentation.feature.apps.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ua.polodarb.gmsflags.analytics.AnalyticsEvent
import ua.polodarb.gmsflags.analytics.AnalyticsTracker
import ua.polodarb.gmsflags.domain.apps.GetSupportedApplicationsSnapshot
import ua.polodarb.gmsflags.domain.servermode.ObserveServerMode
import ua.polodarb.gmsflags.presentation.core.viewmodel.BaseViewModel
import ua.polodarb.gmsflags.presentation.core.error.ErrorResolver
import ua.polodarb.gmsflags.presentation.feature.apps.mvi.AppsEffect
import ua.polodarb.gmsflags.presentation.feature.apps.mvi.AppsEvent
import ua.polodarb.gmsflags.presentation.feature.apps.mvi.AppsState
import ua.polodarb.gmsflags.presentation.feature.apps.ui.model.toUiModel

class AppsViewModel(
    private val getApplicationsSnapshot: GetSupportedApplicationsSnapshot,
    private val errorResolver: ErrorResolver,
    private val analytics: AnalyticsTracker,
    private val observeServerMode: ObserveServerMode,
) : BaseViewModel<AppsEvent, AppsState, AppsEffect>() {
    private var loadJob: Job? = null
    private var searchDebounceJob: Job? = null

    override fun initialState() = AppsState()

    init {
        observeServerMode()
            .onEach { mode ->
                setState { copy(offline = mode.offline, offlineBadge = mode.notice?.badge) }
            }
            .launchIn(viewModelScope)
        load()
    }

    override fun handleEvent(event: AppsEvent) {
        when (event) {
            AppsEvent.Retry -> load()
            AppsEvent.Resumed -> load()
            AppsEvent.Refresh -> load(refresh = true)
            AppsEvent.SettingsClicked -> setEffect { AppsEffect.OpenHookStatus }
            is AppsEvent.ApplicationClicked -> openApplication(event.androidPackageName)
            is AppsEvent.ScopeHelpClicked -> setState {
                copy(
                    scopeHelpApplication = applications.firstOrNull {
                        it.androidPackageName == event.androidPackageName
                    }
                )
            }
            AppsEvent.ScopeHelpDismissed -> setState { copy(scopeHelpApplication = null) }
            is AppsEvent.PairipHelpClicked -> setState {
                copy(
                    pairipHelpApplication = applications.firstOrNull {
                        it.androidPackageName == event.androidPackageName
                    }
                )
            }
            AppsEvent.PairipHelpDismissed -> setState { copy(pairipHelpApplication = null) }
            AppsEvent.ModuleScopeHelpClicked -> setState { copy(moduleScopeHelpVisible = true) }
            AppsEvent.ModuleScopeHelpDismissed -> setState { copy(moduleScopeHelpVisible = false) }
            AppsEvent.UnsupportedDismissed -> setState { copy(unsupportedApplication = null) }
            is AppsEvent.QueryChanged -> debounceSearch(event.query)
            is AppsEvent.QueryDebounced -> setState {
                if (query == event.query) copy(effectiveQuery = event.query) else this
            }
            AppsEvent.SearchToggled -> toggleSearch()
        }
    }

    private fun toggleSearch() {
        searchDebounceJob?.cancel()
        setState {
            if (searchVisible) {
                copy(searchVisible = false, query = "", effectiveQuery = "")
            } else {
                copy(searchVisible = true)
            }
        }
    }

    private fun debounceSearch(query: String) {
        setState { copy(query = query) }
        searchDebounceJob?.cancel()
        searchDebounceJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MILLIS)
            setEvent(AppsEvent.QueryDebounced(query))
        }
    }

    private fun load(refresh: Boolean = false) {
        if (loadJob?.isActive == true) return
        loadJob = viewModelScope.launch {
            setState {
                copy(
                    loading = applications.isEmpty(),
                    refreshing = refresh && applications.isNotEmpty(),
                    error = null,
                )
            }
            getApplicationsSnapshot().fold(
                onSuccess = { snapshot ->
                    setState {
                        copy(
                            loading = false,
                            refreshing = false,
                            applications = snapshot.applications.mapNotNull { it.toUiModel() },
                            moduleStatus = snapshot.moduleStatus,
                        )
                    }
                },
                onFailure = { failure ->
                    setState {
                        copy(
                            loading = false,
                            refreshing = false,
                            error = errorResolver.resolve(failure),
                        )
                    }
                },
            )
        }
    }

    private fun openApplication(androidPackageName: String) {
        val application = viewState.value.applications
            .firstOrNull { it.androidPackageName == androidPackageName }
            ?: return
        if (application.unsupported) {
            setState { copy(unsupportedApplication = application) }
            return
        }
        analytics.track(AnalyticsEvent.appDetailsOpened(application.androidPackageName))
        setEffect {
            AppsEffect.OpenApplication(
                androidPackageName = application.androidPackageName,
                applicationName = application.name,
                flagPackageName = application.mainFlagPackageName,
                availableFlagPackageNames = application.flagPackages.map { it.packageName },
            )
        }
    }

    private companion object {
        const val SEARCH_DEBOUNCE_MILLIS = 300L
    }
}
