package ua.polodarb.gmsflags.data.phenotype.root.datasource

import ua.polodarb.gmsflags.data.phenotype.root.connector.PhenotypeRootServiceConnector
import ua.polodarb.gmsflags.data.repository.settings.datasource.OverrideControlDataSource

internal class RootOverrideControlDataSource(
    private val connector: PhenotypeRootServiceConnector,
) : OverrideControlDataSource {
    override suspend fun readOverrideCount(androidPackageNames: List<String>): Result<Int> =
        connector.call { it.readOverrideCount(androidPackageNames) }

    override suspend fun readPaused(androidPackageNames: List<String>): Result<Boolean> =
        connector.call { it.readOverridesPaused(androidPackageNames) }

    override suspend fun setPaused(
        androidPackageNames: List<String>,
        paused: Boolean,
    ): Result<Unit> = connector.call { it.setOverridesPaused(androidPackageNames, paused) }

    override suspend fun deleteAll(androidPackageNames: List<String>): Result<Unit> =
        connector.call { it.deleteAllOverrides(androidPackageNames) }
}
