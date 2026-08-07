package ua.polodarb.gmsflags.domain.flags

import ua.polodarb.gmsflags.domain.server.content.RecommendationVariantHook

fun interface GetPhenotypeFlags {
    suspend operator fun invoke(
        androidPackageName: String,
        phenotypePackageName: String,
    ): Result<List<PhenotypeFlag>>
}

fun interface ApplyFlagOverrides {
    suspend operator fun invoke(
        androidPackageName: String,
        phenotypePackageName: String,
        overrides: List<FlagOverride>,
    ): Result<Unit>
}

fun interface DeleteFlagOverride {
    suspend operator fun invoke(
        androidPackageName: String,
        phenotypePackageName: String,
        flagName: String,
    ): Result<Unit>
}

fun interface DeleteFlagOverrides {
    suspend operator fun invoke(
        androidPackageName: String,
        phenotypePackageName: String,
        flagNames: List<String>,
    ): Result<Unit>
}

fun interface DeletePackageOverrides {
    suspend operator fun invoke(
        androidPackageName: String,
        phenotypePackageName: String,
    ): Result<Unit>
}

fun interface ApplyMicroHooks {
    suspend operator fun invoke(
        androidPackageName: String,
        hooks: List<RecommendationVariantHook>,
    ): Result<Unit>
}

fun interface DeleteMicroHooks {
    suspend operator fun invoke(
        androidPackageName: String,
        recipeIds: List<Long>,
    ): Result<Unit>
}
