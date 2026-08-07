package ua.polodarb.gmsflags.data.network.publicapi

import ua.polodarb.gmsflags.data.network.publicapi.model.ResolveResponseNetModel

sealed interface FlagResolveNetResult {
    val etag: String?

    data class Modified(
        val response: ResolveResponseNetModel,
        override val etag: String?,
    ) : FlagResolveNetResult

    data class NotModified(
        override val etag: String?,
    ) : FlagResolveNetResult
}
