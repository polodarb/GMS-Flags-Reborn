package ua.polodarb.gmsflags.data.repository.apps.datasource

data class XposedScopeSnapshot(
    val moduleFound: Boolean,
    val moduleEnabled: Boolean,
    val scopedPackageNames: Set<String>,
)

fun interface XposedScopeDataSource {
    suspend fun readScope(
        modulePackageName: String,
        userId: Int,
    ): Result<XposedScopeSnapshot>
}
