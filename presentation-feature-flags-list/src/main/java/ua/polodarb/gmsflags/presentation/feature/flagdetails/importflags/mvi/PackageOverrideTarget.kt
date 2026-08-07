package ua.polodarb.gmsflags.presentation.feature.flagdetails.importflags.mvi

import ua.polodarb.gmsflags.presentation.feature.flagdetails.importflags.model.ImportedFlagKey

sealed interface PackageOverrideTarget {
    data object WholeBatch : PackageOverrideTarget
    data class SingleFlag(val key: ImportedFlagKey) : PackageOverrideTarget
}
