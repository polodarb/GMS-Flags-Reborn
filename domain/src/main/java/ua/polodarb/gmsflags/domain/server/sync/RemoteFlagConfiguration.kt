package ua.polodarb.gmsflags.domain.server.sync

import ua.polodarb.gmsflags.domain.server.content.RemoteFlagValueType

sealed interface RemoteFlagConfigurationResult {
    val etag: String?

    data class Modified(
        val configuration: RemoteFlagConfiguration,
        override val etag: String?,
    ) : RemoteFlagConfigurationResult

    data class NotModified(
        override val etag: String?,
    ) : RemoteFlagConfigurationResult
}

data class RemoteFlagConfiguration(
    val packageName: String,
    val versionCode: Long,
    val flags: List<RemoteResolvedFlag>,
    val configHash: String,
)

data class RemoteResolvedFlag(
    val name: String,
    val type: RemoteFlagValueType,
    val value: String,
)
