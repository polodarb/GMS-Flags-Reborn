package ua.polodarb.gmsflags.domain.report.di

import org.koin.dsl.module
import ua.polodarb.gmsflags.domain.report.CollectReportDiagnostics
import ua.polodarb.gmsflags.domain.report.CollectReportDiagnosticsUseCase
import ua.polodarb.gmsflags.domain.report.SubmitProblemReport
import ua.polodarb.gmsflags.domain.report.SubmitProblemReportUseCase

val reportsDomainModule = module {
    factory<CollectReportDiagnostics> { CollectReportDiagnosticsUseCase(repository = get()) }
    factory<SubmitProblemReport> { SubmitProblemReportUseCase(repository = get()) }
}
