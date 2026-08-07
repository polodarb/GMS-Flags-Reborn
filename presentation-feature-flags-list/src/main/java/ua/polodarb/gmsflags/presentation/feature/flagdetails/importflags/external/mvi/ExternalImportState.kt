package ua.polodarb.gmsflags.presentation.feature.flagdetails.importflags.external.mvi

import androidx.annotation.StringRes
import ua.polodarb.gmsflags.presentation.core.mvi.ViewState

data class ExternalImportTarget(
    val androidPackageName: String,
    val applicationName: String,
    val phenotypePackageName: String,
    val supportedPhenotypePackageNames: List<String>,
)

data class ExternalImportState(
    val loading: Boolean = true,
    val displayName: String = "",
    val phenotypePackageName: String = "",
    val targets: List<ExternalImportTarget> = emptyList(),
    val unsupportedPackageName: String? = null,
    @param:StringRes val errorMessageRes: Int? = null,
) : ViewState
