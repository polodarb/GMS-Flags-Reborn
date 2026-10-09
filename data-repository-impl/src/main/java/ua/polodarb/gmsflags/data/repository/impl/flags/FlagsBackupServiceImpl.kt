package ua.polodarb.gmsflags.data.repository.impl.flags

import kotlinx.coroutines.CancellationException
import ua.polodarb.gmsflags.data.repository.flags.FlagOverridesChangeBus
import ua.polodarb.gmsflags.data.repository.impl.apps.reader.InstalledApplicationReader
import ua.polodarb.gmsflags.data.repository.phenotype.PhenotypeFlagsDataSource
import ua.polodarb.gmsflags.data.repository.phenotype.PhenotypeOverrideRecord
import ua.polodarb.gmsflags.data.repository.phenotype.SavedOverridesDataSource
import ua.polodarb.gmsflags.data.repository.settings.datasource.OverrideControlDataSource
import ua.polodarb.gmsflags.data.repository.settings.repository.OverrideControlRepository
import ua.polodarb.gmsflags.domain.backup.BackupPackage
import ua.polodarb.gmsflags.domain.backup.BackupRestoreResult
import ua.polodarb.gmsflags.domain.backup.FlagsBackup
import ua.polodarb.gmsflags.domain.backup.FlagsBackupService
import ua.polodarb.gmsflags.domain.flags.FlagOverride
import ua.polodarb.gmsflags.domain.flags.FlagOverridesChange
import ua.polodarb.gmsflags.domain.flags.FlagType
import ua.polodarb.xposed.info.XposedTargetRegistry

internal class FlagsBackupServiceImpl(
    private val savedOverrides: SavedOverridesDataSource,
    private val flagsDataSource: PhenotypeFlagsDataSource,
    private val installedApps: InstalledApplicationReader,
    private val targets: XposedTargetRegistry,
    private val restarter: TargetProcessRestarter,
    private val changes: FlagOverridesChangeBus,
    private val controlDataSource: OverrideControlDataSource,
    private val control: OverrideControlRepository,
) : FlagsBackupService {
    override suspend fun availableApps(): Set<String> =
        installedApps.readInstalledApplications(targets.supportedApplicationPackageNames()).keys

    override suspend fun export(): FlagsBackup = FlagsBackup(
        buildList {
            availableApps().sorted().forEach { app ->
                savedOverrides
                    .readAll(app)
                    .getOrThrow()
                    .groupBy { it.packageName }
                    .forEach { (pkg, records) ->
                        add(
                            BackupPackage(
                                app,
                                pkg,
                                records.map {
                                    FlagOverride(
                                        it.flag.name,
                                        FlagType.fromStorageId(it.flag.type)
                                            ?: error("Unsupported saved flag type"),
                                        it.flag.value,
                                    )
                                },
                            )
                        )
                    }
            }
        }
    )

    override suspend fun restore(backup: FlagsBackup): BackupRestoreResult {
        val available = availableApps()
        val skipped = backup.packages.filter { it.androidPackageName !in available }
        val failed = mutableListOf<BackupPackage>()
        val restartFailures = mutableListOf<String>()
        var restored = 0
        backup.packages
            .filter { it.androidPackageName in available }
            .groupBy { it.androidPackageName }
            .forEach { (app, packages) ->
                val pauseResult = controlDataSource.setPaused(listOf(app), control.state.value.paused)
                if (pauseResult.isFailure) {
                    failed += packages
                    return@forEach
                }
                try {
                    packages.forEach { pkg ->
                        val overrides = pkg.flags.map {
                            PhenotypeOverrideRecord(it.name, it.type.storageId, it.value)
                        }
                        val result = try {
                            flagsDataSource.writeOverrides(
                                app,
                                pkg.phenotypePackageName,
                                overrides,
                            )
                        } catch (e: CancellationException) {
                            throw e
                        } catch (e: Exception) {
                            Result.failure(e)
                        }
                        if (result.isSuccess) {
                            restored += pkg.flags.size
                        } else {
                            failed += pkg
                        }
                        changes.notifyChanged(
                            FlagOverridesChange.ReloadRequired(app, pkg.phenotypePackageName)
                        )
                    }
                } finally {
                    if (restarter.restart(app).isFailure) {
                        restartFailures += app
                    }
                }
            }
        val refreshFailed = try {
            control.refresh().isFailure
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            true
        }
        return BackupRestoreResult(restored, skipped, failed, restartFailures, refreshFailed)
    }
}
