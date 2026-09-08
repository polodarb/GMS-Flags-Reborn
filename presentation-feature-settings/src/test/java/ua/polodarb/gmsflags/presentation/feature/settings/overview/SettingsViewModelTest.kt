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
        serverMode: MutableStateFlow<ServerMode>,
        getHomeContent: GetHomeContent = GetHomeContent { Result.success(emptyList()) },
    ): SettingsViewModel = SettingsViewModel(
        observeOverrideControl = ObserveOverrideControl { MutableStateFlow(OverrideControlState()) },
        refreshOverrideControl = RefreshOverrideControl { Result.success(Unit) },
        getHookStatus = GetHookStatus { Result.success(HookStatusOverview(applications = emptyList())) },
        getHomeContent = getHomeContent,
        analytics = NoOpAnalytics,
        errorResolver = NoOpErrorResolver,
        observeServerMode = ObserveServerMode { serverMode },
    )

    @Test
    fun `offline mode reaches the state and skips the server probe`() = runTest(dispatcher) {
        var homeCalls = 0
        val viewModel = settingsViewModel(
            serverMode = MutableStateFlow(offlineMode),
            getHomeContent = GetHomeContent {
                homeCalls++
                Result.success(emptyList())
            },
        )
        advanceUntilIdle()

        assertTrue(viewModel.viewState.value.offline)
        assertEquals("Offline", viewModel.viewState.value.offlineNotice?.badge)
        assertEquals(0, homeCalls)
        assertEquals(ServerConnectionState.Checking, viewModel.viewState.value.serverConnection)
    }

    @Test
    fun `online mode still probes the server`() = runTest(dispatcher) {
        var homeCalls = 0
        val viewModel = settingsViewModel(
            serverMode = MutableStateFlow(ServerMode.Online),
            getHomeContent = GetHomeContent {
                homeCalls++
                Result.success(emptyList())
            },
        )
        advanceUntilIdle()

        assertFalse(viewModel.viewState.value.offline)
        assertEquals(1, homeCalls)
        assertEquals(ServerConnectionState.Available, viewModel.viewState.value.serverConnection)
    }

    @Test
    fun `an offline to online flip probes the server`() = runTest(dispatcher) {
        var homeCalls = 0
        val mode = MutableStateFlow(offlineMode)
        val viewModel = settingsViewModel(
            serverMode = mode,
            getHomeContent = GetHomeContent {
                homeCalls++
                Result.success(emptyList())
            },
        )
        advanceUntilIdle()
        assertEquals(0, homeCalls)

        mode.value = ServerMode.Online
        advanceUntilIdle()

        assertEquals(1, homeCalls)
        assertEquals(ServerConnectionState.Available, viewModel.viewState.value.serverConnection)
    }

    @Test
    fun `an online to offline flip resets the probe result`() = runTest(dispatcher) {
        val mode = MutableStateFlow(ServerMode.Online)
        val viewModel = settingsViewModel(serverMode = mode)
        advanceUntilIdle()
        assertEquals(ServerConnectionState.Available, viewModel.viewState.value.serverConnection)

        mode.value = offlineMode
        advanceUntilIdle()

        assertEquals(ServerConnectionState.Checking, viewModel.viewState.value.serverConnection)
    }

    private val offlineMode =
        ServerMode(offline = true, notice = OfflineNotice(null, null, "Offline"))
}
