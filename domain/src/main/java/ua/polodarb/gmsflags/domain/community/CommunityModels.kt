package ua.polodarb.gmsflags.domain.community

data class CommunityFlagItem(
    val flagName: String,
    val valueType: String,
    val value: String,
    val packageName: String? = null,
)

data class CommunityPackage(
    val id: Long,
    val title: String,
    val description: String?,
    val packageName: String,
    val author: String?,
    val flags: List<CommunityFlagItem>,
    val createdAt: Long?,
)
