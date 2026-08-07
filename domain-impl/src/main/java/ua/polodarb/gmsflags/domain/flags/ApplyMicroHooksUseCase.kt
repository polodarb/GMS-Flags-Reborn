package ua.polodarb.gmsflags.domain.flags

import ua.polodarb.gmsflags.data.repository.flags.HooksRepository
import ua.polodarb.gmsflags.data.repository.phenotype.PhenotypeHookOverrideRecord
import ua.polodarb.gmsflags.domain.server.content.RecommendationVariantHook

class ApplyMicroHooksUseCase(
    private val repository: HooksRepository,
) : ApplyMicroHooks {
    override suspend fun invoke(
        androidPackageName: String,
        hooks: List<RecommendationVariantHook>,
    ): Result<Unit> {
        val signed = hooks.mapNotNull { hook ->
            val envelope = hook.envelope ?: return@mapNotNull null
            PhenotypeHookOverrideRecord(
                recipeId = hook.recipeId,
                payloadBase64 = envelope.payloadBase64,
                payloadSha256 = envelope.payloadSha256,
                signatureBase64 = envelope.signatureBase64,
                required = hook.required,
            )
        }
        if (signed.isEmpty()) return Result.success(Unit)
        return repository.applyHooks(androidPackageName, signed)
    }
}
