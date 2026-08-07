package ua.polodarb.gmsflags.domain.server.content

import ua.polodarb.gmsflags.domain.apps.SupportedApplication
import ua.polodarb.gmsflags.domain.flags.FlagOverride
import ua.polodarb.gmsflags.domain.flags.FlagType

data class ServerRecommendationSummary(
    val id: Long,
    val status: RecommendationStatus,
    val supportStatus: RecommendationSupportStatus,
    val logoUrl: String?,
    val title: String,
    val description: String?,
    val warning: String?,
    val infoBlock: RecommendationInfoBlock? = null,
    val pinned: Boolean = false,
)

data class ServerRecommendationDetails(
    val summary: ServerRecommendationSummary,
    val applicationId: Long,
    val infoBlock: RecommendationInfoBlock?,
    val screenshots: List<String>,
    val externalLink: String?,
    val source: String?,
    val variants: List<RecommendationFlagVariant>,
)

data class RecommendationExperience(
    val details: ServerRecommendationDetails,
    val application: ServerApplication?,
    val installedApplication: SupportedApplication?,
)

data class ServerRecommendationFeedItem(
    val summary: ServerRecommendationSummary,
    val application: ServerApplication?,
    val previewScreenshotUrl: String?,
    val flagNames: List<String>,
    val applicationStatus: RecommendationApplicationStatus,
)

data class RecommendationInfoBlock(
    val message: String,
    val displayMode: InfoDisplayMode,
)

data class RecommendationFlagVariant(
    val id: Long?,
    val label: String?,
    val description: String? = null,
    val versionConstraint: VersionConstraint,
    val flags: List<RecommendedFlag>,
    val hooks: List<RecommendationVariantHook> = emptyList(),
)

data class SignedNeedleEnvelope(
    val mediaType: String,
    val payloadBase64: String,
    val payloadSha256: String,
    val signatureAlgorithm: String,
    val signatureBase64: String,
)

data class RecommendationVariantHook(
    val recipeId: Long,
    val required: Boolean,
    val codename: String? = null,
    val purpose: String? = null,
    val envelope: SignedNeedleEnvelope?,
)

data class RecommendedFlag(
    val name: String,
    val type: RemoteFlagValueType,
    val value: String,
    val title: String?,
    val description: String?,
    val dangerLevel: DangerLevel,
    val badges: List<ServerBadge>,
    val packageName: String? = null,
)

enum class RecommendationStatus { Draft, Published, Archived, Unknown }

enum class RecommendationSupportStatus { Verified, Partial, Experimental, Unknown }

enum class InfoDisplayMode { InlineBadge, Expandable, Unknown }

enum class RemoteFlagValueType { Boolean, Integer, Long, Float, Double, String, Bytes, Unknown }

fun RecommendedFlag.toFlagOverrideOrNull(): FlagOverride? {
    val localType = when (type) {
        RemoteFlagValueType.Boolean -> FlagType.Boolean
        RemoteFlagValueType.Integer,
        RemoteFlagValueType.Long,
        -> FlagType.Integer
        RemoteFlagValueType.Float,
        RemoteFlagValueType.Double,
        -> FlagType.Float
        RemoteFlagValueType.String -> FlagType.String
        RemoteFlagValueType.Bytes,
        RemoteFlagValueType.Unknown,
        -> null
    } ?: return null
    return FlagOverride(name = name, type = localType, value = value)
}

data class VersionConstraint(
    val type: VersionConstraintType,
    val minimumVersionCode: Long?,
    val maximumVersionCode: Long?,
)

enum class VersionConstraintType { Unbounded, From, Until, Range, Unknown }

fun VersionConstraint.matches(versionCode: Long): Boolean = when (type) {
    VersionConstraintType.Unbounded -> true
    VersionConstraintType.From -> minimumVersionCode?.let(versionCode::compareTo)?.let { it >= 0 }
        ?: false
    VersionConstraintType.Until -> maximumVersionCode?.let(versionCode::compareTo)?.let { it <= 0 }
        ?: false
    VersionConstraintType.Range -> {
        val minimum = minimumVersionCode ?: return false
        val maximum = maximumVersionCode ?: return false
        versionCode in minimum..maximum
    }
    VersionConstraintType.Unknown -> false
}
