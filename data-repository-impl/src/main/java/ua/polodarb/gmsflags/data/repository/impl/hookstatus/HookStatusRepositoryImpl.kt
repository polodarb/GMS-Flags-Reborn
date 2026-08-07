package ua.polodarb.gmsflags.data.repository.impl.hookstatus

import ua.polodarb.gmsflags.data.repository.hookstatus.datasource.HookDiagnosticsDataSource
import ua.polodarb.gmsflags.data.repository.hookstatus.repository.HookStatusRepository
import ua.polodarb.gmsflags.data.repository.impl.flags.TargetProcessRestarter
import ua.polodarb.gmsflags.domain.hookstatus.HookDiagnosticRecord

internal class HookStatusRepositoryImpl(
    private val dataSource: HookDiagnosticsDataSource,
    private val targetRestarter: TargetProcessRestarter,
) : HookStatusRepository {
    override suspend fun readDiagnostics(
        androidPackageNames: List<String>,
    ): Result<List<HookDiagnosticRecord>> = dataSource.read(androidPackageNames)

    override suspend fun restart(androidPackageName: String): Result<Unit> =
        targetRestarter.restart(androidPackageName)
}
