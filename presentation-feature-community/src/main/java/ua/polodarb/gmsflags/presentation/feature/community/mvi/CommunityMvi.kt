package ua.polodarb.gmsflags.presentation.feature.community.mvi

import ua.polodarb.gmsflags.domain.community.CommunityFlagItem
import ua.polodarb.gmsflags.domain.community.CommunityPackage
import ua.polodarb.gmsflags.presentation.core.mvi.ViewEvent
import ua.polodarb.gmsflags.presentation.core.mvi.ViewSideEffect

sealed interface CommunityEvent : ViewEvent {
    data object Refresh : CommunityEvent
    data object SearchToggled : CommunityEvent
    data class QueryChanged(val query: String) : CommunityEvent
    data class PackageSelected(val packageItem: CommunityPackage) : CommunityEvent
    data object PackageSheetDismissed : CommunityEvent
    data object DisclaimerDismissed : CommunityEvent
    data object OpenSubmitDialog : CommunityEvent
    data object SubmitDialogDismissed : CommunityEvent
    data class InstallFlags(val packageItem: CommunityPackage) : CommunityEvent
    data class ReportPackage(val packageId: Long, val reason: String) : CommunityEvent
    data class SubmitPackage(
        val title: String,
        val description: String,
        val packageName: String,
        val flags: List<CommunityFlagItem>,
    ) : CommunityEvent
    data object SettingsClicked : CommunityEvent
}

sealed interface CommunityEffect : ViewSideEffect {
    data class ShowSnackbar(val message: String) : CommunityEffect
    data object FlagsInstalled : CommunityEffect
    data object OpenSettings : CommunityEffect
}
