package ua.polodarb.gmsflags.domain.hookstatus

import ua.polodarb.gmsflags.data.repository.hookstatus.repository.HookStatusRepository

internal class GetPairipIncompatiblePackagesUseCase(
    private val repository: HookStatusRepository,
) : GetPairipIncompatiblePackages {
    override suspend fun invoke(
        androidPackageNames: List<String>,
    ): Result<Set<String>> = repository.readDiagnostics(androidPackageNames).map { records ->
        records
            .filter { HookCompatibilityWarning.PairipCore in it.compatibilityWarnings }
            .map { it.androidPackageName }
            .toSet()
    }
}
