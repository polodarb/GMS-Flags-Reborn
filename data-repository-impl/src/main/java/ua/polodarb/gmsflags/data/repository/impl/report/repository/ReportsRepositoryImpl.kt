package ua.polodarb.gmsflags.data.repository.impl.report.repository

import ua.polodarb.gmsflags.data.network.publicapi.GmsFlagsPublicDataSource
import ua.polodarb.gmsflags.data.repository.impl.common.networkResult
import ua.polodarb.gmsflags.data.repository.impl.report.mapper.toNetModel
import ua.polodarb.gmsflags.data.repository.report.datasource.ReportDiagnosticsCollector
import ua.polodarb.gmsflags.data.repository.report.datasource.XposedLogsDataSource
import ua.polodarb.gmsflags.data.repository.report.repository.ReportsRepository
import ua.polodarb.gmsflags.domain.report.ProblemReport
import ua.polodarb.gmsflags.domain.report.ReportDiagnostics

internal class ReportsRepositoryImpl(
    private val dataSource: GmsFlagsPublicDataSource,
    private val diagnosticsCollector: ReportDiagnosticsCollector,
    private val xposedLogsDataSource: XposedLogsDataSource,
) : ReportsRepository {
    override suspend fun collectDiagnostics(): Result<ReportDiagnostics> = networkResult {
        diagnosticsCollector.collect()
    }

    override suspend fun submit(report: ProblemReport): Result<Unit> = networkResult {
        val xposedLogs = report.context.targetPackage
            ?.let { xposedLogsDataSource.read(it).getOrDefault("") }
            .orEmpty()
        dataSource.submitReport(report.toNetModel(xposedLogs))
        Unit
    }
}
