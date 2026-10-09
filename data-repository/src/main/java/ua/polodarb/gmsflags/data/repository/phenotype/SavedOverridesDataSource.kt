package ua.polodarb.gmsflags.data.repository.phenotype

data class SavedOverrideRecord(
    val packageName: String,
    val flag: PhenotypeOverrideRecord,
)

interface SavedOverridesDataSource {
    suspend fun readAll(androidPackageName: String): Result<List<SavedOverrideRecord>>
}
