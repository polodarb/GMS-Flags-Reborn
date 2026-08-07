package ua.polodarb.gmsflags.data.repository.impl.flags

import ua.polodarb.gmsflags.data.repository.flags.HooksRepository
import ua.polodarb.gmsflags.data.repository.phenotype.PhenotypeHookOverrideRecord
import ua.polodarb.gmsflags.data.repository.phenotype.PhenotypeHooksDataSource

internal class HooksRepositoryImpl(
    private val dataSource: PhenotypeHooksDataSource,
    private val targetRestarter: TargetProcessRestarter,
) : HooksRepository {
    override suspend fun applyHooks(
        androidPackageName: String,
        hooks: List<PhenotypeHookOverrideRecord>,
    ): Result<Unit> {
        if (hooks.isEmpty()) return Result.success(Unit)
        return dataSource.writeMicroHooks(androidPackageName, hooks)
            .restartTargetOnSuccess(androidPackageName)
    }

    override suspend fun deleteHooks(
        androidPackageName: String,
        recipeIds: List<Long>,
    ): Result<Unit> {
        if (recipeIds.isEmpty()) return Result.success(Unit)
        return dataSource.deleteMicroHooks(androidPackageName, recipeIds)
            .restartTargetOnSuccess(androidPackageName)
    }

    private suspend fun Result<Unit>.restartTargetOnSuccess(
        androidPackageName: String,
    ): Result<Unit> = fold(
        onSuccess = { targetRestarter.restart(androidPackageName) },
        onFailure = { Result.failure(it) },
    )
}
