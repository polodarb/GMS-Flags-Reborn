package ua.polodarb.gmsflags.domain.hookstatus

import ua.polodarb.gmsflags.data.repository.hookstatus.repository.HookStatusRepository

internal class RestartHookTargetUseCase(
    private val repository: HookStatusRepository,
) : RestartHookTarget {
    override suspend fun invoke(androidPackageName: String): Result<Unit> =
        repository.restart(androidPackageName)
}
