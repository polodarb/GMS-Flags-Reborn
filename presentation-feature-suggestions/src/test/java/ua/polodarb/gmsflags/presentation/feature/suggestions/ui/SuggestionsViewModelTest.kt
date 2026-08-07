package ua.polodarb.gmsflags.presentation.feature.suggestions.ui

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Rule
import org.junit.Test
import ua.polodarb.gmsflags.analytics.NoOpPerformanceTracer
import ua.polodarb.gmsflags.domain.apps.GetApplicationXposedScopeStatus
import ua.polodarb.gmsflags.domain.apps.XposedScopeStatus
import ua.polodarb.gmsflags.domain.server.content.GetRecommendationFeed
import ua.polodarb.gmsflags.domain.server.content.GetHomeContent
import ua.polodarb.gmsflags.domain.flags.ObserveFlagOverrideChanges
import kotlinx.coroutines.flow.emptyFlow
import ua.polodarb.gmsflags.domain.server.content.RecommendationStatus
import ua.polodarb.gmsflags.domain.server.content.RecommendationApplicationStatus
import ua.polodarb.gmsflags.domain.server.content.RecommendationSupportStatus
import ua.polodarb.gmsflags.domain.server.content.ServerRecommendationSummary
import ua.polodarb.gmsflags.domain.server.content.ServerRecommendationFeedItem
import ua.polodarb.gmsflags.domain.server.content.LocallyDemotedRecommendationsStore
import ua.polodarb.gmsflags.domain.error.AppError
import ua.polodarb.gmsflags.presentation.feature.suggestions.MainDispatcherRule
import ua.polodarb.gmsflags.presentation.feature.suggestions.mvi.SuggestionsContentState
import ua.polodarb.gmsflags.presentation.feature.suggestions.mvi.SuggestionsApplicationStatusFilter
import ua.polodarb.gmsflags.presentation.feature.suggestions.mvi.SuggestionsEvent
import ua.polodarb.gmsflags.presentation.feature.suggestions.mvi.SuggestionsFilters
import ua.polodarb.gmsflags.presentation.core.error.DefaultErrorResolver
import ua.polodarb.gmsflags.presentation.core.error.UiError

private object NoOpLocallyDemotedRecommendationsStore : LocallyDemotedRecommendationsStore {
    override suspend fun read(): Result<Set<Long>> = Result.success(emptySet())
    override suspend fun demote(recommendationId: Long): Result<Unit> = Result.success(Unit)
    override suspend fun restore(recommendationId: Long): Result<Unit> = Result.success(Unit)
}

@OptIn(ExperimentalCoroutinesApi::class)
class SuggestionsViewModelTest {
    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    @Test
    fun `loads recommendations into content state`() = runTest(dispatcherRule.dispatcher) {
        val viewModel = SuggestionsViewModel(
            getRecommendationFeed = feed(Result.success(listOf(recommendation))),
            getHomeContent = GetHomeContent { Result.success(emptyList()) },
            observeFlagOverrideChanges = ObserveFlagOverrideChanges { emptyFlow() },
            getApplicationScopeStatus = scopeStatus(),
            locallyDemotedRecommendationsStore = NoOpLocallyDemotedRecommendationsStore,
            performanceTracer = NoOpPerformanceTracer,
            errorResolver = DefaultErrorResolver(),
        )

        advanceUntilIdle()

        val content = viewModel.viewState.value.content as SuggestionsContentState.Content
        assertEquals(listOf(1L), content.recommendations.map { it.id })
    }

    @Test
    fun `maps empty response to empty state`() = runTest(dispatcherRule.dispatcher) {
        val viewModel = SuggestionsViewModel(
            getRecommendationFeed = feed(Result.success(emptyList())),
            getHomeContent = GetHomeContent { Result.success(emptyList()) },
            observeFlagOverrideChanges = ObserveFlagOverrideChanges { emptyFlow() },
            getApplicationScopeStatus = scopeStatus(),
            locallyDemotedRecommendationsStore = NoOpLocallyDemotedRecommendationsStore,
            performanceTracer = NoOpPerformanceTracer,
            errorResolver = DefaultErrorResolver(),
        )

        advanceUntilIdle()

        assertSame(SuggestionsContentState.Empty, viewModel.viewState.value.content)
    }

    @Test
    fun `maps failed response to error state`() = runTest(dispatcherRule.dispatcher) {
        val viewModel = SuggestionsViewModel(
            getRecommendationFeed = GetRecommendationFeed {
                Result.failure(IllegalStateException())
            },
            getHomeContent = GetHomeContent { Result.success(emptyList()) },
            observeFlagOverrideChanges = ObserveFlagOverrideChanges { emptyFlow() },
            getApplicationScopeStatus = scopeStatus(),
            locallyDemotedRecommendationsStore = NoOpLocallyDemotedRecommendationsStore,
            performanceTracer = NoOpPerformanceTracer,
            errorResolver = DefaultErrorResolver(),
        )

        advanceUntilIdle()

        val error = viewModel.viewState.value.content as SuggestionsContentState.Error
        assertSame(UiError.Generic, error.error)
    }

    @Test
    fun `preserves semantic network error for presentation`() = runTest(dispatcherRule.dispatcher) {
        val viewModel = SuggestionsViewModel(
            getRecommendationFeed = GetRecommendationFeed {
                Result.failure(AppError.NetworkUnavailable)
            },
            getHomeContent = GetHomeContent { Result.success(emptyList()) },
            observeFlagOverrideChanges = ObserveFlagOverrideChanges { emptyFlow() },
            getApplicationScopeStatus = scopeStatus(),
            locallyDemotedRecommendationsStore = NoOpLocallyDemotedRecommendationsStore,
            performanceTracer = NoOpPerformanceTracer,
            errorResolver = DefaultErrorResolver(),
        )

        advanceUntilIdle()

        val error = viewModel.viewState.value.content as SuggestionsContentState.Error
        assertSame(UiError.NetworkUnavailable, error.error)
    }

    @Test
    fun `keeps selected filters in view state`() = runTest(dispatcherRule.dispatcher) {
        val viewModel = SuggestionsViewModel(
            getRecommendationFeed = feed(Result.success(listOf(recommendation))),
            getHomeContent = GetHomeContent { Result.success(emptyList()) },
            observeFlagOverrideChanges = ObserveFlagOverrideChanges { emptyFlow() },
            getApplicationScopeStatus = scopeStatus(),
            locallyDemotedRecommendationsStore = NoOpLocallyDemotedRecommendationsStore,
            performanceTracer = NoOpPerformanceTracer,
            errorResolver = DefaultErrorResolver(),
        )
        advanceUntilIdle()

        viewModel.setEvent(
            SuggestionsEvent.ApplicationStatusFilterSelected(
                SuggestionsApplicationStatusFilter.NotEnabled
            )
        )

        assertEquals(
            SuggestionsFilters(applicationStatus = SuggestionsApplicationStatusFilter.NotEnabled),
            viewModel.viewState.value.filters,
        )
    }

    private companion object {
        fun scopeStatus(): GetApplicationXposedScopeStatus = GetApplicationXposedScopeStatus {
            Result.success(XposedScopeStatus.Unknown)
        }

        fun feed(result: Result<List<ServerRecommendationSummary>>) = GetRecommendationFeed {
            result.map { summaries ->
                summaries.map {
                    ServerRecommendationFeedItem(
                        summary = it,
                        application = null,
                        previewScreenshotUrl = null,
                        flagNames = emptyList(),
                        applicationStatus = RecommendationApplicationStatus.Unavailable,
                    )
                }
            }
        }

        val recommendation = ServerRecommendationSummary(
            id = 1,
            status = RecommendationStatus.Published,
            supportStatus = RecommendationSupportStatus.Experimental,
            logoUrl = null,
            title = "Feature",
            description = null,
            warning = null,
        )
    }
}
