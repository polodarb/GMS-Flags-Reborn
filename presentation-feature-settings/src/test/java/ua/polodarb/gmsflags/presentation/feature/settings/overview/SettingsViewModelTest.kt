package ua.polodarb.gmsflags.presentation.feature.settings.overview

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
import ua.polodarb.gmsflags.domain.hookstatus.GetHookStatus
import ua.polodarb.gmsflags.domain.hookstatus.HookStatusOverview
import ua.polodarb.gmsflags.domain.server.content.GetHomeContent
import ua.polodarb.gmsflags.domain.servermode.ObserveServerMode
import ua.polodarb.gmsflags.domain.servermode.OfflineNotice
import ua.polodarb.gmsflags.domain.servermode.ServerMode
import ua.polodarb.gmsflags.domain.settings.ObserveOverrideControl
import ua.polodarb.gmsflags.domain.settings.OverrideControlState
import ua.polodarb.gmsflags.domain.settings.RefreshOverrideControl
import ua.polodarb.gmsflags.presentation.core.error.ErrorResolver
import ua.polodarb.gmsflags.presentation.core.error.UiError

private val NoOpAnalytics = object : AnalyticsTracker {
    override fun track(event: AnalyticsEvent) = Unit
    override fun setUserProperty(property: AnalyticsUserProperty, value: String) = Unit
    override fun setCollectionEnabled(enabled: Boolean) = Unit
}

private val NoOpErrorResolver = ErrorResolver { UiError.Generic }

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() { Dispatchers.setMain(dispatcher) }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    private fun settingsViewModel(
        offline: Boolean,
        getHomeContent: GetHomeContent = GetHomeContent { Result.success(emptyList()) },
    ): SettingsViewModel = SettingsViewModel(
        observeOverrideControl = ObserveOverrideControl { MutableStateFlow(OverrideControlState()) },
        refreshOverrideControl = RefreshOverrideControl { Result.success(Unit) },
        getHookStatus = GetHookStatus { Result.success(HookStatusOverview(applications = emptyList())) },
        getHomeContent = getHomeContent,
        analytics = NoOpAnalytics,
        errorResolver = NoOpErrorResolver,
        observeServerMode = ObserveServerMode {
            MutableStateFlow(
                if (offline) {
                    ServerMode(offline = true, notice = OfflineNotice(null, null, "Offline"))
                } else {
                    ServerMode.Online
                },
            )
        },
    )

    @Test
    fun `offline mode reaches the state and skips the server probe`() = runTest(dispatcher) {
        var homeCalls = 0
        val viewModel = settingsViewModel(
            offline = true,
            getHomeContent = GetHomeContent {
                homeCalls++
                Result.success(emptyList())
            },
        )
        advanceUntilIdle()

        assertTrue(viewModel.viewState.value.offline)
        assertEquals("Offline", viewModel.viewState.value.offlineNotice?.badge)
        assertEquals(0, homeCalls)
    }

    @Test
    fun `online mode still probes the server`() = runTest(dispatcher) {
        var homeCalls = 0
        val viewModel = settingsViewModel(
            offline = false,
            getHomeContent = GetHomeContent {
                homeCalls++
                Result.success(emptyList())
            },
        )
        advanceUntilIdle()

        assertFalse(viewModel.viewState.value.offline)
        assertEquals(1, homeCalls)
    }
}
