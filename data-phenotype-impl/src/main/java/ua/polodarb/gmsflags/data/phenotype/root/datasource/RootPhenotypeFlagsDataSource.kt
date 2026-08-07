package ua.polodarb.gmsflags.data.phenotype.root.datasource

import ua.polodarb.gmsflags.data.phenotype.root.connector.PhenotypeRootServiceConnector
import ua.polodarb.gmsflags.data.phenotype.root.flags.PhenotypeFlagPageCollector
import ua.polodarb.gmsflags.data.phenotype.root.flags.PhenotypeOverridePager
import ua.polodarb.gmsflags.data.phenotype.root.parcel.PhenotypeFlagParcel
import ua.polodarb.gmsflags.data.repository.phenotype.PhenotypeFlagRecord
import ua.polodarb.gmsflags.data.repository.phenotype.PhenotypeFlagsDataSource
import ua.polodarb.gmsflags.data.repository.phenotype.PhenotypeOverrideRecord

internal class RootPhenotypeFlagsDataSource(
    private val connector: PhenotypeRootServiceConnector,
    private val pageCollector: PhenotypeFlagPageCollector = PhenotypeFlagPageCollector(),
    private val overridePager: PhenotypeOverridePager = PhenotypeOverridePager(),
) : PhenotypeFlagsDataSource {
    override suspend fun readFlags(
        androidPackageName: String,
        phenotypePackageName: String,
    ): Result<List<PhenotypeFlagRecord>> = connector.call { service ->
        pageCollector.readAll { offset, limit ->
            service.readFlagsPage(
                androidPackageName,
                phenotypePackageName,
                offset,
                limit,
            )
        }.map { flag ->
            PhenotypeFlagRecord(
                name = flag.name,
                type = flag.type,
                originalValue = flag.originalValue,
                value = flag.value,
                overridden = flag.overridden,
            )
        }
    }

    override suspend fun writeOverrides(
        androidPackageName: String,
        phenotypePackageName: String,
        overrides: List<PhenotypeOverrideRecord>,
    ): Result<Unit> = connector.call { service ->
        overridePager.pages(
            overrides.map { override ->
                PhenotypeFlagParcel(
                    name = override.name,
                    type = override.type,
                    originalValue = null,
                    value = override.value,
                    overridden = true,
                )
            },
        ).forEach { page ->
            service.writeOverrides(
                androidPackageName,
                phenotypePackageName,
                page,
            )
        }
    }

    override suspend fun deleteOverride(
        androidPackageName: String,
        phenotypePackageName: String,
        flagName: String,
    ): Result<Unit> = connector.call { service ->
        service.deleteOverride(androidPackageName, phenotypePackageName, flagName)
    }

    override suspend fun deleteOverrides(
        androidPackageName: String,
        phenotypePackageName: String,
        flagNames: List<String>,
    ): Result<Unit> = connector.call { service ->
        service.deleteOverrides(androidPackageName, phenotypePackageName, flagNames)
    }

    override suspend fun deletePackageOverrides(
        androidPackageName: String,
        phenotypePackageName: String,
    ): Result<Unit> = connector.call { service ->
        service.deletePackageOverrides(androidPackageName, phenotypePackageName)
    }
}
