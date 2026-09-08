package ua.polodarb.gmsflags.data.repository.impl.servermode

import ua.polodarb.gmsflags.data.repository.report.repository.ReportsRepository
import ua.polodarb.gmsflags.data.repository.server.PublicContentRepository
import ua.polodarb.gmsflags.data.repository.server.RemoteConfigurationRepository
import ua.polodarb.gmsflags.data.repository.servermode.ServerModeRepository
import ua.polodarb.gmsflags.domain.error.AppError
import ua.polodarb.gmsflags.domain.report.ProblemReport

private val offlineFailure get() = Result.failure<Nothing>(AppError.NetworkUnavailable)

class OfflinePublicContentRepository(
    private val delegate: PublicContentRepository,
    private val serverMode: ServerModeRepository,
) : PublicContentRepository {
    private val offline: Boolean get() = serverMode.mode.value.offline

    override suspend fun getHome() =
        if (offline) Result.success(emptyList()) else delegate.getHome()

    override suspend fun getFaq() =
        if (offline) Result.success(emptyList()) else delegate.getFaq()

    override suspend fun getApplications() =
        if (offline) Result.success(emptyList()) else delegate.getApplications()

    override suspend fun getApplication(packageName: String) =
        if (offline) offlineFailure else delegate.getApplication(packageName)

    override suspend fun getApplicationRecommendations(packageName: String) =
        if (offline) Result.success(emptyList()) else delegate.getApplicationRecommendations(packageName)

    override suspend fun getRecommendations() =
        if (offline) Result.success(emptyList()) else delegate.getRecommendations()

    override suspend fun getRecommendation(id: Long) =
        if (offline) offlineFailure else delegate.getRecommendation(id)
}

class OfflineRemoteConfigurationRepository(
    private val delegate: RemoteConfigurationRepository,
    private val serverMode: ServerModeRepository,
) : RemoteConfigurationRepository {
    private val offline: Boolean get() = serverMode.mode.value.offline

    override suspend fun resolveFlags(packageName: String, versionCode: Long, etag: String?) =
        if (offline) offlineFailure else delegate.resolveFlags(packageName, versionCode, etag)

    override suspend fun resolveHookCompatibility(packageName: String, versionCode: Long) =
        if (offline) offlineFailure else delegate.resolveHookCompatibility(packageName, versionCode)
}

class OfflineReportsRepository(
    private val delegate: ReportsRepository,
    private val serverMode: ServerModeRepository,
) : ReportsRepository {
    private val offline: Boolean get() = serverMode.mode.value.offline

    override suspend fun collectDiagnostics() = delegate.collectDiagnostics()

    override suspend fun submit(report: ProblemReport) =
        if (offline) offlineFailure else delegate.submit(report)
}
