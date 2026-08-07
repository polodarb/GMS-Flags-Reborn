package ua.polodarb.gmsflags.domain.flags

import ua.polodarb.gmsflags.data.repository.flags.FlagDetailsRepository

class DeleteFlagOverrideUseCase(
    private val repository: FlagDetailsRepository,
) : DeleteFlagOverride {
    override suspend fun invoke(
        androidPackageName: String,
        phenotypePackageName: String,
        flagName: String,
    ): Result<Unit> = runCatching {
        Triple(
            androidPackageName.requireAndroidPackageName(),
            phenotypePackageName.requirePackageName(),
            flagName.trim().also { require(it.isNotEmpty()) { "Flag name cannot be empty" } },
        )
    }.fold(
        onSuccess = { (androidPackage, phenotypePackage, name) ->
            repository.deleteOverride(androidPackage, phenotypePackage, name)
        },
        onFailure = { Result.failure(it) },
    )
}
