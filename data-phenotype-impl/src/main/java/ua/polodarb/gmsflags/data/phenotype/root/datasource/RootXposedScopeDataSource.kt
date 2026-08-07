package ua.polodarb.gmsflags.data.phenotype.root.datasource

import ua.polodarb.gmsflags.data.phenotype.root.connector.PhenotypeRootServiceConnector
import ua.polodarb.gmsflags.data.repository.apps.datasource.XposedScopeDataSource
import ua.polodarb.gmsflags.data.repository.apps.datasource.XposedScopeSnapshot

internal class RootXposedScopeDataSource(
    private val connector: PhenotypeRootServiceConnector,
) : XposedScopeDataSource {
    override suspend fun readScope(
        modulePackageName: String,
        userId: Int,
    ): Result<XposedScopeSnapshot> = connector.call { service ->
        service.readXposedScope(modulePackageName, userId).let { snapshot ->
            XposedScopeSnapshot(
                moduleFound = snapshot.moduleFound,
                moduleEnabled = snapshot.moduleEnabled,
                scopedPackageNames = snapshot.scopedPackageNames.toSet(),
            )
        }
    }
}
