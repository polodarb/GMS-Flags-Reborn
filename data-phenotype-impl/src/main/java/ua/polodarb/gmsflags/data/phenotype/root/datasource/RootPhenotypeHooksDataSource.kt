package ua.polodarb.gmsflags.data.phenotype.root.datasource

import ua.polodarb.gmsflags.data.phenotype.root.connector.PhenotypeRootServiceConnector
import ua.polodarb.gmsflags.data.phenotype.root.parcel.MicroHookEnvelopeParcel
import ua.polodarb.gmsflags.data.repository.phenotype.PhenotypeHookOverrideRecord
import ua.polodarb.gmsflags.data.repository.phenotype.PhenotypeHooksDataSource

internal class RootPhenotypeHooksDataSource(
    private val connector: PhenotypeRootServiceConnector,
) : PhenotypeHooksDataSource {
    override suspend fun writeMicroHooks(
        androidPackageName: String,
        hooks: List<PhenotypeHookOverrideRecord>,
    ): Result<Unit> = connector.call { service ->
        service.writeMicroHooks(
            androidPackageName,
            hooks.map { hook ->
                MicroHookEnvelopeParcel(
                    recipeId = hook.recipeId,
                    payloadBase64 = hook.payloadBase64,
                    payloadSha256 = hook.payloadSha256,
                    signatureBase64 = hook.signatureBase64,
                    required = hook.required,
                )
            },
        )
    }

    override suspend fun deleteMicroHooks(
        androidPackageName: String,
        recipeIds: List<Long>,
    ): Result<Unit> = connector.call { service ->
        service.deleteMicroHooks(androidPackageName, recipeIds.toLongArray())
    }
}
