package ua.polodarb.gmsflags.presentation.feature.suggestions.ui.model

import androidx.compose.runtime.Immutable
import ua.polodarb.gmsflags.presentation.feature.suggestions.details.ui.model.RecommendationInfoUiModel

@Immutable
data class RecommendationUiModel(
    val id: Long,
    val applicationId: Long?,
    val applicationName: String?,
    val applicationPackageName: String?,
    val applicationIconUrl: String?,
    val logoUrl: String?,
    val previewScreenshotUrl: String?,
    val title: String,
    val description: String?,
    val warning: String?,
    val infoBlock: RecommendationInfoUiModel? = null,
    val flagNames: List<String>,
    val supportStatus: RecommendationSupportUiModel,
    val applicationStatus: RecommendationApplicationUiStatus,
    val pinned: Boolean = false,
    val scopeExcluded: Boolean = false,
)

enum class RecommendationSupportUiModel {
    Verified,
    Partial,
    Experimental,
    Unknown,
}

enum class RecommendationApplicationUiStatus {
    Checking,
    Applied,
    PartiallyApplied,
    NotApplied,
    Unavailable,
}
