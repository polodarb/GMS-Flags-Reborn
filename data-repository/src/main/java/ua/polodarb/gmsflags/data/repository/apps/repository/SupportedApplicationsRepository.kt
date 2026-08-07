package ua.polodarb.gmsflags.data.repository.apps.repository

import ua.polodarb.gmsflags.domain.apps.SupportedApplication
import ua.polodarb.gmsflags.domain.apps.SupportedApplicationsSnapshot
import ua.polodarb.gmsflags.domain.apps.XposedModuleStatus

interface SupportedApplicationsRepository {
    suspend fun getApplications(): Result<List<SupportedApplication>>

    suspend fun getSnapshot(): Result<SupportedApplicationsSnapshot> =
        getApplications().map { applications ->
            SupportedApplicationsSnapshot(
                applications = applications,
                moduleStatus = XposedModuleStatus.Unknown,
            )
        }
}
