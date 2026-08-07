package ua.polodarb.gmsflags.data.repository.phenotype

data class PhenotypeHookOverrideRecord(
    val recipeId: Long,
    val payloadBase64: String,
    val payloadSha256: String,
    val signatureBase64: String,
    val required: Boolean,
)

interface PhenotypeHooksDataSource {
    suspend fun writeMicroHooks(
        androidPackageName: String,
        hooks: List<PhenotypeHookOverrideRecord>,
    ): Result<Unit>

    suspend fun deleteMicroHooks(
        androidPackageName: String,
        recipeIds: List<Long>,
    ): Result<Unit>
}
