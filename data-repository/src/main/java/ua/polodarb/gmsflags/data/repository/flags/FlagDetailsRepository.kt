package ua.polodarb.gmsflags.data.repository.flags

import ua.polodarb.gmsflags.domain.flags.FlagOverride
import ua.polodarb.gmsflags.domain.flags.PhenotypeFlag

interface FlagDetailsRepository {
    suspend fun getFlags(
        androidPackageName: String,
        phenotypePackageName: String,
    ): Result<List<PhenotypeFlag>>

    suspend fun applyOverrides(
        androidPackageName: String,
        phenotypePackageName: String,
        overrides: List<FlagOverride>,
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
