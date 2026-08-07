package ua.polodarb.gmsflags.domain.report

/**
 * Collects the device / OS / app-version / signing-certificate diagnostics that a report attaches.
 */
fun interface CollectReportDiagnostics {
    suspend operator fun invoke(): Result<ReportDiagnostics>
}

/**
 * Sends a fully-assembled [ProblemReport] to the backend.
 */
fun interface SubmitProblemReport {
    suspend operator fun invoke(report: ProblemReport): Result<Unit>
}
