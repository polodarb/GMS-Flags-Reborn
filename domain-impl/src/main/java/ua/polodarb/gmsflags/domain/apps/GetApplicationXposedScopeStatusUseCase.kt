package ua.polodarb.gmsflags.domain.apps

import ua.polodarb.gmsflags.data.repository.apps.repository.XposedScopeRepository

class GetApplicationXposedScopeStatusUseCase(
    private val repository: XposedScopeRepository,
) : GetApplicationXposedScopeStatus {
    override suspend fun invoke(androidPackageName: String): Result<XposedScopeStatus> =
        repository.getApplicationStatus(androidPackageName)
}
