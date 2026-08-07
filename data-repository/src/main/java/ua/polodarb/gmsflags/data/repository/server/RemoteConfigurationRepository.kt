package ua.polodarb.gmsflags.data.repository.server

import ua.polodarb.gmsflags.domain.server.sync.HookCompatibility
import ua.polodarb.gmsflags.domain.server.sync.RemoteFlagConfigurationResult

interface RemoteConfigurationRepository {
    suspend fun resolveFlags(
        packageName: String,
        versionCode: Long,
        etag: String? = null,
    ): Result<RemoteFlagConfigurationResult>

    suspend fun resolveHookCompatibility(
        packageName: String,
        versionCode: Long,
    ): Result<HookCompatibility>
}
