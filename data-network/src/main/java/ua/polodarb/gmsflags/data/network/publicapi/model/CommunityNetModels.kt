package ua.polodarb.gmsflags.data.network.publicapi.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CommunityFlagItemNetModel(
    @SerialName("flag_name") val flagName: String,
    @SerialName("value_type") val valueType: String,
    val value: String,
    @SerialName("package_name") val packageName: String? = null,
)

@Serializable
data class CommunityPackageNetModel(
    val id: Long,
    val title: String,
    val description: String? = null,
    @SerialName("package_name") val packageName: String,
    val author: String? = null,
    val flags: List<CommunityFlagItemNetModel> = emptyList(),
    @SerialName("created_at") val createdAt: Long? = null,
)

@Serializable
data class CommunitySubmitRequestNetModel(
    val title: String,
    val description: String,
    @SerialName("package_name") val packageName: String,
    val flags: List<CommunityFlagItemNetModel>,
)

@Serializable
data class CommunityReportRequestNetModel(
    @SerialName("package_id") val packageId: Long,
    val reason: String,
    val comment: String? = null,
)
