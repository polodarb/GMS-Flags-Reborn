package ua.polodarb.gmsflags.data.repository.phenotype

data class PhenotypeFlagRecord(
    val name: String,
    val type: Int,
    val originalValue: String?,
    val value: String,
    val overridden: Boolean,
)

data class PhenotypeOverrideRecord(
    val name: String,
    val type: Int,
    val value: String,
)

interface PhenotypeFlagsDataSource {
    suspend fun readFlags(
        androidPackageName: String,
        phenotypePackageName: String,
    ): Result<List<PhenotypeFlagRecord>>

    suspend fun writeOverrides(
        androidPackageName: String,
        phenotypePackageName: String,
        overrides: List<PhenotypeOverrideRecord>,
    ): Result<Unit>

    suspend fun deleteOverride(
        androidPackageName: String,
        phenotypePackageName: String,
        flagName: String,
    ): Result<Unit>

    suspend fun deleteOverrides(
        androidPackageName: String,
        phenotypePackageName: String,
        flagNames: List<String>,
    ): Result<Unit>

    suspend fun deletePackageOverrides(
        androidPackageName: String,
        phenotypePackageName: String,
    ): Result<Unit>
}
