package ua.polodarb.gmsflags.presentation.feature.apps.mvi

import ua.polodarb.gmsflags.presentation.core.mvi.ViewEvent

sealed class AppsEvent : ViewEvent {
    data object Retry : AppsEvent()
    data object Resumed : AppsEvent()
    data object Refresh : AppsEvent()
    data object SettingsClicked : AppsEvent()
    data class ApplicationClicked(
        val androidPackageName: String,
    ) : AppsEvent()
    data class ScopeHelpClicked(val androidPackageName: String) : AppsEvent()
    data object ScopeHelpDismissed : AppsEvent()
    data class PairipHelpClicked(val androidPackageName: String) : AppsEvent()
    data object PairipHelpDismissed : AppsEvent()
    data object ModuleScopeHelpClicked : AppsEvent()
    data object ModuleScopeHelpDismissed : AppsEvent()
    data object UnsupportedDismissed : AppsEvent()
    data class QueryChanged(val query: String) : AppsEvent()
    data class QueryDebounced(val query: String) : AppsEvent()
    data object SearchToggled : AppsEvent()
}
