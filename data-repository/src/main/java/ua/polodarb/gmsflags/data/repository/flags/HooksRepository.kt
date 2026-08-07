package ua.polodarb.gmsflags.data.repository.flags

import ua.polodarb.gmsflags.data.repository.phenotype.PhenotypeHookOverrideRecord

interface HooksRepository {
    suspend fun applyHooks(
        androidPackageName: String,
        hooks: List<PhenotypeHookOverrideRecord>,
    ): Result<Unit>

    suspend fun deleteHooks(
        androidPackageName: String,
        recipeIds: List<Long>,
    ): Result<Unit>
}
