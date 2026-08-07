package ua.polodarb.gmsflags.presentation.feature.flagdetails.importflags.mvi

import ua.polodarb.gmsflags.presentation.core.mvi.ViewEvent
import ua.polodarb.gmsflags.presentation.feature.flagdetails.importflags.model.ImportedFlagKey

sealed interface ImportFlagsEvent : ViewEvent {
    data object BackClicked : ImportFlagsEvent
    data object RetryClicked : ImportFlagsEvent
    data object ChooseAnotherFileClicked : ImportFlagsEvent
    data class DocumentSelected(val documentUri: String) : ImportFlagsEvent
    data class FlagSelectionChanged(
        val key: ImportedFlagKey,
        val selected: Boolean,
    ) : ImportFlagsEvent
    data object SelectAllClicked : ImportFlagsEvent
    data object ClearSelectionClicked : ImportFlagsEvent
    data object ApplyClicked : ImportFlagsEvent
    data class PackageOverrideRequested(val target: PackageOverrideTarget) : ImportFlagsEvent
    data object PackageOverrideDismissed : ImportFlagsEvent
    data class PackageOverrideSelected(val packageName: String) : ImportFlagsEvent
}
