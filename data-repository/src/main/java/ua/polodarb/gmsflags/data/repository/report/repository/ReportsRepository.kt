package ua.polodarb.gmsflags.data.repository.report.repository

import ua.polodarb.gmsflags.domain.report.ProblemReport
import ua.polodarb.gmsflags.domain.report.ReportDiagnostics

interface ReportsRepository {
    suspend fun collectDiagnostics(): Result<ReportDiagnostics>

    /** Attaches the target app's Xposed logs and submits the report to the backend. */
    suspend fun submit(report: ProblemReport): Result<Unit>
}
