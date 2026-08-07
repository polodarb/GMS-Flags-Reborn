package ua.polodarb.gmsflags.data.network.publicapi.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReportRequestNetModel(
    val message: String,
    val contact: String? = null,
    @SerialName("app_version_name") val appVersionName: String,
    @SerialName("app_version_code") val appVersionCode: Long,
    @SerialName("app_signature_sha256") val appSignatureSha256: String? = null,
    val device: ReportDeviceNetModel,
    val context: ReportContextNetModel,
    @SerialName("xposed_logs") val xposedLogs: String,
    val category: String,
)

@Serializable
data class ReportDeviceNetModel(
    val manufacturer: String,
    val model: String,
    @SerialName("android_release") val androidRelease: String,
    @SerialName("sdk_int") val sdkInt: Int,
    val abi: String,
)

@Serializable
data class ReportContextNetModel(
    @SerialName("recommendation_id") val recommendationId: Long? = null,
    @SerialName("variant_label") val variantLabel: String? = null,
    @SerialName("hook_recipe_id") val hookRecipeId: Long? = null,
    @SerialName("hook_trust_status") val hookTrustStatus: String? = null,
    @SerialName("target_package") val targetPackage: String? = null,
    @SerialName("target_version_name") val targetVersionName: String? = null,
)

@Serializable
data class ReportResponseNetModel(
    val id: Long? = null,
)
