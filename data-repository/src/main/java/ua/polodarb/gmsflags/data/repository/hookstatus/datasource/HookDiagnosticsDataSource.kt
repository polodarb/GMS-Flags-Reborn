package ua.polodarb.gmsflags.data.repository.hookstatus.datasource

import ua.polodarb.gmsflags.domain.hookstatus.HookDiagnosticRecord

fun interface HookDiagnosticsDataSource {
    suspend fun read(androidPackageNames: List<String>): Result<List<HookDiagnosticRecord>>
}
