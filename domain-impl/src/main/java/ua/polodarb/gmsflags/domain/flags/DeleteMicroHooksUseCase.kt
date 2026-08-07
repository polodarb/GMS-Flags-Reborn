package ua.polodarb.gmsflags.domain.flags

import ua.polodarb.gmsflags.data.repository.flags.HooksRepository

class DeleteMicroHooksUseCase(
    private val repository: HooksRepository,
) : DeleteMicroHooks {
    override suspend fun invoke(
        androidPackageName: String,
        recipeIds: List<Long>,
    ): Result<Unit> = repository.deleteHooks(androidPackageName, recipeIds)
}
