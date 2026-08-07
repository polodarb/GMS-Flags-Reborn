package ua.polodarb.gmsflags.domain.report

import ua.polodarb.gmsflags.data.repository.report.repository.ReportsRepository

class CollectReportDiagnosticsUseCase(
    private val repository: ReportsRepository,
) : CollectReportDiagnostics {
    override suspend fun invoke(): Result<ReportDiagnostics> = repository.collectDiagnostics()
}

class SubmitProblemReportUseCase(
    private val repository: ReportsRepository,
) : SubmitProblemReport {
    override suspend fun invoke(report: ProblemReport): Result<Unit> = repository.submit(report)
}
