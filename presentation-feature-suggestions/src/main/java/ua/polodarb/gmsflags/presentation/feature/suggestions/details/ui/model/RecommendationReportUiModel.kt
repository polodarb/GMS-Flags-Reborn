package ua.polodarb.gmsflags.presentation.feature.suggestions.details.ui.model

import androidx.compose.runtime.Immutable
import ua.polodarb.gmsflags.domain.report.ProblemReportContext
import ua.polodarb.gmsflags.domain.report.ReportDiagnostics

/**
 * The transient state of the "report a problem" surface, held on the details screen state while the
 * report sheet is open (null = closed). Diagnostics are collected asynchronously the moment the sheet
 * opens so the "what will be sent" disclosure can preview the exact values before the user sends.
 *
 * Nothing is ever transmitted until [RecommendationReportPhase.Sending] - reached only by an explicit
 * Send tap.
 */
@Immutable
data class RecommendationReportUiState(
    val context: ProblemReportContext,
    val message: String = "",
    val contact: String = "",
    val diagnostics: ReportDiagnostics? = null,
    val diagnosticsReady: Boolean = false,
    val detailsExpanded: Boolean = false,
    val phase: RecommendationReportPhase = RecommendationReportPhase.Editing,
)

enum class RecommendationReportPhase { Editing, Sending, Sent, Error }

/** True only when the report is complete enough to send and no send is already in flight. */
fun RecommendationReportUiState.canSend(): Boolean =
    message.isNotBlank() &&
        diagnosticsReady &&
        (phase == RecommendationReportPhase.Editing || phase == RecommendationReportPhase.Error)
