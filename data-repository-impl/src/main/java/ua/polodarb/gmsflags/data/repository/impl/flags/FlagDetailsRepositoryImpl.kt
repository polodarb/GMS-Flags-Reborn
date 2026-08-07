package ua.polodarb.gmsflags.data.repository.impl.flags

import ua.polodarb.gmsflags.data.repository.flags.FlagDetailsRepository
import ua.polodarb.gmsflags.data.repository.flags.FlagOverridesChangeBus
import ua.polodarb.gmsflags.data.repository.phenotype.PhenotypeFlagsDataSource
import ua.polodarb.gmsflags.data.repository.phenotype.PhenotypeOverrideRecord
import ua.polodarb.gmsflags.domain.flags.FlagOverride
import ua.polodarb.gmsflags.domain.flags.FlagOverridesChange
import ua.polodarb.gmsflags.domain.flags.FlagType
import ua.polodarb.gmsflags.domain.flags.PhenotypeFlag

internal class FlagDetailsRepositoryImpl(
    private val dataSource: PhenotypeFlagsDataSource,
    private val targetRestarter: TargetProcessRestarter,
    private val changeBus: FlagOverridesChangeBus,
) : FlagDetailsRepository {
    override suspend fun getFlags(
        androidPackageName: String,
        phenotypePackageName: String,
    ): Result<List<PhenotypeFlag>> = dataSource
        .readFlags(androidPackageName, phenotypePackageName)
        .map { records ->
            records.mapNotNull { record ->
                val type = FlagType.fromStorageId(record.type) ?: return@mapNotNull null
                PhenotypeFlag(
                    name = record.name,
                    type = type,
                    originalValue = record.originalValue,
                    value = record.value,
                    overridden = record.overridden,
                )
            }.sortedWith(compareBy(PhenotypeFlag::type).thenBy(PhenotypeFlag::name))
        }

    override suspend fun applyOverrides(
        androidPackageName: String,
        phenotypePackageName: String,
        overrides: List<FlagOverride>,
    ): Result<Unit> = dataSource.writeOverrides(
        androidPackageName,
        phenotypePackageName,
        overrides.map { override ->
            PhenotypeOverrideRecord(
                name = override.name,
                type = override.type.storageId,
                value = override.value,
            )
        },
    ).restartTargetOnSuccess(
        androidPackageName = androidPackageName,
        change = FlagOverridesChange.Applied(
            androidPackageName = androidPackageName,
            phenotypePackageName = phenotypePackageName,
            overrides = overrides,
        ),
    )

    override suspend fun deleteOverride(
        androidPackageName: String,
        phenotypePackageName: String,
        flagName: String,
    ): Result<Unit> = dataSource
        .deleteOverride(androidPackageName, phenotypePackageName, flagName)
        .restartTargetOnSuccess(
            androidPackageName = androidPackageName,
            change = FlagOverridesChange.Removed(
                androidPackageName = androidPackageName,
                phenotypePackageName = phenotypePackageName,
                flagNames = setOf(flagName),
            ),
        )

    override suspend fun deleteOverrides(
        androidPackageName: String,
        phenotypePackageName: String,
        flagNames: List<String>,
    ): Result<Unit> = dataSource
        .deleteOverrides(androidPackageName, phenotypePackageName, flagNames)
        .restartTargetOnSuccess(
            androidPackageName = androidPackageName,
            change = FlagOverridesChange.Removed(
                androidPackageName = androidPackageName,
                phenotypePackageName = phenotypePackageName,
                flagNames = flagNames.toSet(),
            ),
        )

    override suspend fun deletePackageOverrides(
        androidPackageName: String,
        phenotypePackageName: String,
    ): Result<Unit> = dataSource
        .deletePackageOverrides(androidPackageName, phenotypePackageName)
        .restartTargetOnSuccess(
            androidPackageName = androidPackageName,
            change = FlagOverridesChange.PackageCleared(
                androidPackageName = androidPackageName,
                phenotypePackageName = phenotypePackageName,
            ),
        )

    private suspend fun Result<Unit>.restartTargetOnSuccess(
        androidPackageName: String,
        change: FlagOverridesChange,
    ): Result<Unit> = fold(
        onSuccess = {
            changeBus.notifyChanged(change)
            targetRestarter.restart(androidPackageName)
        },
        onFailure = { Result.failure(it) },
    )
}
