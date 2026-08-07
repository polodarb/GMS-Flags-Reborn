package ua.polodarb.gmsflags.data.phenotype.root.connector

import ua.polodarb.gmsflags.data.phenotype.root.diagnostic.LsposedScopeReader
import ua.polodarb.gmsflags.data.phenotype.root.diagnostic.XposedLogsReader
import ua.polodarb.gmsflags.data.phenotype.root.flags.OverrideRuntimeController
import ua.polodarb.gmsflags.data.phenotype.root.flags.PhenotypeFlagStore
import ua.polodarb.gmsflags.data.phenotype.root.hooks.HookDiagnosticsReader
import ua.polodarb.gmsflags.data.phenotype.root.hooks.MicroHookStore
import ua.polodarb.gmsflags.data.phenotype.root.parcel.HookDiagnosticSnapshotParcel
import ua.polodarb.gmsflags.data.phenotype.root.parcel.MicroHookEnvelopeParcel
import ua.polodarb.gmsflags.data.phenotype.root.parcel.PhenotypeFlagPageParcel
import ua.polodarb.gmsflags.data.phenotype.root.parcel.PhenotypeFlagParcel
import ua.polodarb.gmsflags.data.phenotype.root.parcel.PhenotypePackageBindingParcel
import ua.polodarb.gmsflags.data.phenotype.root.parcel.XposedScopeSnapshotParcel
import ua.polodarb.gmsflags.data.phenotype.sqlite.PhenotypeDatabaseReader

internal class DefaultPhenotypeRootOperations(
    private val packageReader: PhenotypeDatabaseReader,
    private val flagStore: PhenotypeFlagStore,
    private val microHookStore: MicroHookStore,
    private val hookDiagnosticsReader: HookDiagnosticsReader,
    private val xposedScopeReader: LsposedScopeReader,
    private val xposedLogsReader: XposedLogsReader,
    private val overrideRuntimeController: OverrideRuntimeController,
) : PhenotypeRootOperations {
    override fun readPhenotypePackages(): List<PhenotypePackageBindingParcel> =
        packageReader.readPhenotypePackages().map { binding ->
            PhenotypePackageBindingParcel(
                phenotypePackageName = binding.phenotypePackageName,
                androidPackageName = binding.androidPackageName,
            )
        }

    override fun readFlagsPage(
        androidPackageName: String,
        phenotypePackageName: String,
        offset: Int,
        limit: Int,
    ): PhenotypeFlagPageParcel = flagStore.readFlagsPage(
        androidPackageName,
        phenotypePackageName,
        offset,
        limit,
    )

    override fun writeOverrides(
        androidPackageName: String,
        phenotypePackageName: String,
        overrides: List<PhenotypeFlagParcel>,
    ) = flagStore.writeOverrides(androidPackageName, phenotypePackageName, overrides)

    override fun writeMicroHooks(androidPackageName: String, hooks: List<MicroHookEnvelopeParcel>) =
        microHookStore.writeMicroHooks(androidPackageName, hooks)

    override fun deleteMicroHooks(androidPackageName: String, recipeIds: List<Long>) =
        microHookStore.deleteMicroHooks(androidPackageName, recipeIds)

    override fun deleteOverride(
        androidPackageName: String,
        phenotypePackageName: String,
        flagName: String,
    ) = flagStore.deleteOverride(androidPackageName, phenotypePackageName, flagName)

    override fun deleteOverrides(
        androidPackageName: String,
        phenotypePackageName: String,
        flagNames: List<String>,
    ) = flagStore.deleteOverrides(androidPackageName, phenotypePackageName, flagNames)

    override fun deletePackageOverrides(
        androidPackageName: String,
        phenotypePackageName: String,
    ) = flagStore.deletePackageOverrides(androidPackageName, phenotypePackageName)

    override fun readOverrideCount(androidPackageNames: List<String>): Int =
        overrideRuntimeController.readOverrideCount(androidPackageNames)

    override fun readOverridesPaused(androidPackageNames: List<String>): Boolean =
        overrideRuntimeController.readPaused(androidPackageNames)

    override fun setOverridesPaused(androidPackageNames: List<String>, paused: Boolean) =
        overrideRuntimeController.setPaused(androidPackageNames, paused)

    override fun deleteAllOverrides(androidPackageNames: List<String>) =
        overrideRuntimeController.deleteAll(androidPackageNames)

    override fun readHookDiagnostics(
        androidPackageNames: List<String>,
    ): List<HookDiagnosticSnapshotParcel> = hookDiagnosticsReader.read(androidPackageNames)

    override fun readXposedScope(
        modulePackageName: String,
        userId: Int,
    ): XposedScopeSnapshotParcel = xposedScopeReader.read(modulePackageName, userId)

    override fun readXposedLogs(androidPackageName: String): String =
        xposedLogsReader.read(androidPackageName)
}
