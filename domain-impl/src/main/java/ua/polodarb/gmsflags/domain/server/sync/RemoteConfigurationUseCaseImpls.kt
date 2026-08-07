package ua.polodarb.gmsflags.domain.server.sync

import ua.polodarb.gmsflags.data.repository.server.RemoteConfigurationRepository

class ResolveRemoteFlagConfigurationUseCase(
    private val repository: RemoteConfigurationRepository,
) : ResolveRemoteFlagConfiguration {
    override suspend fun invoke(
        packageName: String,
        versionCode: Long,
        etag: String?,
    ) = repository.resolveFlags(
        packageName = packageName.requirePackageName(),
        versionCode = versionCode.requireVersionCode(),
        etag = etag?.takeIf(String::isNotBlank),
    )
}

class ResolveHookCompatibilityUseCase(
    private val repository: RemoteConfigurationRepository,
) : ResolveHookCompatibility {
    override suspend fun invoke(packageName: String, versionCode: Long) =
        repository.resolveHookCompatibility(
            packageName = packageName.requirePackageName(),
            versionCode = versionCode.requireVersionCode(),
        )
}

private fun String.requirePackageName(): String = trim().also {
    require(it.isNotEmpty()) { "Package name cannot be blank" }
}

private fun Long.requireVersionCode(): Long = also {
    require(it >= 0L) { "Version code cannot be negative" }
}
