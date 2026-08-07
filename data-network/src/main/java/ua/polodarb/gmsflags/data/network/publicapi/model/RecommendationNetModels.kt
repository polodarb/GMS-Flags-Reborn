package ua.polodarb.gmsflags.data.network.publicapi.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WarningBlockNetModel(
    val message: String,
    @SerialName("show_on_card") val showOnCard: Boolean = true,
)

@Serializable
data class RecommendationInfoBlockNetModel(
    val message: String,
    @SerialName("display_mode") val displayMode: String,
    @SerialName("show_on_card") val showOnCard: Boolean = false,
)

@Serializable
data class RecommendationSummaryNetModel(
    val id: Long,
    val status: String,
    @SerialName("support_status") val supportStatus: String,
    @SerialName("logo_url") val logoUrl: String? = null,
    val title: String,
    val description: String? = null,
    @SerialName("warning_block") val warningBlock: WarningBlockNetModel? = null,
    @SerialName("info_block") val infoBlock: RecommendationInfoBlockNetModel? = null,
    val pinned: Boolean = false,
)

@Serializable
data class RecommendationDetailNetModel(
    val id: Long,
    @SerialName("app_id") val appId: Long,
    val status: String,
    @SerialName("support_status") val supportStatus: String,
    @SerialName("logo_url") val logoUrl: String? = null,
    val title: String,
    val description: String? = null,
    @SerialName("warning_block") val warningBlock: WarningBlockNetModel? = null,
    @SerialName("info_block") val infoBlock: RecommendationInfoBlockNetModel? = null,
    val screenshots: List<String> = emptyList(),
    @SerialName("external_link") val externalLink: String? = null,
    val source: String? = null,
    val variants: List<FlagVariantNetModel> = emptyList(),
)

@Serializable
data class FlagVariantNetModel(
    val id: Long? = null,
    val label: String? = null,
    val description: String? = null,
    @SerialName("version_constraint") val versionConstraint: VersionConstraintNetModel,
    val flags: List<RecommendationFlagNetModel> = emptyList(),
    val hooks: List<RecommendationHookNetModel> = emptyList(),
)

@Serializable
data class RecommendationFlagNetModel(
    @SerialName("flag_name") val flagName: String,
    @SerialName("value_type") val valueType: String,
    val value: String,
    val title: String? = null,
    val description: String? = null,
    @SerialName("danger_level") val dangerLevel: String? = null,
    val badges: List<BadgeNetModel> = emptyList(),
    @SerialName("package_name") val packageName: String? = null,
)

@Serializable
data class NeedleEnvelopeNetModel(
    @SerialName("media_type") val mediaType: String,
    @SerialName("payload_base64") val payloadBase64: String,
    @SerialName("payload_sha256") val payloadSha256: String,
    @SerialName("signature_algorithm") val signatureAlgorithm: String,
    @SerialName("signature_base64") val signatureBase64: String,
)

@Serializable
data class RecommendationHookNetModel(
    @SerialName("recipe_id") val recipeId: Long,
    val required: Boolean,
    val codename: String? = null,
    val purpose: String? = null,
    @SerialName("signed_envelope") val signedEnvelope: NeedleEnvelopeNetModel? = null,
)
