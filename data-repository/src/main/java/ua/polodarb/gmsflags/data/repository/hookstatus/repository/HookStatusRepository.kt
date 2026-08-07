package ua.polodarb.gmsflags.data.repository.hookstatus.repository

import ua.polodarb.gmsflags.domain.hookstatus.HookDiagnosticRecord

interface HookStatusRepository {
    suspend fun readDiagnostics(androidPackageNames: List<String>): Result<List<HookDiagnosticRecord>>
    suspend fun restart(androidPackageName: String): Result<Unit>
}
