package ua.polodarb.gmsflags.data.phenotype.root.datasource

import ua.polodarb.gmsflags.data.phenotype.root.connector.PhenotypeRootServiceConnector
import ua.polodarb.gmsflags.data.repository.report.datasource.XposedLogsDataSource

internal class RootXposedLogsDataSource(
    private val connector: PhenotypeRootServiceConnector,
) : XposedLogsDataSource {
    override suspend fun read(androidPackageName: String): Result<String> =
        connector.call { service -> service.readXposedLogs(androidPackageName) }
}
