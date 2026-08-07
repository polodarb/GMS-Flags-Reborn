package ua.polodarb.gmsflags.domain.flags

import ua.polodarb.gmsflags.data.repository.flags.FlagDetailsRepository

class GetPhenotypeFlagsUseCase(
    private val repository: FlagDetailsRepository,
) : GetPhenotypeFlags {
    override suspend fun invoke(
        androidPackageName: String,
        phenotypePackageName: String,
    ): Result<List<PhenotypeFlag>> = validatePackageNames(
        androidPackageName,
        phenotypePackageName,
    ).fold(
        onSuccess = { (androidPackage, phenotypePackage) ->
            repository.getFlags(androidPackage, phenotypePackage)
        },
        onFailure = { Result.failure(it) },
    )
}

internal fun String.requirePackageName(): String = trim().also {
    require(it.isNotEmpty()) { "Package name cannot be empty" }
    require(it.none(Char::isWhitespace)) { "Package name cannot contain whitespace" }
}

internal fun String.requireAndroidPackageName(): String = requirePackageName().also {
    require(ANDROID_PACKAGE_PATTERN.matches(it)) { "Invalid Android package name" }
}

internal fun validatePackageNames(
    androidPackageName: String,
    phenotypePackageName: String,
): Result<Pair<String, String>> = runCatching {
    androidPackageName.requireAndroidPackageName() to phenotypePackageName.requirePackageName()
}

private val ANDROID_PACKAGE_PATTERN = Regex("[A-Za-z0-9_]+(?:\\.[A-Za-z0-9_]+)+")
