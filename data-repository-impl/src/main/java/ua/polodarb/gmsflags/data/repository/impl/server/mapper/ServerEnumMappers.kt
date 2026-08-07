package ua.polodarb.gmsflags.data.repository.impl.server.mapper

import ua.polodarb.gmsflags.domain.server.content.DangerLevel
import ua.polodarb.gmsflags.domain.server.content.InfoBlockType
import ua.polodarb.gmsflags.domain.server.content.InfoDisplayMode
import ua.polodarb.gmsflags.domain.server.content.RecommendationStatus
import ua.polodarb.gmsflags.domain.server.content.RecommendationSupportStatus
import ua.polodarb.gmsflags.domain.server.content.RemoteFlagValueType
import ua.polodarb.gmsflags.domain.server.content.VersionConstraintType

internal fun String.toRemoteFlagValueType() = when (uppercase()) {
    "BOOL" -> RemoteFlagValueType.Boolean
    "INT" -> RemoteFlagValueType.Integer
    "LONG" -> RemoteFlagValueType.Long
    "FLOAT" -> RemoteFlagValueType.Float
    "DOUBLE" -> RemoteFlagValueType.Double
    "STRING" -> RemoteFlagValueType.String
    "BYTES" -> RemoteFlagValueType.Bytes
    else -> RemoteFlagValueType.Unknown
}

internal fun String.toInfoBlockType() = when (uppercase()) {
    "INFO" -> InfoBlockType.Info
    "WARNING" -> InfoBlockType.Warning
    "SUCCESS" -> InfoBlockType.Success
    "PROMO" -> InfoBlockType.Promo
    "DANGER" -> InfoBlockType.Danger
    else -> InfoBlockType.Unknown
}

internal fun String.toDangerLevel() = when (uppercase()) {
    "NONE" -> DangerLevel.None
    "CAUTION" -> DangerLevel.Caution
    "DANGEROUS" -> DangerLevel.Dangerous
    else -> DangerLevel.Unknown
}

internal fun String.toRecommendationStatus() = when (uppercase()) {
    "DRAFT" -> RecommendationStatus.Draft
    "PUBLISHED" -> RecommendationStatus.Published
    "ARCHIVED" -> RecommendationStatus.Archived
    else -> RecommendationStatus.Unknown
}

internal fun String.toRecommendationSupportStatus() = when (uppercase()) {
    "VERIFIED" -> RecommendationSupportStatus.Verified
    "PARTIAL" -> RecommendationSupportStatus.Partial
    "EXPERIMENTAL" -> RecommendationSupportStatus.Experimental
    else -> RecommendationSupportStatus.Unknown
}

internal fun String.toInfoDisplayMode() = when (uppercase()) {
    "INLINE_BADGE" -> InfoDisplayMode.InlineBadge
    "EXPANDABLE" -> InfoDisplayMode.Expandable
    else -> InfoDisplayMode.Unknown
}

internal fun String.toVersionConstraintType() = when (uppercase()) {
    "UNBOUNDED" -> VersionConstraintType.Unbounded
    "FROM" -> VersionConstraintType.From
    "UNTIL" -> VersionConstraintType.Until
    "RANGE" -> VersionConstraintType.Range
    else -> VersionConstraintType.Unknown
}
