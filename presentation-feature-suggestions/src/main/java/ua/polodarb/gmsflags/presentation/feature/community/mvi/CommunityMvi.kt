package ua.polodarb.gmsflags.presentation.feature.community.mvi

import ua.polodarb.gmsflags.domain.community.CommunityFlagItem
import ua.polodarb.gmsflags.domain.community.CommunityPackage


sealed interface CommunityEvent {
    data class SearchQueryChanged(val query: String) : CommunityEvent
    data class PackageSelected(val packageItem: CommunityPackage) : CommunityEvent
    data object DismissDetails : CommunityEvent
    data object DismissDisclaimer : CommunityEvent
    data class ReportClicked(val packageId: Long) : CommunityEvent
    data class SubmitReport(val reason: String) : CommunityEvent
    data class OpenSubmitDialog(val defaultPackageName: String = "", val initialFlags: List<CommunityFlagItem> = emptyList()) : CommunityEvent
    data object CloseSubmitDialog : CommunityEvent
    data class AddFlagToSubmit(val flagName: String, val valueType: String, val value: String) : CommunityEvent
    data class RemoveFlagFromSubmit(val index: Int) : CommunityEvent
    data class SubmitPackage(val title: String, val description: String, val packageName: String) : CommunityEvent
    data class InstallFlags(val packageItem: CommunityPackage) : CommunityEvent
    data object Refresh : CommunityEvent
}

sealed interface CommunityEffect {
    data class ShowSnackbar(val message: String) : CommunityEffect
    data object FlagsInstalled : CommunityEffect
}
