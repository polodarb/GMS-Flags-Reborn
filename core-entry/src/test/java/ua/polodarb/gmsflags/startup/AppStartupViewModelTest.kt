package ua.polodarb.gmsflags.startup

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import ua.polodarb.gmsflags.analytics.RecordingCrashReporter
import ua.polodarb.gmsflags.domain.navigation.RefreshNavigationFlags
import ua.polodarb.gmsflags.domain.onboarding.ObserveOnboardingCompletion
import ua.polodarb.gmsflags.domain.onboarding.RequestRootAccess
import ua.polodarb.gmsflags.domain.servermode.RefreshServerMode

@OptIn(ExperimentalCoroutinesApi::class)
class AppStartupViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `first launch opens onboarding without requesting root`() = runTest(dispatcher) {
        val completed = MutableStateFlow(false)
        var rootRequests = 0
        val viewModel = AppStartupViewModel(
            observeOnboardingCompletion = ObserveOnboardingCompletion { completed },
            requestRootAccess = RequestRootAccess {
                rootRequests++
                Result.success(Unit)
            },
            refreshServerMode = RefreshServerMode {},
            hasCachedServerMode = { true },
            refreshNavigationFlags = RefreshNavigationFlags {},
            crashReporter = RecordingCrashReporter(),
        )
        advanceUntilIdle()

        assertEquals(AppStartupState.Onboarding, viewModel.state.value)
        assertEquals(0, rootRequests)
    }

    @Test
    fun `completed onboarding verifies root before opening the app`() = runTest(dispatcher) {
        val viewModel = AppStartupViewModel(
            observeOnboardingCompletion = ObserveOnboardingCompletion { MutableStateFlow(true) },
            requestRootAccess = RequestRootAccess { Result.success(Unit) },
            refreshServerMode = RefreshServerMode {},
            hasCachedServerMode = { true },
            refreshNavigationFlags = RefreshNavigationFlags {},
            crashReporter = RecordingCrashReporter(),
        )
        advanceUntilIdle()

        assertEquals(AppStartupState.Ready, viewModel.state.value)
    }

    @Test
    fun `first launch waits for the server mode refresh before opening the app`() =
        runTest(dispatcher) {
            var refreshes = 0
            val viewModel = AppStartupViewModel(
                observeOnboardingCompletion = ObserveOnboardingCompletion { MutableStateFlow(true) },
                requestRootAccess = RequestRootAccess { Result.success(Unit) },
                refreshServerMode = RefreshServerMode { refreshes++ },
                hasCachedServerMode = { false },
                refreshNavigationFlags = RefreshNavigationFlags {},
                crashReporter = RecordingCrashReporter(),
            )
            advanceUntilIdle()

            assertEquals(1, refreshes)
            assertEquals(AppStartupState.Ready, viewModel.state.value)
        }

    @Test
    fun `a hanging refresh cannot block startup`() = runTest(dispatcher) {
        val viewModel = AppStartupViewModel(
            observeOnboardingCompletion = ObserveOnboardingCompletion { MutableStateFlow(true) },
            requestRootAccess = RequestRootAccess { Result.success(Unit) },
            refreshServerMode = RefreshServerMode { awaitCancellation() },
            hasCachedServerMode = { false },
            refreshNavigationFlags = RefreshNavigationFlags {},
            crashReporter = RecordingCrashReporter(),
        )
        advanceUntilIdle()

        assertEquals(AppStartupState.Ready, viewModel.state.value)
    }

    @Test
    fun `a failing refresh does not stop startup`() = runTest(dispatcher) {
        val viewModel = AppStartupViewModel(
            observeOnboardingCompletion = ObserveOnboardingCompletion { MutableStateFlow(true) },
            requestRootAccess = RequestRootAccess { Result.success(Unit) },
            refreshServerMode = RefreshServerMode { throw IllegalStateException("boom") },
            hasCachedServerMode = { false },
            refreshNavigationFlags = RefreshNavigationFlags {},
            crashReporter = RecordingCrashReporter(),
        )
        advanceUntilIdle()

        assertEquals(AppStartupState.Ready, viewModel.state.value)
    }

    @Test
    fun `a throwing server mode refresh is reported and startup still reaches Ready`() =
        runTest(dispatcher) {
            val crashReporter = RecordingCrashReporter()
            val thrown = IllegalStateException("boom")
            val viewModel = AppStartupViewModel(
                observeOnboardingCompletion = ObserveOnboardingCompletion { MutableStateFlow(true) },
                requestRootAccess = RequestRootAccess { Result.success(Unit) },
                refreshServerMode = RefreshServerMode { throw thrown },
                hasCachedServerMode = { false },
                refreshNavigationFlags = RefreshNavigationFlags {},
                crashReporter = crashReporter,
            )
            advanceUntilIdle()

            assertEquals(AppStartupState.Ready, viewModel.state.value)
            assertEquals(listOf(thrown), crashReporter.recordedExceptions)
        }

    @Test
    fun `a throwing navigation flags refresh is reported and startup still reaches Ready`() =
        runTest(dispatcher) {
            val crashReporter = RecordingCrashReporter()
            val thrown = IllegalStateException("boom")
            val viewModel = AppStartupViewModel(
                observeOnboardingCompletion = ObserveOnboardingCompletion { MutableStateFlow(true) },
                requestRootAccess = RequestRootAccess { Result.success(Unit) },
                refreshServerMode = RefreshServerMode {},
                hasCachedServerMode = { true },
                refreshNavigationFlags = RefreshNavigationFlags { throw thrown },
                crashReporter = crashReporter,
            )
            advanceUntilIdle()

            assertEquals(AppStartupState.Ready, viewModel.state.value)
            assertEquals(listOf(thrown), crashReporter.recordedExceptions)
        }

    @Test
    fun `a cancelled refresh does not continue startup`() = runTest(dispatcher) {
        var onboardingReads = 0
        val crashReporter = RecordingCrashReporter()
        val viewModel = AppStartupViewModel(
            observeOnboardingCompletion = ObserveOnboardingCompletion {
                onboardingReads++
                MutableStateFlow(true)
            },
            requestRootAccess = RequestRootAccess { Result.success(Unit) },
            refreshServerMode = RefreshServerMode { throw CancellationException("cleared") },
            hasCachedServerMode = { false },
            refreshNavigationFlags = RefreshNavigationFlags {},
            crashReporter = crashReporter,
        )
        advanceUntilIdle()

        assertEquals(0, onboardingReads)
        assertEquals(AppStartupState.Loading, viewModel.state.value)
        assertEquals(emptyList<Throwable>(), crashReporter.recordedExceptions)
    }

    @Test
    fun `a cached value refreshes without delaying startup`() = runTest(dispatcher) {
        var refreshes = 0
        val viewModel = AppStartupViewModel(
            observeOnboardingCompletion = ObserveOnboardingCompletion { MutableStateFlow(true) },
            requestRootAccess = RequestRootAccess { Result.success(Unit) },
            refreshServerMode = RefreshServerMode { refreshes++ },
            hasCachedServerMode = { true },
            refreshNavigationFlags = RefreshNavigationFlags {},
            crashReporter = RecordingCrashReporter(),
        )
        advanceUntilIdle()

        assertEquals(1, refreshes)
        assertEquals(AppStartupState.Ready, viewModel.state.value)
    }

    @Test
    fun `navigation flags refresh in the background on every launch`() = runTest(dispatcher) {
        var refreshes = 0
        val viewModel = AppStartupViewModel(
            observeOnboardingCompletion = ObserveOnboardingCompletion { MutableStateFlow(true) },
            requestRootAccess = RequestRootAccess { Result.success(Unit) },
            refreshServerMode = RefreshServerMode {},
            hasCachedServerMode = { true },
            refreshNavigationFlags = RefreshNavigationFlags { refreshes++ },
            crashReporter = RecordingCrashReporter(),
        )
        advanceUntilIdle()

        assertEquals(1, refreshes)
        assertEquals(AppStartupState.Ready, viewModel.state.value)
    }

    @Test
    fun `a hanging navigation flags refresh cannot block startup`() = runTest(dispatcher) {
        val viewModel = AppStartupViewModel(
            observeOnboardingCompletion = ObserveOnboardingCompletion { MutableStateFlow(true) },
            requestRootAccess = RequestRootAccess { Result.success(Unit) },
            refreshServerMode = RefreshServerMode {},
            hasCachedServerMode = { true },
            refreshNavigationFlags = RefreshNavigationFlags { awaitCancellation() },
            crashReporter = RecordingCrashReporter(),
        )
        advanceUntilIdle()

        assertEquals(AppStartupState.Ready, viewModel.state.value)
    }

    @Test
    fun `a failing navigation flags refresh does not stop startup`() = runTest(dispatcher) {
        val viewModel = AppStartupViewModel(
            observeOnboardingCompletion = ObserveOnboardingCompletion { MutableStateFlow(true) },
            requestRootAccess = RequestRootAccess { Result.success(Unit) },
            refreshServerMode = RefreshServerMode {},
            hasCachedServerMode = { true },
            refreshNavigationFlags = RefreshNavigationFlags { throw IllegalStateException("boom") },
            crashReporter = RecordingCrashReporter(),
        )
        advanceUntilIdle()

        assertEquals(AppStartupState.Ready, viewModel.state.value)
    }

    @Test
    fun `navigation flags refresh in the background on first install`() = runTest(dispatcher) {
        var refreshes = 0
        val viewModel = AppStartupViewModel(
            observeOnboardingCompletion = ObserveOnboardingCompletion { MutableStateFlow(true) },
            requestRootAccess = RequestRootAccess { Result.success(Unit) },
            refreshServerMode = RefreshServerMode {},
            hasCachedServerMode = { false },
            refreshNavigationFlags = RefreshNavigationFlags { refreshes++ },
            crashReporter = RecordingCrashReporter(),
        )
        advanceUntilIdle()

        assertEquals(1, refreshes)
        assertEquals(AppStartupState.Ready, viewModel.state.value)
    }

    @Test
    fun `navigation flags refresh still runs when the server mode refresh hangs through the whole timeout on first install`() =
        runTest(dispatcher) {
            var refreshes = 0
            val viewModel = AppStartupViewModel(
                observeOnboardingCompletion = ObserveOnboardingCompletion { MutableStateFlow(true) },
                requestRootAccess = RequestRootAccess { Result.success(Unit) },
                refreshServerMode = RefreshServerMode { awaitCancellation() },
                hasCachedServerMode = { false },
                refreshNavigationFlags = RefreshNavigationFlags { refreshes++ },
                crashReporter = RecordingCrashReporter(),
            )
            advanceUntilIdle()

            assertEquals(1, refreshes)
            assertEquals(AppStartupState.Ready, viewModel.state.value)
        }
}
