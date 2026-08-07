package ua.polodarb.gmsflags.data.network.publicapi.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AppNetModel(
    val id: Long,
    @SerialName("package_name") val packageName: String,
    @SerialName("display_name") val displayName: String,
    @SerialName("icon_url") val iconUrl: String? = null,
    @SerialName("disabled_from_version") val disabledFromVersion: Long? = null,
)

@Serializable
data class InfoBlockNetModel(
    val id: Long,
    val type: String,
    val message: String,
    @SerialName("is_active") val isActive: Boolean,
    @SerialName("sort_order") val sortOrder: Int,
    @SerialName("external_link") val externalLink: String? = null,
)

@Serializable
data class FaqEntryNetModel(
    val id: Long,
    val question: String,
    val answer: String,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("sort_order") val sortOrder: Int = 0,
)

@Serializable
data class BadgeNetModel(
    val label: String,
)

@Serializable
data class FlagCatalogEntryNetModel(
    val id: Long,
    @SerialName("flag_name") val flagName: String,
    val title: String? = null,
    val description: String? = null,
    @SerialName("danger_level") val dangerLevel: String,
    val badges: List<BadgeNetModel> = emptyList(),
)

@Serializable
data class AppDetailsNetModel(
    val app: AppNetModel,
    @SerialName("info_blocks") val infoBlocks: List<InfoBlockNetModel> = emptyList(),
    @SerialName("flag_catalog") val flagCatalog: List<FlagCatalogEntryNetModel> = emptyList(),
)
