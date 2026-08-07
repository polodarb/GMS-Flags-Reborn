package ua.polodarb.gmsflags.presentation.feature.suggestions.details.mvi

import ua.polodarb.gmsflags.domain.apps.XposedScopeStatus
import ua.polodarb.gmsflags.presentation.core.error.UiError
import ua.polodarb.gmsflags.presentation.core.mvi.ViewEvent
import ua.polodarb.gmsflags.presentation.core.mvi.ViewSideEffect
import ua.polodarb.gmsflags.presentation.core.mvi.ViewState
import ua.polodarb.gmsflags.presentation.feature.suggestions.details.ui.model.RecommendationDetailsUiModel
import ua.polodarb.gmsflags.presentation.feature.suggestions.details.ui.model.RecommendationReportUiState
import ua.polodarb.gmsflags.presentation.feature.suggestions.ui.model.RecommendationApplicationUiStatus

data class RecommendationDetailsState(
    val content: RecommendationDetailsContent = RecommendationDetailsContent.Loading,
    val selectedVariantIndex: Int = 0,
    val infoExpanded: Boolean = false,
    val applying: Boolean = false,
    val applicationStatus: RecommendationApplicationUiStatus =
        RecommendationApplicationUiStatus.Checking,
    val xposedScopeStatus: XposedScopeStatus = XposedScopeStatus.Unknown,
    val scopeStatusChecking: Boolean = true,
    val scopeHelpVisible: Boolean = false,
    val appIsOfficial: Boolean = true,
    val applyAnywayConfirmVisible: Boolean = false,
    val report: RecommendationReportUiState? = null,
) : ViewState

sealed interface RecommendationDetailsContent {
    data object Loading : RecommendationDetailsContent
    data class Error(val error: UiError) : RecommendationDetailsContent
    data class Ready(val recommendation: RecommendationDetailsUiModel) : RecommendationDetailsContent
}

sealed interface RecommendationDetailsEvent : ViewEvent {
    data object BackClicked : RecommendationDetailsEvent
    data object Retry : RecommendationDetailsEvent
    data object ApplyClicked : RecommendationDetailsEvent
    data object DisableClicked : RecommendationDetailsEvent
    data object LaunchApplicationClicked : RecommendationDetailsEvent
    data object ExternalLinkClicked : RecommendationDetailsEvent
    data object InfoBlockClicked : RecommendationDetailsEvent
    data class VariantSelected(val index: Int) : RecommendationDetailsEvent
    data object ScopeHelpClicked : RecommendationDetailsEvent
    data object ScopeHelpDismissed : RecommendationDetailsEvent
    data object ScopeRefresh : RecommendationDetailsEvent
    data class FlagNameLongClicked(val name: String) : RecommendationDetailsEvent
    data object ApplicationStatusRefresh : RecommendationDetailsEvent
    data object ApplyAnywayClicked : RecommendationDetailsEvent
    data object ApplyAnywayDismissed : RecommendationDetailsEvent
    data object ApplyAnywayConfirmed : RecommendationDetailsEvent
    data object ReportProblemClicked : RecommendationDetailsEvent
    data object HeaderReportClicked : RecommendationDetailsEvent
    data class ReportMessageChanged(val message: String) : RecommendationDetailsEvent
    data class ReportContactChanged(val contact: String) : RecommendationDetailsEvent
    data object ReportDetailsToggled : RecommendationDetailsEvent
    data object ReportSendClicked : RecommendationDetailsEvent
    data object ReportDismissed : RecommendationDetailsEvent
}

sealed interface RecommendationDetailsEffect : ViewSideEffect {
    data object NavigateBack : RecommendationDetailsEffect
    data class OpenExternalLink(val url: String) : RecommendationDetailsEffect
    data object Applied : RecommendationDetailsEffect
    data object Disabled : RecommendationDetailsEffect
    data object DisabledUncertain : RecommendationDetailsEffect
    data class LaunchApplication(val androidPackageName: String) : RecommendationDetailsEffect
    data class CopyFlagName(val name: String) : RecommendationDetailsEffect
    data class ShowError(val error: UiError) : RecommendationDetailsEffect
}
