package ua.polodarb.gmsflags.domain.flags

import ua.polodarb.gmsflags.data.repository.flags.FlagDetailsRepository

class DeleteFlagOverridesUseCase(
    private val repository: FlagDetailsRepository,
) : DeleteFlagOverrides {
    override suspend fun invoke(
        androidPackageName: String,
        phenotypePackageName: String,
        flagNames: List<String>,
    ): Result<Unit> = runCatching {
        Triple(
            androidPackageName.requireAndroidPackageName(),
            phenotypePackageName.requirePackageName(),
            flagNames.map(String::trim).filter(String::isNotEmpty).distinct(),
        )
    }.fold(
        onSuccess = { (androidPackage, phenotypePackage, names) ->
            if (names.isEmpty()) Result.success(Unit)
            else repository.deleteOverrides(androidPackage, phenotypePackage, names)
        },
        onFailure = { Result.failure(it) },
    )
}
