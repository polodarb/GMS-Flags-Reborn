package ua.polodarb.gmsflags.presentation.feature.flagdetails.importflags.mvi

import androidx.annotation.StringRes
import ua.polodarb.gmsflags.presentation.core.mvi.ViewState
import ua.polodarb.gmsflags.presentation.feature.flagdetails.importflags.model.ImportedFlagBatch
import ua.polodarb.gmsflags.presentation.feature.flagdetails.importflags.model.ImportedFlagKey
import ua.polodarb.gmsflags.presentation.feature.flagdetails.importflags.model.resolvedPackageName

data class UnsupportedImportPackage(
    val packageName: String,
)

data class ImportFlagsState(
    val androidPackageName: String,
    val currentPhenotypePackageName: String,
    val supportedPhenotypePackageNames: Set<String>,
    val loading: Boolean = true,
    val applying: Boolean = false,
    val displayName: String = "",
    val batch: ImportedFlagBatch? = null,
    val selectedFlags: Set<ImportedFlagKey> = emptySet(),
    val unsupportedPackage: UnsupportedImportPackage? = null,
    val packageOverrideTarget: PackageOverrideTarget? = null,
    @param:StringRes val errorMessageRes: Int? = null,
) : ViewState {
    val selectedCount: Int get() = selectedFlags.size
    val allSelected: Boolean get() = batch?.flags?.all { it.key in selectedFlags } == true

    val effectivePackages: Map<ImportedFlagKey, String?>
        get() {
            val current = batch ?: return emptyMap()
            return current.flags.associate {
                it.key to it.resolvedPackageName(current.phenotypePackageName, supportedPhenotypePackageNames)
            }
        }

    val homogeneousPackageName: String? get() = effectivePackages.values.toSet().singleOrNull()
}
