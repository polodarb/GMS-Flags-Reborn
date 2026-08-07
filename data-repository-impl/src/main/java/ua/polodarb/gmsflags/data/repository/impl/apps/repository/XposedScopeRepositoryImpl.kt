package ua.polodarb.gmsflags.data.repository.impl.apps.repository

import ua.polodarb.gmsflags.data.repository.apps.datasource.XposedScopeDataSource
import ua.polodarb.gmsflags.data.repository.apps.repository.XposedScopeRepository
import ua.polodarb.gmsflags.domain.apps.XposedScopeStatus

internal class XposedScopeRepositoryImpl(
    private val dataSource: XposedScopeDataSource,
    private val modulePackageName: String,
    private val userId: Int,
) : XposedScopeRepository {
    override suspend fun getApplicationStatus(
        androidPackageName: String,
    ): Result<XposedScopeStatus> = dataSource.readScope(modulePackageName, userId).map { scope ->
        when {
            !scope.moduleFound || !scope.moduleEnabled -> XposedScopeStatus.Excluded
            androidPackageName in scope.scopedPackageNames -> XposedScopeStatus.Included
            else -> XposedScopeStatus.Excluded
        }
    }
}
