package ua.polodarb.gmsflags.data.phenotype.root.connector

import ua.polodarb.gmsflags.data.phenotype.root.parcel.HookDiagnosticSnapshotParcel
import ua.polodarb.gmsflags.data.phenotype.root.parcel.MicroHookEnvelopeParcel
import ua.polodarb.gmsflags.data.phenotype.root.parcel.PhenotypeFlagPageParcel
import ua.polodarb.gmsflags.data.phenotype.root.parcel.PhenotypeFlagParcel
import ua.polodarb.gmsflags.data.phenotype.root.parcel.PhenotypePackageBindingParcel
import ua.polodarb.gmsflags.data.phenotype.root.parcel.XposedScopeSnapshotParcel

internal interface PhenotypeRootOperations {
    fun readPhenotypePackages(): List<PhenotypePackageBindingParcel>
    fun readFlagsPage(
        androidPackageName: String,
        phenotypePackageName: String,
        offset: Int,
        limit: Int,
    ): PhenotypeFlagPageParcel

    fun writeOverrides(
        androidPackageName: String,
        phenotypePackageName: String,
        overrides: List<PhenotypeFlagParcel>,
    )

    fun writeMicroHooks(androidPackageName: String, hooks: List<MicroHookEnvelopeParcel>)
    fun deleteMicroHooks(androidPackageName: String, recipeIds: List<Long>)

    fun deleteOverride(
        androidPackageName: String,
        phenotypePackageName: String,
        flagName: String,
    )

    fun deleteOverrides(
        androidPackageName: String,
        phenotypePackageName: String,
        flagNames: List<String>,
    )

    fun deletePackageOverrides(
        androidPackageName: String,
        phenotypePackageName: String,
    )

    fun readOverrideCount(androidPackageNames: List<String>): Int
    fun readOverridesPaused(androidPackageNames: List<String>): Boolean
    fun setOverridesPaused(androidPackageNames: List<String>, paused: Boolean)
    fun deleteAllOverrides(androidPackageNames: List<String>)

    fun readHookDiagnostics(
        androidPackageNames: List<String>,
    ): List<HookDiagnosticSnapshotParcel>

    fun readXposedScope(modulePackageName: String, userId: Int): XposedScopeSnapshotParcel

    fun readXposedLogs(androidPackageName: String): String
}
