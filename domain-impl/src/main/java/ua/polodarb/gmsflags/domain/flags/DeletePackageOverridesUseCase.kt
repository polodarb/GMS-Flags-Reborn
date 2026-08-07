package ua.polodarb.gmsflags.domain.flags

import ua.polodarb.gmsflags.data.repository.flags.FlagDetailsRepository

class DeletePackageOverridesUseCase(
    private val repository: FlagDetailsRepository,
) : DeletePackageOverrides {
    override suspend fun invoke(
        androidPackageName: String,
        phenotypePackageName: String,
    ): Result<Unit> = validatePackageNames(androidPackageName, phenotypePackageName).fold(
        onSuccess = { (androidPackage, phenotypePackage) ->
            repository.deletePackageOverrides(androidPackage, phenotypePackage)
        },
        onFailure = { Result.failure(it) },
    )
}
