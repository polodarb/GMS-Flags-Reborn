package ua.polodarb.gmsflags.presentation.feature.suggestions.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import ua.polodarb.gmsflags.analytics.PerformanceTracer
import ua.polodarb.gmsflags.domain.apps.GetApplicationXposedScopeStatus
import ua.polodarb.gmsflags.domain.apps.XposedScopeStatus
import ua.polodarb.gmsflags.domain.flags.ObserveFlagOverrideChanges
import ua.polodarb.gmsflags.domain.server.content.GetHomeContent
import ua.polodarb.gmsflags.domain.server.content.GetRecommendationFeed
import ua.polodarb.gmsflags.domain.server.content.LocallyDemotedRecommendationsStore
import ua.polodarb.gmsflags.domain.server.content.ServerRecommendationFeedItem
import ua.polodarb.gmsflags.presentation.core.error.ErrorResolver
import ua.polodarb.gmsflags.presentation.core.viewmodel.BaseViewModel
import ua.polodarb.gmsflags.presentation.feature.suggestions.mvi.SuggestionsEffect
import ua.polodarb.gmsflags.presentation.feature.suggestions.mvi.SuggestionsEvent
import ua.polodarb.gmsflags.presentation.feature.suggestions.mvi.SuggestionsContentState
import ua.polodarb.gmsflags.presentation.feature.suggestions.mvi.SuggestionsFilters
import ua.polodarb.gmsflags.presentation.feature.suggestions.mvi.SuggestionsState
import ua.polodarb.gmsflags.presentation.feature.suggestions.ui.model.toUiModel

class SuggestionsViewModel(
    private val getRecommendationFeed: GetRecommendationFeed,
    private val getHomeContent: GetHomeContent,
    private val observeFlagOverrideChanges: ObserveFlagOverrideChanges,
    private val getApplicationScopeStatus: GetApplicationXposedScopeStatus,
    private val locallyDemotedRecommendationsStore: LocallyDemotedRecommendationsStore,
    private val performanceTracer: PerformanceTracer,
    private val errorResolver: ErrorResolver,
) : BaseViewModel<SuggestionsEvent, SuggestionsState, SuggestionsEffect>() {
    private var loadJob: Job? = null
    private var searchDebounceJob: Job? = null

    override fun initialState() = SuggestionsState()

    init {
        load()
        observeOverrideChanges()
        loadDemotedRecommendationIds()
    }

    private fun loadDemotedRecommendationIds() {
        viewModelScope.launch {
            val ids = locallyDemotedRecommendationsStore.read().getOrDefault(emptySet())
            setState { copy(demotedRecommendationIds = ids) }
        }
    }

    private fun observeOverrideChanges() {
        viewModelScope.launch {
            observeFlagOverrideChanges().collectLatest {
                if (viewState.value.content is SuggestionsContentState.Content) {
                    load(refresh = true)
                }
            }
        }
    }

    override fun handleEvent(event: SuggestionsEvent) {
        when (event) {
            SuggestionsEvent.Retry -> load()
            SuggestionsEvent.Refresh -> load(refresh = true)
            SuggestionsEvent.SearchToggled -> toggleSearch()
            SuggestionsEvent.FiltersReset -> setState { copy(filters = SuggestionsFilters()) }
            is SuggestionsEvent.QueryChanged -> debounceSearch(event.query)
            is SuggestionsEvent.QueryDebounced -> setState {
                if (query == event.query) copy(effectiveQuery = event.query) else this
            }
            is SuggestionsEvent.FiltersApplied -> setState { copy(filters = event.filters) }
            is SuggestionsEvent.ApplicationStatusFilterSelected -> setState {
                copy(filters = filters.copy(applicationStatus = event.filter))
            }
            is SuggestionsEvent.RecommendationSelected -> setEffect {
                SuggestionsEffect.OpenRecommendation(event.id)
            }
            is SuggestionsEvent.RecommendationDismissedFromTop -> demoteFromTop(event.id)
            is SuggestionsEvent.RecommendationTopUndoClicked -> restoreToTop(event.id)
        }
    }

    private fun demoteFromTop(recommendationId: Long) {
        val title = (viewState.value.content as? SuggestionsContentState.Content)
            ?.recommendations
            ?.firstOrNull { it.id == recommendationId }
            ?.title
            ?: return
        setState { copy(demotedRecommendationIds = demotedRecommendationIds + recommendationId) }
        viewModelScope.launch { locallyDemotedRecommendationsStore.demote(recommendationId) }
        setEffect { SuggestionsEffect.ShowTopDismissUndo(recommendationId, title) }
    }

    private fun restoreToTop(recommendationId: Long) {
        setState { copy(demotedRecommendationIds = demotedRecommendationIds - recommendationId) }
        viewModelScope.launch { locallyDemotedRecommendationsStore.restore(recommendationId) }
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
            setEvent(SuggestionsEvent.QueryDebounced(query))
        }
    }

    private fun load(refresh: Boolean = false) {
        if (loadJob?.isActive == true) return

        loadJob = viewModelScope.launch {
            if (refresh && viewState.value.content is SuggestionsContentState.Content) {
                setState { copy(refreshing = true) }
            } else {
                setState { copy(content = SuggestionsContentState.Loading) }
            }
            val feedTrace = performanceTracer.start("recommendation_feed_load")
            runCatching {
                coroutineScope {
                    val home = async { getHomeContent().getOrThrow() }
                    val recommendations = async { getRecommendationFeed().getOrThrow() }
                    home.await() to recommendations.await()
                }
            }.also { feedTrace.stop() }.fold(
                onSuccess = { (home, recommendations) ->
                    val infoBlocks = home
                        .filter { it.isActive }
                        .sortedBy { it.sortOrder }
                        .map { it.toUiModel() }
                    val scopeExcludedPackages = excludedScopePackages(recommendations)
                    val items = recommendations.map {
                        it.toUiModel(
                            scopeExcluded = it.application?.packageName in scopeExcludedPackages,
                        )
                    }
                    setState {
                        copy(
                            content = if (infoBlocks.isEmpty() && items.isEmpty()) {
                                SuggestionsContentState.Empty
                            } else {
                                SuggestionsContentState.Content(
                                    infoBlocks = infoBlocks,
                                    recommendations = items,
                                )
                            },
                            refreshing = false,
                        )
                    }
                },
                onFailure = { error ->
                    val resolvedError = errorResolver.resolve(error)
                    if (viewState.value.content is SuggestionsContentState.Content) {
                        setEffect { SuggestionsEffect.ShowError(resolvedError) }
                    }
                    setState {
                        if (content is SuggestionsContentState.Content) {
                            copy(refreshing = false)
                        } else {
                            copy(
                                content = SuggestionsContentState.Error(resolvedError),
                                refreshing = false,
                            )
                        }
                    }
                },
            )
        }
    }

    private suspend fun excludedScopePackages(
        recommendations: List<ServerRecommendationFeedItem>,
    ): Set<String> = coroutineScope {
        recommendations.mapNotNull { it.application?.packageName }
            .distinct()
            .map { packageName ->
                async {
                    val status = getApplicationScopeStatus(packageName)
                        .getOrDefault(XposedScopeStatus.Unknown)
                    packageName.takeIf { status == XposedScopeStatus.Excluded }
                }
            }
            .awaitAll()
            .filterNotNull()
            .toSet()
    }

    private companion object {
        const val SEARCH_DEBOUNCE_MILLIS = 300L
    }
}
