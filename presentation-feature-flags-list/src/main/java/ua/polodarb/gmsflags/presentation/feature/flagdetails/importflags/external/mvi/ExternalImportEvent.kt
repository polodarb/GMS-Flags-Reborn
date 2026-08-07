package ua.polodarb.gmsflags.presentation.feature.flagdetails.importflags.external.mvi

import ua.polodarb.gmsflags.presentation.core.mvi.ViewEvent

sealed interface ExternalImportEvent : ViewEvent {
    data object BackClicked : ExternalImportEvent
    data object RetryClicked : ExternalImportEvent
    data class TargetSelected(val androidPackageName: String) : ExternalImportEvent
}
