package ua.polodarb.gmsflags.presentation.feature.flagdetails.importflags.external.mvi

import ua.polodarb.gmsflags.presentation.core.mvi.ViewSideEffect

sealed interface ExternalImportEffect : ViewSideEffect {
    data object NavigateBack : ExternalImportEffect
    data class OpenImport(val target: ExternalImportTarget) : ExternalImportEffect
}
