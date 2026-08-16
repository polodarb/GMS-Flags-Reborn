package ua.polodarb.gmsflags.presentation.feature.suggestions.ui.model

import ua.polodarb.gmsflags.domain.server.content.RecommendationSupportStatus
import ua.polodarb.gmsflags.domain.server.content.ServerRecommendationSummary
import ua.polodarb.gmsflags.domain.server.content.ServerInfoBlock
import ua.polodarb.gmsflags.domain.server.content.InfoBlockType
import ua.polodarb.gmsflags.domain.server.content.ServerRecommendationFeedItem
import ua.polodarb.gmsflags.domain.server.content.RecommendationApplicationStatus
import ua.polodarb.gmsflags.presentation.feature.suggestions.details.ui.model.RecommendationInfoUiModel

internal fun ServerRecommendationSummary.toUiModel(
    applicationId: Long? = null,
    applicationName: String? = null,
    applicationPackageName: String? = null,
    applicationIconUrl: String? = null,
    previewScreenshotUrl: String? = null,
    flagNames: List<String> = emptyList(),
    applicationStatus: RecommendationApplicationUiStatus =
        RecommendationApplicationUiStatus.Unavailable,
    scopeExcluded: Boolean = false,
) = RecommendationUiModel(
    id = id,
    applicationId = applicationId,
    applicationName = applicationName?.trim()?.takeIf(String::isNotEmpty),
    applicationPackageName = applicationPackageName?.trim()?.takeIf(String::isNotEmpty),
    applicationIconUrl = applicationIconUrl?.trim()?.takeIf(String::isNotEmpty),
    logoUrl = logoUrl?.trim()?.takeIf(String::isNotEmpty),
    previewScreenshotUrl = previewScreenshotUrl?.trim()?.takeIf(String::isNotEmpty),
    title = title.trim(),
    description = description?.trim()?.takeIf(String::isNotEmpty),
    warning = warning?.trim()?.takeIf(String::isNotEmpty),
    infoBlock = infoBlock?.let { RecommendationInfoUiModel(message = it.message.trim(), displayMode = it.displayMode) },
    flagNames = flagNames.map(String::trim).filter(String::isNotEmpty).distinct(),
    supportStatus = supportStatus.toUiModel(),
    applicationStatus = applicationStatus,
    pinned = pinned,
    scopeExcluded = scopeExcluded,
)

internal fun ServerRecommendationFeedItem.toUiModel(scopeExcluded: Boolean = false) =
    summary.toUiModel(
        applicationId = application?.id,
        applicationName = application?.displayName,
        applicationPackageName = application?.packageName,
        applicationIconUrl = application?.iconUrl,
        previewScreenshotUrl = previewScreenshotUrl,
        flagNames = flagNames,
        applicationStatus = applicationStatus.toUiModel(),
        scopeExcluded = scopeExcluded,
    )

internal fun RecommendationApplicationStatus.toUiModel() = when (this) {
    RecommendationApplicationStatus.Applied -> RecommendationApplicationUiStatus.Applied
    RecommendationApplicationStatus.PartiallyApplied ->
        RecommendationApplicationUiStatus.PartiallyApplied
    RecommendationApplicationStatus.NotApplied -> RecommendationApplicationUiStatus.NotApplied
    RecommendationApplicationStatus.Unavailable -> RecommendationApplicationUiStatus.Unavailable
    RecommendationApplicationStatus.ClientUpdateRequired -> RecommendationApplicationUiStatus.Unavailable
}

internal fun RecommendationSupportStatus.toUiModel() = when (this) {
    RecommendationSupportStatus.Verified -> RecommendationSupportUiModel.Verified
    RecommendationSupportStatus.Partial -> RecommendationSupportUiModel.Partial
    RecommendationSupportStatus.Experimental -> RecommendationSupportUiModel.Experimental
    RecommendationSupportStatus.Unknown -> RecommendationSupportUiModel.Unknown
}

internal fun ServerInfoBlock.toUiModel() = HomeInfoBlockUiModel(
    id = id,
    message = message.trim(),
    type = when (type) {
        InfoBlockType.Info -> HomeInfoBlockTypeUiModel.Info
        InfoBlockType.Warning -> HomeInfoBlockTypeUiModel.Warning
        InfoBlockType.Success -> HomeInfoBlockTypeUiModel.Success
        InfoBlockType.Promo -> HomeInfoBlockTypeUiModel.Promo
        InfoBlockType.Danger -> HomeInfoBlockTypeUiModel.Danger
        InfoBlockType.Unknown -> HomeInfoBlockTypeUiModel.Unknown
    },
    externalLink = externalLink?.trim()?.takeIf(String::isNotEmpty),
)
