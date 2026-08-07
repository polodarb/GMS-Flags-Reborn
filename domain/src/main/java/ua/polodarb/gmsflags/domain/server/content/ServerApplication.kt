package ua.polodarb.gmsflags.domain.server.content

data class ServerApplication(
    val id: Long,
    val packageName: String,
    val displayName: String,
    val iconUrl: String?,
    val disabledFromVersion: Long? = null,
)

data class ServerApplicationDetails(
    val application: ServerApplication,
    val infoBlocks: List<ServerInfoBlock>,
    val highlightedFlags: List<ServerFlagCatalogEntry>,
)

data class ServerInfoBlock(
    val id: Long,
    val type: InfoBlockType,
    val message: String,
    val isActive: Boolean,
    val sortOrder: Int,
    val externalLink: String? = null,
)

enum class InfoBlockType { Info, Warning, Success, Promo, Danger, Unknown }

data class ServerFlagCatalogEntry(
    val id: Long,
    val flagName: String,
    val title: String?,
    val description: String?,
    val dangerLevel: DangerLevel,
    val badges: List<ServerBadge>,
)

data class ServerBadge(
    val label: String,
)

enum class DangerLevel { None, Caution, Dangerous, Unknown }
