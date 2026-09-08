package ua.polodarb.gmsflags.presentation.feature.flagdetails.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import ua.polodarb.gmsflags.analytics.AnalyticsEvent
import ua.polodarb.gmsflags.analytics.AnalyticsTracker
import ua.polodarb.gmsflags.domain.flags.ApplyFlagOverrides
import ua.polodarb.gmsflags.domain.flags.DeleteFlagOverride
import ua.polodarb.gmsflags.domain.flags.DeleteFlagOverrides
import ua.polodarb.gmsflags.domain.flags.DeletePackageOverrides
import ua.polodarb.gmsflags.domain.flags.FlagOverride
import ua.polodarb.gmsflags.domain.flags.FlagType
import ua.polodarb.gmsflags.analytics.PerformanceTracer
import ua.polodarb.gmsflags.domain.flags.GetPhenotypeFlags
import ua.polodarb.gmsflags.domain.flags.FlagOverridesChange
import ua.polodarb.gmsflags.domain.flags.ObserveFlagOverrideChanges
import ua.polodarb.gmsflags.domain.apps.GetApplicationXposedScopeStatus
import ua.polodarb.gmsflags.domain.apps.XposedScopeStatus
import ua.polodarb.gmsflags.domain.hookstatus.GetPairipIncompatiblePackages
import ua.polodarb.gmsflags.domain.server.content.GetApplicationRecommendations
import ua.polodarb.gmsflags.domain.server.content.GetServerApplication
import ua.polodarb.gmsflags.domain.server.content.ServerRecommendationSummary
import ua.polodarb.gmsflags.domain.servermode.ObserveServerMode
import ua.polodarb.gmsflags.presentation.core.viewmodel.BaseViewModel
import ua.polodarb.gmsflags.presentation.core.error.ErrorResolver
import ua.polodarb.gmsflags.presentation.core.error.UiError
import ua.polodarb.gmsflags.presentation.feature.flagdetails.editor.FlagEditorValidator
import ua.polodarb.gmsflags.presentation.feature.flagdetails.editor.FlagEditorValidationException
import ua.polodarb.gmsflags.presentation.feature.flagdetails.mutation.FlagMutationCoordinator
import ua.polodarb.gmsflags.presentation.feature.flagdetails.mutation.FlagMutationResult
import ua.polodarb.gmsflags.presentation.feature.flagdetails.mutation.FlagOverrideWriteQueue
import ua.polodarb.gmsflags.presentation.feature.flagdetails.mvi.FlagDetailsDialog
import ua.polodarb.gmsflags.presentation.feature.flagdetails.mvi.FlagDetailsEffect
import ua.polodarb.gmsflags.presentation.feature.flagdetails.mvi.FlagDetailsEvent
import ua.polodarb.gmsflags.presentation.feature.flagdetails.mvi.FlagFilter
import ua.polodarb.gmsflags.presentation.feature.flagdetails.mvi.FlagDetailsReducer
import ua.polodarb.gmsflags.presentation.feature.flagdetails.mvi.FlagDetailsState
import ua.polodarb.gmsflags.presentation.feature.flagdetails.mvi.AppRemoteContentState
import ua.polodarb.gmsflags.presentation.feature.flagdetails.mvi.selectedFlagValues
import ua.polodarb.gmsflags.presentation.feature.flagdetails.mvi.withBooleanOverride
import ua.polodarb.gmsflags.presentation.feature.flagdetails.mvi.withOverride
import ua.polodarb.gmsflags.presentation.feature.flagdetails.mvi.withOverrides
import ua.polodarb.gmsflags.presentation.feature.flagdetails.mvi.withSelectedBooleanValue
import ua.polodarb.gmsflags.presentation.feature.flagdetails.mvi.withoutAllOverrides
import ua.polodarb.gmsflags.presentation.feature.flagdetails.mvi.withoutSelectedOverrides
import ua.polodarb.gmsflags.presentation.core.message.UiMessageType
import ua.polodarb.gmsflags.presentation.feature.flagdetails.mvi.withoutOverride
import ua.polodarb.gmsflags.presentation.feature.flagdetails.mvi.withoutOverrides
import ua.polodarb.gmsflags.presentation.feature.flagdetails.share.FlagShareContentFactory
import ua.polodarb.gmsflags.presentation.feature.flagdetails.R
import androidx.annotation.StringRes

class FlagDetailsViewModel(
    private val androidPackageName: String,
    private val applicationName: String,
    private val initialPhenotypePackageName: String,
    availablePhenotypePackageNames: List<String>,
    private val getFlags: GetPhenotypeFlags,
    private val applyOverrides: ApplyFlagOverrides,
    private val deleteOverride: DeleteFlagOverride,
    private val deleteOverrides: DeleteFlagOverrides,
    private val deletePackageOverrides: DeletePackageOverrides,
    private val observeFlagOverrideChanges: ObserveFlagOverrideChanges,
    private val getApplicationScopeStatus: GetApplicationXposedScopeStatus,
    private val getPairipIncompatiblePackages: GetPairipIncompatiblePackages,
    private val getServerApplication: GetServerApplication,
    private val getApplicationRecommendations: GetApplicationRecommendations,
    private val errorResolver: ErrorResolver,
    private val analytics: AnalyticsTracker,
    private val performanceTracer: PerformanceTracer,
    private val observeServerMode: ObserveServerMode,
    private val backgroundDispatcher: CoroutineDispatcher = Dispatchers.Default,
) : BaseViewModel<FlagDetailsEvent, FlagDetailsState, FlagDetailsEffect>() {
    private val mutations = FlagMutationCoordinator()
    private var overrideWrites = createOverrideWriteQueue(initialPhenotypePackageName)
    private var loadJob: Job? = null
    private var searchDebounceJob: Job? = null
    private var scopeStatusJob: Job? = null
    private var remoteContentJob: Job? = null
    private val flagPackageNames = buildList {
        add(initialPhenotypePackageName)
        addAll(availablePhenotypePackageNames)
    }.filter(String::isNotBlank).distinct()

    override fun initialState() = FlagDetailsState(
        androidPackageName = androidPackageName,
        applicationName = applicationName,
        phenotypePackageName = initialPhenotypePackageName,
        primaryPhenotypePackageName = initialPhenotypePackageName,
        availablePhenotypePackageNames = flagPackageNames,
    )

    init {
        load()
        observeOverrideChanges()
        refreshScopeStatus()
        checkPairipCompatibility()
        loadRemoteContent()
    }

    private fun checkPairipCompatibility() {
        viewModelScope.launch {
            getPairipIncompatiblePackages(listOf(androidPackageName)).onSuccess { packages ->
                setState { copy(pairipIncompatible = androidPackageName in packages) }
            }
        }
    }

    private fun observeOverrideChanges() {
        viewModelScope.launch {
            observeFlagOverrideChanges().collect { change ->
                setState {
                    if (
                        change.androidPackageName != androidPackageName ||
                        change.phenotypePackageName != phenotypePackageName
                    ) {
                        return@setState this
                    }
                    when (change) {
                        is FlagOverridesChange.Applied -> withOverrides(change.overrides)
                        is FlagOverridesChange.Removed -> withoutOverrides(change.flagNames)
                        is FlagOverridesChange.PackageCleared -> withoutAllOverrides()
                    }
                }
            }
        }
    }

    override fun handleEvent(event: FlagDetailsEvent) {
        when (event) {
            FlagDetailsEvent.Retry -> load()
            FlagDetailsEvent.ScopeRefresh -> refreshScopeStatus()
            FlagDetailsEvent.ScopeHelpClicked -> setState { copy(scopeHelpVisible = true) }
            FlagDetailsEvent.ScopeHelpDismissed -> setState { copy(scopeHelpVisible = false) }
            FlagDetailsEvent.PairipHelpClicked -> setState { copy(pairipHelpVisible = true) }
            FlagDetailsEvent.PairipHelpDismissed -> setState { copy(pairipHelpVisible = false) }
            FlagDetailsEvent.BooleanControlHelpClicked -> setState {
                copy(booleanControlHelpVisible = true)
            }
            FlagDetailsEvent.BooleanControlHelpDismissed -> setState {
                copy(booleanControlHelpVisible = false)
            }
            FlagDetailsEvent.RemoteContentRetry -> loadRemoteContent()
            is FlagDetailsEvent.RecommendationClicked -> setEffect {
                FlagDetailsEffect.OpenRecommendation(event.id)
            }
            is FlagDetailsEvent.PackageSelected -> switchPackage(event.packageName)
            is FlagDetailsEvent.QueryChanged -> debounceSearch(event.query)
            FlagDetailsEvent.SearchToggled,
            FlagDetailsEvent.FiltersToggled -> {
                searchDebounceJob?.cancel()
                reduce(event)
            }
            is FlagDetailsEvent.BooleanChanged -> setBoolean(event.name, event.enabled)
            is FlagDetailsEvent.BooleanOverrideCleared -> clearBooleanOverride(event.name)
            is FlagDetailsEvent.SetSelectedBooleans -> setSelectedBooleans(event.enabled)
            FlagDetailsEvent.ResetSelectedToDefault -> resetSelectedToDefault()
            FlagDetailsEvent.DeleteAllOverridesConfirmed -> deleteAllOverrides()
            FlagDetailsEvent.InlineEditorSaved -> saveInlineEditor()
            FlagDetailsEvent.InlineEditorReset -> resetInlineEditor()
            FlagDetailsEvent.EditorSaved -> saveEditor()
            FlagDetailsEvent.EditorReset -> resetEditor()
            FlagDetailsEvent.ExportConfirmed -> exportFlags()
            FlagDetailsEvent.ReportConfirmed -> reportFlags()
            else -> reduce(event)
        }
    }

    private fun loadRemoteContent() {
        if (observeServerMode().value.offline) {
            remoteContentJob?.cancel()
            remoteContentJob = null
            setState { copy(remoteContent = AppRemoteContentState.Unavailable) }
            return
        }
        if (remoteContentJob?.isActive == true) return
        remoteContentJob = viewModelScope.launch {
            setState { copy(remoteContent = AppRemoteContentState.Loading) }
            val (applicationResult, recommendationsResult) = coroutineScope {
                val application = async { getServerApplication(androidPackageName) }
                val recommendations = async {
                    Result.success(emptyList<ServerRecommendationSummary>())
                }
                application.await() to recommendations.await()
            }
            val application = applicationResult.getOrNull()
            val recommendations = recommendationsResult.getOrDefault(emptyList())
            when {
                application != null || recommendationsResult.isSuccess -> setState {
                    copy(
                        remoteContent = AppRemoteContentState.Ready(
                            infoBlocks = application?.infoBlocks
                                ?.filter { it.isActive }
                                ?.sortedBy { it.sortOrder }
                                .orEmpty(),
                            recommendations = recommendations,
                            flagAnnotations = application?.highlightedFlags
                                ?.associateBy { it.flagName }
                                .orEmpty(),
                        )
                    )
                }
                errorResolver.resolve(applicationResult.exceptionOrNull()!!)
                    == UiError.NotFound -> setState {
                    copy(remoteContent = AppRemoteContentState.Unavailable)
                }
                else -> setState {
                    copy(
                        remoteContent = AppRemoteContentState.Error(
                            errorResolver.resolve(applicationResult.exceptionOrNull()!!)
                        )
                    )
                }
            }
        }
    }

    private fun refreshScopeStatus() {
        if (scopeStatusJob?.isActive == true) return
        scopeStatusJob = viewModelScope.launch {
            getApplicationScopeStatus(androidPackageName).fold(
                onSuccess = { status ->
                    setState {
                        copy(
                            xposedScopeStatus = status,
                            scopeHelpVisible = scopeHelpVisible &&
                                status == XposedScopeStatus.Excluded,
                        )
                    }
                },
                onFailure = {
                    setState { copy(xposedScopeStatus = XposedScopeStatus.Unknown) }
                },
            )
        }
    }

    private fun reduce(event: FlagDetailsEvent) {
        val reduction = FlagDetailsReducer.reduce(viewState.value, event) ?: return
        setState { reduction.state }
        reduction.effect?.let { effect -> setEffect { effect } }
    }

    private fun debounceSearch(query: String) {
        analytics.track(AnalyticsEvent.flagSearch(query.length))
        reduce(FlagDetailsEvent.QueryChanged(query))
        searchDebounceJob?.cancel()
        searchDebounceJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MILLIS)
            reduce(FlagDetailsEvent.QueryDebounced(query))
        }
    }

    private fun load(packageName: String = viewState.value.phenotypePackageName) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            setState {
                copy(
                    loading = true,
                    error = null,
                    inlineEditor = null,
                )
            }
            val flagTrace = performanceTracer.start("flag_load")
            getFlags(androidPackageName, packageName).also { flagTrace.stop() }.fold(
                onSuccess = { loadedFlags ->
                    setState {
                        if (phenotypePackageName != packageName) return@setState this
                        copy(
                            loading = false,
                            flags = loadedFlags,
                            selectedFlags = selectedFlags.filterTo(mutableSetOf()) { selected ->
                                loadedFlags.any {
                                    it.type == selected.type && it.name == selected.name
                                }
                            },
                        )
                    }
                },
                onFailure = { failure ->
                    setState {
                        if (phenotypePackageName != packageName) return@setState this
                        copy(
                            loading = false,
                            error = errorResolver.resolve(failure),
                        )
                    }
                },
            )
        }
    }

    private fun setBoolean(name: String, enabled: Boolean) {
        val (state, override) = viewState.value.withBooleanOverride(name, enabled)
        setState { state }
        apply(listOf(override))
    }

    private fun setSelectedBooleans(enabled: Boolean) {
        val mutation = viewState.value.withSelectedBooleanValue(enabled)
        if (mutation.overrides.isEmpty()) return
        setState { mutation.state }
        apply(mutation.overrides)
        enqueueMutation(operation = { Result.success(Unit) }, isBulk = true)
        showMessage(
            messageRes = R.string.message_bulk_override_warning,
            type = UiMessageType.Warning,
            longDuration = true,
        )
    }

    private fun resetSelectedToDefault() {
        val mutation = viewState.value.withoutSelectedOverrides()
        if (mutation.names.isEmpty()) return
        val packageName = viewState.value.phenotypePackageName
        setState { mutation.state }
        mutation.names.forEach { name ->
            analytics.track(AnalyticsEvent.flagReset(androidPackageName, name))
        }
        enqueueMutation(
            operation = { deleteOverrides(androidPackageName, packageName, mutation.names.toList()) },
            successMessageRes = R.string.message_overrides_deleted,
            isBulk = true,
        )
    }

    private fun clearBooleanOverride(name: String) {
        val flag = viewState.value.flags.firstOrNull { it.name == name } ?: return
        if (!flag.overridden) return
        val packageName = viewState.value.phenotypePackageName
        setState { withoutOverride(name) }
        analytics.track(AnalyticsEvent.flagReset(androidPackageName, name))
        viewModelScope.launch {
            deleteOverride(androidPackageName, packageName, name).onFailure(::backgroundMutationFailed)
        }
    }

    private fun saveEditor() {
        val editor = viewState.value.dialog as? FlagDetailsDialog.Editor ?: return
        FlagEditorValidator.validate(editor).fold(
            onSuccess = { override ->
                setState { copy(dialog = null).withOverride(override) }
                apply(listOf(override))
            },
            onFailure = { error ->
                showMessage(
                    (error as? FlagEditorValidationException)?.messageRes
                        ?: R.string.message_invalid_flag_value
                )
            },
        )
    }

    private fun saveInlineEditor() {
        val editor = viewState.value.inlineEditor ?: return
        FlagEditorValidator.validate(
            name = editor.name,
            type = editor.type,
            value = editor.value,
        ).fold(
            onSuccess = { override ->
                setState { copy(inlineEditor = null).withOverride(override) }
                apply(listOf(override))
            },
            onFailure = { error ->
                showMessage(
                    (error as? FlagEditorValidationException)?.messageRes
                        ?: R.string.message_invalid_flag_value,
                )
            },
        )
    }

    private fun resetInlineEditor() {
        val editor = viewState.value.inlineEditor ?: return
        val packageName = viewState.value.phenotypePackageName
        setState { copy(inlineEditor = null).withoutOverride(editor.name) }
        analytics.track(AnalyticsEvent.flagReset(androidPackageName, editor.name))
        enqueueMutation(
            operation = {
                deleteOverride(androidPackageName, packageName, editor.name)
            },
            successMessageRes = R.string.message_flag_reset,
        )
    }

    private fun resetEditor() {
        val editor = viewState.value.dialog as? FlagDetailsDialog.Editor ?: return
        val flagName = editor.originalName ?: return
        val packageName = viewState.value.phenotypePackageName
        setState { copy(dialog = null).withoutOverride(flagName) }
        analytics.track(AnalyticsEvent.flagReset(androidPackageName, flagName))
        enqueueMutation(
            operation = { deleteOverride(androidPackageName, packageName, flagName) },
            successMessageRes = R.string.message_flag_reset,
        )
    }

    private fun deleteAllOverrides() {
        val packageName = viewState.value.phenotypePackageName
        setState { withoutAllOverrides() }
        enqueueMutation(
            operation = { deletePackageOverrides(androidPackageName, packageName) },
            successMessageRes = R.string.message_overrides_deleted,
        )
    }

    private fun apply(overrides: List<FlagOverride>) {
        overrideWrites.submit(overrides)
    }

    private fun trackFlagApplied(override: FlagOverride) {
        if (override.type == FlagType.String) return
        analytics.track(
            AnalyticsEvent.flagApplied(
                androidPackageName,
                override.name,
                override.type.name,
                override.value,
            )
        )
    }

    private fun exportFlags() {
        val state = viewState.value
        val dialog = state.dialog as? FlagDetailsDialog.Export ?: return
        val selected = state.selectedFlagValues()
        if (selected.isEmpty()) return
        setState { copy(dialog = null) }
        viewModelScope.launch(backgroundDispatcher) {
            val content = FlagShareContentFactory.export(state.phenotypePackageName, selected)
            setEffect {
                FlagDetailsEffect.ShareFlags(
                    fileName = dialog.fileName.ifBlank { state.phenotypePackageName },
                    content = content,
                )
            }
        }
    }

    private fun reportFlags() {
        val state = viewState.value
        val dialog = state.dialog as? FlagDetailsDialog.Report ?: return
        val selected = state.selectedFlagValues()
        setState { copy(dialog = null) }
        viewModelScope.launch(backgroundDispatcher) {
            val content = FlagShareContentFactory.report(selected)
            setEffect {
                FlagDetailsEffect.ReportFlags(
                    packageName = state.phenotypePackageName,
                    description = dialog.description,
                    flags = content,
                )
            }
        }
    }

    private fun enqueueMutation(
        operation: suspend () -> Result<Unit>,
        @StringRes successMessageRes: Int? = null,
        isBulk: Boolean = false,
    ) {
        val generation = mutations.begin(resetFailure = !viewState.value.operationInProgress)
        setState { copy(operationInProgress = true, bulkOperationInProgress = isBulk, error = null) }
        viewModelScope.launch {
            val serializedOperation: suspend () -> Result<Unit> = {
                overrideWrites.awaitIdle()
                operation()
            }
            when (val result = mutations.execute(generation, serializedOperation)) {
                FlagMutationResult.AwaitingLatest -> Unit
                FlagMutationResult.Success -> {
                    setState { copy(operationInProgress = false, bulkOperationInProgress = false) }
                    successMessageRes?.let { messageRes ->
                        showMessage(messageRes, UiMessageType.Success)
                    }
                }
                is FlagMutationResult.Failure -> mutationFailed(result.error)
            }
        }
    }

    private fun mutationFailed(error: Throwable) {
        setState { copy(operationInProgress = false, bulkOperationInProgress = false) }
        showError(error)
        load()
    }

    private fun backgroundMutationFailed(error: Throwable) {
        showError(error)
        load()
    }

    private fun switchPackage(packageName: String) {
        val currentState = viewState.value
        if (
            packageName == currentState.phenotypePackageName ||
            packageName !in currentState.availablePhenotypePackageNames ||
            currentState.operationInProgress
        ) {
            return
        }

        analytics.track(AnalyticsEvent.packageSwitched(packageName))
        loadJob?.cancel()
        searchDebounceJob?.cancel()
        val previousQueue = overrideWrites
        viewModelScope.launch {
            setState {
                copy(
                    operationInProgress = true,
                    bulkOperationInProgress = false,
                    inlineEditor = null,
                    selectedFlags = emptySet(),
                    dialog = null,
                )
            }
            previousQueue.awaitIdle()
            overrideWrites = createOverrideWriteQueue(packageName)
            setState {
                copy(
                    phenotypePackageName = packageName,
                    loading = true,
                    operationInProgress = false,
                    bulkOperationInProgress = false,
                    flags = emptyList(),
                    query = "",
                    effectiveQuery = "",
                    filter = FlagFilter.All,
                    error = null,
                )
            }
            load(packageName)
        }
    }

    private fun createOverrideWriteQueue(packageName: String) = FlagOverrideWriteQueue(
        scope = viewModelScope,
        writer = { overrides -> applyOverrides(androidPackageName, packageName, overrides) },
        onFailure = ::backgroundMutationFailed,
        onApplied = { overrides -> overrides.forEach(::trackFlagApplied) },
    )

    private fun showMessage(
        @StringRes messageRes: Int,
        type: UiMessageType = UiMessageType.Error,
        longDuration: Boolean = false,
    ) {
        setEffect { FlagDetailsEffect.ShowMessage(messageRes, type, longDuration) }
    }

    private fun showError(error: Throwable) {
        setEffect { FlagDetailsEffect.ShowError(errorResolver.resolve(error)) }
    }

    private companion object {
        const val SEARCH_DEBOUNCE_MILLIS = 300L
    }
}
