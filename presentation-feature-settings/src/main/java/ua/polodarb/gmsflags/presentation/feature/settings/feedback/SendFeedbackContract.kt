package ua.polodarb.gmsflags.presentation.feature.settings.feedback

import ua.polodarb.gmsflags.domain.report.ReportCategory
import ua.polodarb.gmsflags.domain.report.ReportDiagnostics
import ua.polodarb.gmsflags.presentation.core.mvi.ViewEvent
import ua.polodarb.gmsflags.presentation.core.mvi.ViewSideEffect
import ua.polodarb.gmsflags.presentation.core.mvi.ViewState

data class SendFeedbackState(
    val category: ReportCategory? = null,
    val message: String = "",
    val contact: String = "",
    val diagnostics: ReportDiagnostics? = null,
    val diagnosticsReady: Boolean = false,
    val detailsExpanded: Boolean = false,
    val phase: SendFeedbackPhase = SendFeedbackPhase.Editing,
) : ViewState

enum class SendFeedbackPhase { Editing, Sending, Sent, Error }

fun SendFeedbackState.canSend(): Boolean =
    category != null &&
        message.isNotBlank() &&
        diagnosticsReady &&
        (phase == SendFeedbackPhase.Editing || phase == SendFeedbackPhase.Error)

sealed interface SendFeedbackEvent : ViewEvent {
    data class CategorySelected(val category: ReportCategory) : SendFeedbackEvent
    data class MessageChanged(val message: String) : SendFeedbackEvent
    data class ContactChanged(val contact: String) : SendFeedbackEvent
    data object DetailsToggled : SendFeedbackEvent
    data object SendClicked : SendFeedbackEvent
    data object Dismissed : SendFeedbackEvent
}

sealed interface SendFeedbackEffect : ViewSideEffect
