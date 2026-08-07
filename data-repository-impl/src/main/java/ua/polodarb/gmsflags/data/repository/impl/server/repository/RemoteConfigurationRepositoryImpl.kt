package ua.polodarb.gmsflags.data.repository.impl.server.repository

import ua.polodarb.gmsflags.data.network.publicapi.FlagResolveNetResult
import ua.polodarb.gmsflags.data.network.publicapi.GmsFlagsPublicDataSource
import ua.polodarb.gmsflags.data.repository.impl.common.networkResult
import ua.polodarb.gmsflags.data.repository.impl.server.mapper.toDomain
import ua.polodarb.gmsflags.data.repository.server.RemoteConfigurationRepository
import ua.polodarb.gmsflags.domain.server.sync.RemoteFlagConfigurationResult

class RemoteConfigurationRepositoryImpl(
    private val dataSource: GmsFlagsPublicDataSource,
) : RemoteConfigurationRepository {
    override suspend fun resolveFlags(
        packageName: String,
        versionCode: Long,
        etag: String?,
    ) = networkResult {
        when (val result = dataSource.resolveFlags(packageName, versionCode, etag)) {
            is FlagResolveNetResult.Modified -> RemoteFlagConfigurationResult.Modified(
                configuration = result.response.toDomain(),
                etag = result.etag,
            )

            is FlagResolveNetResult.NotModified -> RemoteFlagConfigurationResult.NotModified(
                etag = result.etag,
            )
        }
    }

    override suspend fun resolveHookCompatibility(packageName: String, versionCode: Long) =
        networkResult {
            dataSource.resolveHookCompatibility(packageName, versionCode).toDomain()
        }
}
