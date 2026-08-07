package ua.polodarb.gmsflags.presentation.feature.settings.feedback

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Test
import ua.polodarb.gmsflags.domain.report.CollectReportDiagnostics
import ua.polodarb.gmsflags.domain.report.ProblemReport
import ua.polodarb.gmsflags.domain.report.ReportCategory
import ua.polodarb.gmsflags.domain.report.ReportDeviceInfo
import ua.polodarb.gmsflags.domain.report.ReportDiagnostics
import ua.polodarb.gmsflags.domain.report.SubmitProblemReport

private val SampleDiagnostics = ReportDiagnostics(
    device = ReportDeviceInfo("Google", "Pixel", "16", 36, "arm64-v8a"),
    appVersionName = "1.0.1",
    appVersionCode = 101,
    appSignatureSha256 = null,
)

private val NoOpDiagnostics = CollectReportDiagnostics { Result.success(SampleDiagnostics) }

@OptIn(ExperimentalCoroutinesApi::class)
class SendFeedbackViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() { Dispatchers.setMain(dispatcher) }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `cannot send until a category, a message, and diagnostics are all ready`() = runTest(dispatcher) {
        val viewModel = SendFeedbackViewModel(NoOpDiagnostics, SubmitProblemReport { Result.success(Unit) })
        advanceUntilIdle()

        assert(!viewModel.viewState.value.canSend())

        viewModel.setEvent(SendFeedbackEvent.MessageChanged("great app, one idea:"))
        assert(!viewModel.viewState.value.canSend())

        viewModel.setEvent(SendFeedbackEvent.CategorySelected(ReportCategory.Suggestion))
        assert(viewModel.viewState.value.canSend())
    }

    @Test
    fun `send submits a report with no recommendation context and the picked category`() = runTest(dispatcher) {
        var submitted: ProblemReport? = null
        val viewModel = SendFeedbackViewModel(
            NoOpDiagnostics,
            SubmitProblemReport { report -> submitted = report; Result.success(Unit) },
        )
        advanceUntilIdle()

        viewModel.setEvent(SendFeedbackEvent.CategorySelected(ReportCategory.Other))
        viewModel.setEvent(SendFeedbackEvent.MessageChanged("just a note"))
        viewModel.setEvent(SendFeedbackEvent.SendClicked)
        advanceUntilIdle()

        assertEquals(ReportCategory.Other, submitted?.category)
        assertNull(submitted?.context?.recommendationId)
        assertEquals(SendFeedbackPhase.Sent, viewModel.viewState.value.phase)
    }

    @Test
    fun `cannot send while diagnostics are still pending, even with category and message set`() = runTest(dispatcher) {
        val diagnosticsDeferred = CompletableDeferred<Result<ReportDiagnostics>>()
        val viewModel = SendFeedbackViewModel(
            CollectReportDiagnostics { diagnosticsDeferred.await() },
            SubmitProblemReport { Result.success(Unit) },
        )
        advanceUntilIdle()

        viewModel.setEvent(SendFeedbackEvent.CategorySelected(ReportCategory.Suggestion))
        viewModel.setEvent(SendFeedbackEvent.MessageChanged("great app, one idea:"))

        assertFalse(viewModel.viewState.value.diagnosticsReady)
        assertFalse(viewModel.viewState.value.canSend())

        diagnosticsDeferred.complete(Result.success(SampleDiagnostics))
        advanceUntilIdle()

        assertTrue(viewModel.viewState.value.diagnosticsReady)
        assertTrue(viewModel.viewState.value.canSend())
    }

    @Test
    fun `cannot send again while a submission is already in flight`() = runTest(dispatcher) {
        val submitDeferred = CompletableDeferred<Result<Unit>>()
        var submitCallCount = 0
        val viewModel = SendFeedbackViewModel(
            NoOpDiagnostics,
            SubmitProblemReport { submitCallCount++; submitDeferred.await() },
        )
        advanceUntilIdle()

        viewModel.setEvent(SendFeedbackEvent.CategorySelected(ReportCategory.Suggestion))
        viewModel.setEvent(SendFeedbackEvent.MessageChanged("in flight"))
        viewModel.setEvent(SendFeedbackEvent.SendClicked)

        assertEquals(SendFeedbackPhase.Sending, viewModel.viewState.value.phase)
        assertFalse(viewModel.viewState.value.canSend())

        viewModel.setEvent(SendFeedbackEvent.SendClicked)
        assertEquals(1, submitCallCount)

        submitDeferred.complete(Result.success(Unit))
        advanceUntilIdle()

        assertEquals(SendFeedbackPhase.Sent, viewModel.viewState.value.phase)
    }

    @Test
    fun `a failed submission moves to phase Error and canSend becomes true again`() = runTest(dispatcher) {
        val viewModel = SendFeedbackViewModel(
            NoOpDiagnostics,
            SubmitProblemReport { Result.failure(RuntimeException("network down")) },
        )
        advanceUntilIdle()

        viewModel.setEvent(SendFeedbackEvent.CategorySelected(ReportCategory.Suggestion))
        viewModel.setEvent(SendFeedbackEvent.MessageChanged("retry me"))
        viewModel.setEvent(SendFeedbackEvent.SendClicked)
        advanceUntilIdle()

        assertEquals(SendFeedbackPhase.Error, viewModel.viewState.value.phase)
        assertTrue(viewModel.viewState.value.canSend())
    }

    @Test
    fun `Dismissed resets the form back to a blank Editing state without losing diagnostics`() = runTest(dispatcher) {
        val viewModel = SendFeedbackViewModel(NoOpDiagnostics, SubmitProblemReport { Result.success(Unit) })
        advanceUntilIdle()

        viewModel.setEvent(SendFeedbackEvent.CategorySelected(ReportCategory.Bug))
        viewModel.setEvent(SendFeedbackEvent.MessageChanged("something broke"))
        viewModel.setEvent(SendFeedbackEvent.ContactChanged("me@example.com"))
        viewModel.setEvent(SendFeedbackEvent.SendClicked)
        advanceUntilIdle()

        assertEquals(SendFeedbackPhase.Sent, viewModel.viewState.value.phase)

        viewModel.setEvent(SendFeedbackEvent.Dismissed)

        val state = viewModel.viewState.value
        assertEquals(SendFeedbackPhase.Editing, state.phase)
        assertNull(state.category)
        assertEquals("", state.message)
        assertEquals("", state.contact)
        assertTrue(state.diagnosticsReady)
    }

    @Test
    fun `Dismissed cancels an in-flight submission instead of letting it overwrite the reset state`() =
        runTest(dispatcher) {
            val submitDeferred = CompletableDeferred<Result<Unit>>()
            val viewModel = SendFeedbackViewModel(
                NoOpDiagnostics,
                SubmitProblemReport { submitDeferred.await() },
            )
            advanceUntilIdle()

            viewModel.setEvent(SendFeedbackEvent.CategorySelected(ReportCategory.Bug))
            viewModel.setEvent(SendFeedbackEvent.MessageChanged("something broke"))
            viewModel.setEvent(SendFeedbackEvent.SendClicked)

            assertEquals(SendFeedbackPhase.Sending, viewModel.viewState.value.phase)

            viewModel.setEvent(SendFeedbackEvent.Dismissed)

            val resetState = viewModel.viewState.value
            assertEquals(SendFeedbackPhase.Editing, resetState.phase)
            assertNull(resetState.category)
            assertEquals("", resetState.message)

            submitDeferred.complete(Result.success(Unit))
            advanceUntilIdle()

            val finalState = viewModel.viewState.value
            assertEquals(SendFeedbackPhase.Editing, finalState.phase)
            assertNull(finalState.category)
            assertEquals("", finalState.message)
        }
}
