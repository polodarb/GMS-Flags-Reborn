package ua.polodarb.gmsflags.presentation.feature.apps.ui

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import ua.polodarb.gmsflags.analytics.AnalyticsEvent
import ua.polodarb.gmsflags.analytics.AnalyticsTracker
import ua.polodarb.gmsflags.analytics.AnalyticsUserProperty
import ua.polodarb.gmsflags.domain.apps.GetSupportedApplicationsSnapshot
import ua.polodarb.gmsflags.domain.apps.SupportedApplicationsSnapshot
import ua.polodarb.gmsflags.domain.apps.XposedModuleStatus
import ua.polodarb.gmsflags.domain.servermode.ObserveServerMode
import ua.polodarb.gmsflags.domain.servermode.OfflineNotice
import ua.polodarb.gmsflags.domain.servermode.ServerMode
import ua.polodarb.gmsflags.presentation.core.error.ErrorResolver
import ua.polodarb.gmsflags.presentation.core.error.UiError

private val NoOpAnalytics = object : AnalyticsTracker {
    override fun track(event: AnalyticsEvent) = Unit
    override fun setUserProperty(property: AnalyticsUserProperty, value: String) = Unit
    override fun setCollectionEnabled(enabled: Boolean) = Unit
}

private val NoOpErrorResolver = ErrorResolver { UiError.Generic }

private val EmptySnapshot = GetSupportedApplicationsSnapshot {
    Result.success(SupportedApplicationsSnapshot(emptyList(), XposedModuleStatus.Unknown))
}

@OptIn(ExperimentalCoroutinesApi::class)
class AppsViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() { Dispatchers.setMain(dispatcher) }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    private fun appsViewModel(
        mode: ServerMode,
        getApplicationsSnapshot: GetSupportedApplicationsSnapshot = EmptySnapshot,
    ): AppsViewModel = AppsViewModel(
        getApplicationsSnapshot = getApplicationsSnapshot,
        errorResolver = NoOpErrorResolver,
        analytics = NoOpAnalytics,
        observeServerMode = ObserveServerMode { MutableStateFlow(mode) },
    )

    @Test
    fun `offline mode exposes the badge`() = runTest(dispatcher) {
        val viewModel = appsViewModel(
            mode = ServerMode(offline = true, notice = OfflineNotice(null, null, "Offline")),
        )
        advanceUntilIdle()

        assertTrue(viewModel.viewState.value.offline)
        assertEquals("Offline", viewModel.viewState.value.offlineBadge)
    }

    @Test
    fun `online mode exposes no badge`() = runTest(dispatcher) {
        val viewModel = appsViewModel(mode = ServerMode.Online)
        advanceUntilIdle()

        assertFalse(viewModel.viewState.value.offline)
        assertEquals(null, viewModel.viewState.value.offlineBadge)
    }
}
