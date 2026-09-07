package ua.polodarb.gmsflags.startup

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
        )
        advanceUntilIdle()

        assertEquals(AppStartupState.Ready, viewModel.state.value)
    }

    @Test
    fun `a cached value refreshes without delaying startup`() = runTest(dispatcher) {
        var refreshes = 0
        val viewModel = AppStartupViewModel(
            observeOnboardingCompletion = ObserveOnboardingCompletion { MutableStateFlow(true) },
            requestRootAccess = RequestRootAccess { Result.success(Unit) },
            refreshServerMode = RefreshServerMode { refreshes++ },
            hasCachedServerMode = { true },
        )
        advanceUntilIdle()

        assertEquals(1, refreshes)
        assertEquals(AppStartupState.Ready, viewModel.state.value)
    }
}
