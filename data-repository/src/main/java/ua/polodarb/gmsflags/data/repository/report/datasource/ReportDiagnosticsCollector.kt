package ua.polodarb.gmsflags.data.repository.report.datasource

import ua.polodarb.gmsflags.domain.report.ReportDiagnostics

/**
 * Reads THIS build's device / OS / version / signing-certificate diagnostics. Synchronous and
 * root-free; implementations must never throw (they return best-effort values, e.g. a null
 * signature, when something cannot be read).
 */
fun interface ReportDiagnosticsCollector {
    fun collect(): ReportDiagnostics
}
