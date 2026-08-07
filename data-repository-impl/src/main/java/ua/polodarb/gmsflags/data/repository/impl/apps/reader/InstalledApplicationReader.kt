package ua.polodarb.gmsflags.data.repository.impl.apps.reader

internal data class InstalledApplicationMetadata(
    val packageName: String,
    val name: String,
    val versionName: String?,
    val versionCode: Long,
    val lastUpdateTime: Long,
)

internal fun interface InstalledApplicationReader {
    suspend fun readInstalledApplications(
        packageNames: Set<String>,
    ): Map<String, InstalledApplicationMetadata>
}
