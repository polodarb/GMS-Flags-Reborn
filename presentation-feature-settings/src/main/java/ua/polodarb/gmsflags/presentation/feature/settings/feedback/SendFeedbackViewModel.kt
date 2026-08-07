package ua.polodarb.gmsflags.presentation.feature.settings.feedback

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import ua.polodarb.gmsflags.domain.report.CollectReportDiagnostics
import ua.polodarb.gmsflags.domain.report.ProblemReport
import ua.polodarb.gmsflags.domain.report.ProblemReportContext
import ua.polodarb.gmsflags.domain.report.SubmitProblemReport
import ua.polodarb.gmsflags.presentation.core.viewmodel.BaseViewModel

internal class SendFeedbackViewModel(
    private val collectReportDiagnostics: CollectReportDiagnostics,
    private val submitProblemReport: SubmitProblemReport,
) : BaseViewModel<SendFeedbackEvent, SendFeedbackState, SendFeedbackEffect>() {
    override fun initialState() = SendFeedbackState()

    private var sendJob: Job? = null

    init {
        viewModelScope.launch {
            val diagnostics = collectReportDiagnostics().getOrNull()
            setState { copy(diagnostics = diagnostics, diagnosticsReady = true) }
        }
    }

    override fun handleEvent(event: SendFeedbackEvent) {
        when (event) {
            is SendFeedbackEvent.CategorySelected -> setState { copy(category = event.category) }
            is SendFeedbackEvent.MessageChanged -> setState { copy(message = event.message) }
            is SendFeedbackEvent.ContactChanged -> setState { copy(contact = event.contact) }
            SendFeedbackEvent.DetailsToggled -> setState { copy(detailsExpanded = !detailsExpanded) }
            SendFeedbackEvent.SendClicked -> send()
            SendFeedbackEvent.Dismissed -> {
                sendJob?.cancel()
                setState {
                    copy(
                        category = null,
                        message = "",
                        contact = "",
                        detailsExpanded = false,
                        phase = SendFeedbackPhase.Editing,
                    )
                }
            }
        }
    }

    private fun send() {
        val state = viewState.value
        if (!state.canSend()) return
        val category = state.category ?: return
        val diagnostics = state.diagnostics ?: return
        val report = ProblemReport(
            message = state.message.trim(),
            contact = state.contact.trim().ifBlank { null },
            diagnostics = diagnostics,
            context = ProblemReportContext.EMPTY,
            category = category,
        )
        setState { copy(phase = SendFeedbackPhase.Sending) }
        sendJob?.cancel()
        sendJob = viewModelScope.launch {
            val phase = submitProblemReport(report).fold(
                onSuccess = { SendFeedbackPhase.Sent },
                onFailure = { SendFeedbackPhase.Error },
            )
            setState { copy(phase = phase) }
        }
    }
}
