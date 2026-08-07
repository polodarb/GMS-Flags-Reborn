package ua.polodarb.gmsflags.domain.server.sync

fun interface ResolveRemoteFlagConfiguration {
    suspend operator fun invoke(
        packageName: String,
        versionCode: Long,
        etag: String?,
    ): Result<RemoteFlagConfigurationResult>
}

fun interface ResolveHookCompatibility {
    suspend operator fun invoke(
        packageName: String,
        versionCode: Long,
    ): Result<HookCompatibility>
}
