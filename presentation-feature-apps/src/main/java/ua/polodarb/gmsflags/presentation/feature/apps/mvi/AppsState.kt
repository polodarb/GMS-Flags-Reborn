package ua.polodarb.gmsflags.presentation.feature.apps.mvi

import ua.polodarb.gmsflags.presentation.core.mvi.ViewState
import ua.polodarb.gmsflags.presentation.core.error.UiError
import ua.polodarb.gmsflags.presentation.feature.apps.ui.model.ApplicationUiModel
import ua.polodarb.gmsflags.domain.apps.XposedModuleStatus

data class AppsState(
    val loading: Boolean = true,
    val refreshing: Boolean = false,
    val applications: List<ApplicationUiModel> = emptyList(),
    val error: UiError? = null,
    val moduleStatus: XposedModuleStatus = XposedModuleStatus.Unknown,
    val moduleScopeHelpVisible: Boolean = false,
    val scopeHelpApplication: ApplicationUiModel? = null,
    val pairipHelpApplication: ApplicationUiModel? = null,
    val unsupportedApplication: ApplicationUiModel? = null,
    val query: String = "",
    val effectiveQuery: String = "",
    val searchVisible: Boolean = false,
) : ViewState

internal fun AppsState.visibleApplications(): List<ApplicationUiModel> {
    val terms = effectiveQuery.trim()
        .split(Regex("\\s+"))
        .filter(String::isNotBlank)
    val matched = if (terms.isEmpty()) {
        applications
    } else {
        applications.filter { application ->
            terms.all { term ->
                application.name.contains(term, ignoreCase = true) ||
                    application.androidPackageName.contains(term, ignoreCase = true)
            }
        }
    }
    return matched.sortedBy { it.unsupported }
}
