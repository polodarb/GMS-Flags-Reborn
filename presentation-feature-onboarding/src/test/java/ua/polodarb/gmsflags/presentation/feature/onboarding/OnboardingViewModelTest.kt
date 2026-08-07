package ua.polodarb.gmsflags.presentation.feature.onboarding

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import ua.polodarb.gmsflags.domain.onboarding.CompleteOnboarding
import ua.polodarb.gmsflags.domain.onboarding.RequestRootAccess
import ua.polodarb.gmsflags.presentation.feature.onboarding.mvi.OnboardingEvent
import ua.polodarb.gmsflags.presentation.feature.onboarding.mvi.OnboardingStep

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {
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
    fun `welcome advances to the disclaimer step`() = runTest(dispatcher) {
        val viewModel = viewModel()
        viewModel.setEvent(OnboardingEvent.ContinueFromWelcome)
        advanceUntilIdle()

        assertEquals(OnboardingStep.Disclaimer, viewModel.viewState.value.step)
    }

    @Test
    fun `successful root request advances to notifications`() = runTest(dispatcher) {
        val viewModel = viewModel(rootResult = Result.success(Unit))
        viewModel.setEvent(OnboardingEvent.ContinueFromWelcome)
        viewModel.setEvent(OnboardingEvent.ContinueFromDisclaimer)
        viewModel.setEvent(OnboardingEvent.RequestRoot)
        advanceUntilIdle()

        assertEquals(OnboardingStep.Notifications, viewModel.viewState.value.step)
        assertFalse(viewModel.viewState.value.requestingRoot)
        assertFalse(viewModel.viewState.value.rootRequestFailed)
    }

    @Test
    fun `denied root stays on root step with recovery state`() = runTest(dispatcher) {
        val viewModel = viewModel(rootResult = Result.failure(IllegalStateException()))
        viewModel.setEvent(OnboardingEvent.ContinueFromWelcome)
        viewModel.setEvent(OnboardingEvent.ContinueFromDisclaimer)
        viewModel.setEvent(OnboardingEvent.RequestRoot)
        advanceUntilIdle()

        assertEquals(OnboardingStep.RootAccess, viewModel.viewState.value.step)
        assertTrue(viewModel.viewState.value.rootRequestFailed)
    }

    @Test
    fun `skipping notifications completes onboarding`() = runTest(dispatcher) {
        var completionCalls = 0
        val viewModel = viewModel(
            complete = CompleteOnboarding {
                completionCalls++
                Result.success(Unit)
            }
        )
        viewModel.setEvent(OnboardingEvent.SkipNotifications)
        advanceUntilIdle()

        assertEquals(1, completionCalls)
        assertTrue(viewModel.viewState.value.completing)
    }

    @Test
    fun `failed completion returns an actionable state`() = runTest(dispatcher) {
        val viewModel = viewModel(
            complete = CompleteOnboarding { Result.failure(IllegalStateException()) }
        )
        viewModel.setEvent(OnboardingEvent.SkipNotifications)
        advanceUntilIdle()

        assertFalse(viewModel.viewState.value.completing)
        assertTrue(viewModel.viewState.value.completionFailed)
    }

    private fun viewModel(
        rootResult: Result<Unit> = Result.success(Unit),
        complete: CompleteOnboarding = CompleteOnboarding { Result.success(Unit) },
    ) = OnboardingViewModel(
        requestRootAccess = RequestRootAccess { rootResult },
        completeOnboarding = complete,
    )
}
