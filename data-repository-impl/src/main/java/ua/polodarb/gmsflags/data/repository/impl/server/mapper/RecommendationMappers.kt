package ua.polodarb.gmsflags.data.repository.impl.server.mapper

import ua.polodarb.gmsflags.data.network.publicapi.model.RecommendationDetailNetModel
import ua.polodarb.gmsflags.data.network.publicapi.model.RecommendationFlagNetModel
import ua.polodarb.gmsflags.data.network.publicapi.model.RecommendationHookNetModel
import ua.polodarb.gmsflags.data.network.publicapi.model.RecommendationSummaryNetModel
import ua.polodarb.gmsflags.data.network.publicapi.model.VersionConstraintNetModel
import ua.polodarb.gmsflags.domain.server.content.DangerLevel
import ua.polodarb.gmsflags.domain.server.content.RecommendationFlagVariant
import ua.polodarb.gmsflags.domain.server.content.RecommendationInfoBlock
import ua.polodarb.gmsflags.domain.server.content.RecommendationVariantHook
import ua.polodarb.gmsflags.domain.server.content.RecommendedFlag
import ua.polodarb.gmsflags.domain.server.content.ServerRecommendationDetails
import ua.polodarb.gmsflags.domain.server.content.ServerRecommendationSummary
import ua.polodarb.gmsflags.domain.server.content.SignedNeedleEnvelope
import ua.polodarb.gmsflags.domain.server.content.VersionConstraint

internal fun RecommendationSummaryNetModel.toDomain() = ServerRecommendationSummary(
    id = id,
    status = status.toRecommendationStatus(),
    supportStatus = supportStatus.toRecommendationSupportStatus(),
    logoUrl = logoUrl,
    title = title,
    description = description,
    warning = warningBlock?.message,
    infoBlock = infoBlock?.let { RecommendationInfoBlock(message = it.message, displayMode = it.displayMode.toInfoDisplayMode()) },
    pinned = pinned,
)

internal fun RecommendationDetailNetModel.toDomain() = ServerRecommendationDetails(
    summary = ServerRecommendationSummary(
        id = id,
        status = status.toRecommendationStatus(),
        supportStatus = supportStatus.toRecommendationSupportStatus(),
        logoUrl = logoUrl,
        title = title,
        description = description,
        warning = warningBlock?.message,
        infoBlock = null,
    ),
    applicationId = appId,
    infoBlock = infoBlock?.let {
        RecommendationInfoBlock(
            message = it.message,
            displayMode = it.displayMode.toInfoDisplayMode(),
        )
    },
    screenshots = screenshots,
    externalLink = externalLink,
    source = source,
    variants = variants.map { variant ->
        RecommendationFlagVariant(
            id = variant.id,
            label = variant.label,
            description = variant.description,
            versionConstraint = variant.versionConstraint.toDomain(),
            flags = variant.flags.map { it.toDomain() },
            hooks = variant.hooks.map { it.toDomain() },
        )
    },
)

private fun RecommendationHookNetModel.toDomain() = RecommendationVariantHook(
    recipeId = recipeId,
    required = required,
    codename = codename,
    purpose = purpose,
    envelope = signedEnvelope?.let {
        SignedNeedleEnvelope(it.mediaType, it.payloadBase64, it.payloadSha256, it.signatureAlgorithm, it.signatureBase64)
    },
)

private fun RecommendationFlagNetModel.toDomain() = RecommendedFlag(
    name = flagName,
    type = valueType.toRemoteFlagValueType(),
    value = value,
    title = title,
    description = description,
    dangerLevel = dangerLevel?.toDangerLevel() ?: DangerLevel.None,
    badges = badges.map { it.toDomain() },
    packageName = packageName,
)

internal fun VersionConstraintNetModel.toDomain() = VersionConstraint(
    type = type.toVersionConstraintType(),
    minimumVersionCode = min,
    maximumVersionCode = max,
)
