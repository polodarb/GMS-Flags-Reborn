package ua.polodarb.gmsflags.domain.apps

import ua.polodarb.gmsflags.data.repository.apps.repository.SupportedApplicationsRepository
import ua.polodarb.gmsflags.data.repository.server.PublicContentRepository
import ua.polodarb.gmsflags.domain.hookstatus.GetPairipIncompatiblePackages

class GetSupportedApplicationsSnapshotUseCase(
    private val repository: SupportedApplicationsRepository,
    private val publicContentRepository: PublicContentRepository,
    private val getPairipIncompatiblePackages: GetPairipIncompatiblePackages,
) : GetSupportedApplicationsSnapshot {
    override suspend fun invoke(): Result<SupportedApplicationsSnapshot> =
        repository.getSnapshot().map { snapshot ->
            val disabledByPackage = publicContentRepository.getApplications()
                .getOrDefault(emptyList())
                .filter { it.disabledFromVersion != null }
                .associate { it.packageName to it.disabledFromVersion }
            val pairipPackages = getPairipIncompatiblePackages(
                snapshot.applications.map { it.androidPackageName },
            ).getOrDefault(emptySet())
            val annotated = snapshot.applications.map { application ->
                application.copy(
                    disabledFromVersion = disabledByPackage[application.androidPackageName],
                    pairipIncompatible = application.androidPackageName in pairipPackages,
                )
            }
            snapshot.copy(applications = annotated.normalized())
        }
}

internal fun List<SupportedApplication>.normalized(): List<SupportedApplication> =
    filter { application ->
        application.androidPackageName.isNotBlank() && application.flagPackages.isNotEmpty()
    }
        .distinctBy(SupportedApplication::androidPackageName)
        .sortedWith(
            compareBy<SupportedApplication, String>(String.CASE_INSENSITIVE_ORDER) { it.name }
                .thenBy(SupportedApplication::androidPackageName)
        )
