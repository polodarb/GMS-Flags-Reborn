package ua.polodarb.gmsflags.domain.hookstatus

import ua.polodarb.gmsflags.data.repository.apps.repository.SupportedApplicationsRepository
import ua.polodarb.gmsflags.data.repository.hookstatus.repository.HookStatusRepository

internal class GetHookStatusUseCase(
    private val applicationsRepository: SupportedApplicationsRepository,
    private val hookStatusRepository: HookStatusRepository,
) : GetHookStatus {
    override suspend fun invoke(): Result<HookStatusOverview> = runCatching {
        val applications = applicationsRepository.getApplications().getOrThrow()
        val diagnostics = hookStatusRepository.readDiagnostics(
            applications.map { it.androidPackageName }
        ).getOrThrow().associateBy(HookDiagnosticRecord::androidPackageName)

        HookStatusOverview(
            applications = applications.map { application ->
                val record = diagnostics[application.androidPackageName]
                    ?: HookDiagnosticRecord(application.androidPackageName, 0)
                val installedStrategies = record.session?.strategies.orEmpty()
                    .filter { it.state == HookStrategyState.Installed }
                HookApplicationStatus(
                    androidPackageName = application.androidPackageName,
                    applicationName = application.name,
                    versionName = application.versionName,
                    health = HookHealthResolver.resolve(record),
                    currentOverrideCount = record.currentOverrideCount,
                    loadedOverrideCount = record.session?.loadedOverrideCount ?: 0,
                    installedStrategyCount = installedStrategies.size,
                    appliedCount = installedStrategies.sumOf(HookStrategyDiagnostic::appliedCount),
                    consumedCount = installedStrategies.sumOf(HookStrategyDiagnostic::consumedCount),
                    lastCheckedAt = record.session?.updatedAt,
                    session = record.session,
                    compatibilityWarnings = record.compatibilityWarnings,
                )
            }.sortedWith(
                compareByDescending<HookApplicationStatus> { it.health == HookHealth.Working }
                    .thenBy(HookApplicationStatus::applicationName)
            ),
        )
    }
}
