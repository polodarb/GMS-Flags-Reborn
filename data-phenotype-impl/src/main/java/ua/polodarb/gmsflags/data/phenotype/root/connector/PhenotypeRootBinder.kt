package ua.polodarb.gmsflags.data.phenotype.root.connector

import ua.polodarb.gmsflags.data.phenotype.root.IPhenotypeRootService
import ua.polodarb.gmsflags.data.phenotype.root.parcel.HookDiagnosticSnapshotParcel
import ua.polodarb.gmsflags.data.phenotype.root.parcel.MicroHookEnvelopeParcel
import ua.polodarb.gmsflags.data.phenotype.root.parcel.PhenotypeFlagPageParcel
import ua.polodarb.gmsflags.data.phenotype.root.parcel.PhenotypeFlagParcel
import ua.polodarb.gmsflags.data.phenotype.root.parcel.PhenotypePackageBindingParcel
import ua.polodarb.gmsflags.data.phenotype.root.parcel.XposedScopeSnapshotParcel

internal class PhenotypeRootBinder(
    private val operations: PhenotypeRootOperations,
) : IPhenotypeRootService.Stub() {
    override fun readPhenotypePackages(): List<PhenotypePackageBindingParcel> =
        operations.readPhenotypePackages()

    override fun readFlagsPage(
        androidPackageName: String,
        phenotypePackageName: String,
        offset: Int,
        limit: Int,
    ): PhenotypeFlagPageParcel = operations.readFlagsPage(
        androidPackageName,
        phenotypePackageName,
        offset,
        limit,
    )

    override fun writeOverrides(
        androidPackageName: String,
        phenotypePackageName: String,
        overrides: List<PhenotypeFlagParcel>,
    ) = operations.writeOverrides(androidPackageName, phenotypePackageName, overrides)

    override fun writeMicroHooks(
        androidPackageName: String,
        hooks: List<MicroHookEnvelopeParcel>,
    ) = operations.writeMicroHooks(androidPackageName, hooks)

    override fun deleteMicroHooks(
        androidPackageName: String,
        recipeIds: LongArray,
    ) = operations.deleteMicroHooks(androidPackageName, recipeIds.toList())

    override fun deleteOverride(
        androidPackageName: String,
        phenotypePackageName: String,
        flagName: String,
    ) = operations.deleteOverride(androidPackageName, phenotypePackageName, flagName)

    override fun deleteOverrides(
        androidPackageName: String,
        phenotypePackageName: String,
        flagNames: List<String>,
    ) = operations.deleteOverrides(androidPackageName, phenotypePackageName, flagNames)

    override fun deletePackageOverrides(
        androidPackageName: String,
        phenotypePackageName: String,
    ) = operations.deletePackageOverrides(androidPackageName, phenotypePackageName)

    override fun readOverrideCount(androidPackageNames: List<String>): Int =
        operations.readOverrideCount(androidPackageNames)

    override fun readOverridesPaused(androidPackageNames: List<String>): Boolean =
        operations.readOverridesPaused(androidPackageNames)

    override fun setOverridesPaused(androidPackageNames: List<String>, paused: Boolean) =
        operations.setOverridesPaused(androidPackageNames, paused)

    override fun deleteAllOverrides(androidPackageNames: List<String>) =
        operations.deleteAllOverrides(androidPackageNames)

    override fun readHookDiagnostics(
        androidPackageNames: List<String>,
    ): List<HookDiagnosticSnapshotParcel> = operations.readHookDiagnostics(androidPackageNames)

    override fun readXposedScope(
        modulePackageName: String,
        userId: Int,
    ): XposedScopeSnapshotParcel = operations.readXposedScope(modulePackageName, userId)

    override fun readXposedLogs(androidPackageName: String): String =
        operations.readXposedLogs(androidPackageName)
}
