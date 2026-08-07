package ua.polodarb.gmsflags.data.repository.settings.datasource

interface OverrideControlDataSource {
    suspend fun readOverrideCount(androidPackageNames: List<String>): Result<Int>
    suspend fun readPaused(androidPackageNames: List<String>): Result<Boolean>
    suspend fun setPaused(androidPackageNames: List<String>, paused: Boolean): Result<Unit>
    suspend fun deleteAll(androidPackageNames: List<String>): Result<Unit>
}
