package ua.polodarb.gmsflags.data.repository.impl.apps.repository

import ua.polodarb.gmsflags.data.repository.apps.datasource.XposedScopeDataSource
import ua.polodarb.gmsflags.data.repository.apps.repository.SupportedApplicationsRepository
import ua.polodarb.gmsflags.data.repository.impl.apps.reader.InstalledApplicationReader
import ua.polodarb.gmsflags.data.repository.phenotype.PhenotypePackageReader
import ua.polodarb.gmsflags.domain.apps.FlagPackage
import ua.polodarb.gmsflags.domain.apps.FlagPackageCategory
import ua.polodarb.gmsflags.domain.apps.SupportedApplication
import ua.polodarb.gmsflags.domain.apps.SupportedApplicationsSnapshot
import ua.polodarb.gmsflags.domain.apps.XposedModuleStatus
import ua.polodarb.gmsflags.domain.apps.XposedScopeStatus
import ua.polodarb.xposed.info.XposedTargetRegistry

internal class SupportedApplicationsRepositoryImpl(
    private val packageReader: PhenotypePackageReader,
    private val installedApplicationReader: InstalledApplicationReader,
    private val xposedTargetRegistry: XposedTargetRegistry,
    private val xposedScopeDataSource: XposedScopeDataSource,
    private val modulePackageName: String,
    private val userId: Int,
) : SupportedApplicationsRepository {
    override suspend fun getApplications(): Result<List<SupportedApplication>> =
        getSnapshot().map(SupportedApplicationsSnapshot::applications)

    override suspend fun getSnapshot(): Result<SupportedApplicationsSnapshot> =
        packageReader.readPhenotypePackages().mapCatching { bindings ->
            val supportedPackages = xposedTargetRegistry.supportedApplicationPackageNames()
            val scope = xposedScopeDataSource.readScope(modulePackageName, userId).getOrNull()
            val flagPackageNamesByApplication = bindings.asSequence()
                .filter {
                    it.androidPackageName in supportedPackages &&
                        it.phenotypePackageName.isNotBlank()
                }
                .groupBy(
                    keySelector = { it.androidPackageName },
                    valueTransform = { it.phenotypePackageName },
                )
                .mapValues { (_, packages) -> packages.distinct().sorted() }

            val mendelPackages = xposedTargetRegistry.mendelApplicationPackageNames()
                .filter { it in supportedPackages }

            val installedApplications = installedApplicationReader.readInstalledApplications(
                flagPackageNamesByApplication.keys + mendelPackages,
            )

            val applications = installedApplications.values.map { metadata ->
                val discoveredPackageNames =
                    flagPackageNamesByApplication[metadata.packageName].orEmpty()
                val preferredFlagPackageName = xposedTargetRegistry
                    .preferredFlagPackageName(metadata.packageName)
                val knownFlagPackageNames = xposedTargetRegistry
                    .knownFlagPackageNames(metadata.packageName)
                val forcedPackageNames = (setOfNotNull(preferredFlagPackageName) + knownFlagPackageNames)
                    .filterNot { it in discoveredPackageNames }
                val packageNames = if (forcedPackageNames.isEmpty()) {
                    discoveredPackageNames
                } else {
                    (discoveredPackageNames + forcedPackageNames).sorted()
                }
                SupportedApplication(
                    androidPackageName = metadata.packageName,
                    flagPackages = packageNames.map { packageName ->
                        FlagPackage(
                            packageName = packageName,
                            category = packageName.categoryFor(
                                metadata.packageName,
                                preferredFlagPackageName,
                            ),
                        )
                    },
                    name = metadata.name,
                    versionName = metadata.versionName,
                    versionCode = metadata.versionCode,
                    lastUpdateTime = metadata.lastUpdateTime,
                    preferredFlagPackageName = preferredFlagPackageName,
                    xposedScopeStatus = when {
                        scope == null -> XposedScopeStatus.Unknown
                        !scope.moduleFound || !scope.moduleEnabled -> XposedScopeStatus.Excluded
                        metadata.packageName in scope.scopedPackageNames -> XposedScopeStatus.Included
                        else -> XposedScopeStatus.Excluded
                    },
                )
            }
            SupportedApplicationsSnapshot(
                applications = applications,
                moduleStatus = when {
                    scope == null -> XposedModuleStatus.Unknown
                    scope.moduleFound && scope.moduleEnabled -> XposedModuleStatus.Enabled
                    else -> XposedModuleStatus.Disabled
                },
            )
        }

    private fun String.categoryFor(
        androidPackageName: String,
        preferredFlagPackageName: String?,
    ): FlagPackageCategory {
        val isPrimary = if (preferredFlagPackageName != null) {
            this == preferredFlagPackageName
        } else {
            this == androidPackageName ||
                this == "$androidPackageName#$androidPackageName" ||
                contains("device#$androidPackageName") ||
                contains("user#$androidPackageName") ||
                contains("finsky", ignoreCase = true)
        }

        return if (isPrimary) FlagPackageCategory.Primary else FlagPackageCategory.Secondary
    }
}
