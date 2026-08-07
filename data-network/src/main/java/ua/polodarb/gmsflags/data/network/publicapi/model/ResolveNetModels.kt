package ua.polodarb.gmsflags.data.network.publicapi.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class ResolvedFlagNetModel(
    @SerialName("flag_name") val flagName: String,
    @SerialName("value_type") val valueType: String,
    val value: JsonElement,
)

@Serializable
data class ResolveResponseNetModel(
    @SerialName("package") val packageName: String,
    @SerialName("version_code") val versionCode: Long,
    val flags: List<ResolvedFlagNetModel> = emptyList(),
    @SerialName("config_hash") val configHash: String,
)

@Serializable
data class VersionConstraintNetModel(
    val type: String,
    val min: Long? = null,
    val max: Long? = null,
)

@Serializable
data class HookCompatibilityProfileNetModel(
    val id: Long,
    val adapter: String,
    @SerialName("version_constraint") val versionConstraint: VersionConstraintNetModel,
    @SerialName("anchor_groups") val anchorGroups: List<List<String>> = emptyList(),
    val enabled: Boolean,
    val priority: Int,
    @SerialName("compatibility_status") val compatibilityStatus: String,
    val notes: String? = null,
)

@Serializable
data class HookCompatibilityResolveNetModel(
    val status: String,
    val profile: HookCompatibilityProfileNetModel? = null,
)
