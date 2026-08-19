package ua.polodarb.gmsflags.presentation.feature.flagdetails.mvi

import ua.polodarb.gmsflags.domain.flags.FlagType
import ua.polodarb.gmsflags.presentation.core.mvi.ViewEvent

sealed interface FlagDetailsEvent : ViewEvent {
    data object Retry : FlagDetailsEvent
    data object BackClicked : FlagDetailsEvent
    data object PackageNameLongClicked : FlagDetailsEvent
    data object CopySelectedFlagNamesClicked : FlagDetailsEvent
    data class PackageSelected(val packageName: String) : FlagDetailsEvent
    data class TypeSelected(val type: FlagType) : FlagDetailsEvent
    data object SearchToggled : FlagDetailsEvent
    data class QueryChanged(val query: String) : FlagDetailsEvent
    data class QueryDebounced(val query: String) : FlagDetailsEvent
    data object FiltersToggled : FlagDetailsEvent
    data class FilterSelected(val filter: FlagFilter) : FlagDetailsEvent
    data class FlagLongClicked(val name: String) : FlagDetailsEvent
    data class FlagClicked(val name: String) : FlagDetailsEvent
    data class DragSelectionChanged(val selectedFlags: Set<SelectedFlag>) : FlagDetailsEvent
    data class BooleanChanged(val name: String, val enabled: Boolean) : FlagDetailsEvent
    data class BooleanOverrideCleared(val name: String) : FlagDetailsEvent
    data class InlineEditorValueChanged(val value: String) : FlagDetailsEvent
    data object InlineEditorSaved : FlagDetailsEvent
    data object InlineEditorReset : FlagDetailsEvent
    data object InlineEditorDismissed : FlagDetailsEvent
    data object ExitSelection : FlagDetailsEvent
    data object SelectAll : FlagDetailsEvent
    data class SetSelectedBooleans(val enabled: Boolean) : FlagDetailsEvent
    data object ResetSelectedToDefault : FlagDetailsEvent
    data object AddFlagClicked : FlagDetailsEvent
    data object AddMultipleClicked : FlagDetailsEvent
    data object ImportFlagsClicked : FlagDetailsEvent
    data object DeleteAllOverridesClicked : FlagDetailsEvent
    data object DeleteAllOverridesConfirmed : FlagDetailsEvent
    data object LaunchApplicationClicked : FlagDetailsEvent
    data object OpenAppSettingsClicked : FlagDetailsEvent
    data class EditorNameChanged(val value: String) : FlagDetailsEvent
    data class EditorTypeChanged(val value: FlagType) : FlagDetailsEvent
    data class EditorValueChanged(val value: String) : FlagDetailsEvent
    data object EditorSaved : FlagDetailsEvent
    data object EditorReset : FlagDetailsEvent
    data object ExportClicked : FlagDetailsEvent
    data class ExportFileNameChanged(val value: String) : FlagDetailsEvent
    data object ExportConfirmed : FlagDetailsEvent
    data object ShareToCommunityClicked : FlagDetailsEvent
    data class AddCommunityFlag(val flagName: String, val valueType: String, val value: String) : FlagDetailsEvent
    data class RemoveCommunityFlag(val index: Int) : FlagDetailsEvent
    data class SubmitCommunityPackage(val title: String, val description: String, val packageName: String) : FlagDetailsEvent
    data object ReportClicked : FlagDetailsEvent
    data class ReportDescriptionChanged(val value: String) : FlagDetailsEvent
    data object ReportConfirmed : FlagDetailsEvent

    data object DialogDismissed : FlagDetailsEvent
    data object ScopeRefresh : FlagDetailsEvent
    data object ScopeHelpClicked : FlagDetailsEvent
    data object ScopeHelpDismissed : FlagDetailsEvent
    data object PairipHelpClicked : FlagDetailsEvent
    data object PairipHelpDismissed : FlagDetailsEvent
    data object BooleanControlHelpClicked : FlagDetailsEvent
    data object BooleanControlHelpDismissed : FlagDetailsEvent
    data object RemoteContentRetry : FlagDetailsEvent
    data class RecommendationClicked(val id: Long) : FlagDetailsEvent
}
