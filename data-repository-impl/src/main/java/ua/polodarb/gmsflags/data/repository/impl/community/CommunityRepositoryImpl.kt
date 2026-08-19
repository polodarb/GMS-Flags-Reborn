package ua.polodarb.gmsflags.data.repository.impl.community

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import ua.polodarb.gmsflags.data.network.publicapi.GmsFlagsPublicDataSource
import ua.polodarb.gmsflags.data.network.publicapi.model.CommunityFlagItemNetModel
import ua.polodarb.gmsflags.data.network.publicapi.model.CommunityPackageNetModel
import ua.polodarb.gmsflags.data.network.publicapi.model.CommunityReportRequestNetModel
import ua.polodarb.gmsflags.data.network.publicapi.model.CommunitySubmitRequestNetModel
import ua.polodarb.gmsflags.domain.community.CommunityFlagItem
import ua.polodarb.gmsflags.domain.community.CommunityPackage
import ua.polodarb.gmsflags.data.repository.community.CommunityRepository


internal class CommunityRepositoryImpl(
    private val dataSource: GmsFlagsPublicDataSource,
    context: Context,
) : CommunityRepository {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val localPackages = MutableStateFlow<List<CommunityPackage>>(emptyList())

    override suspend fun getPackages(query: String?): List<CommunityPackage> {
        val remote = runCatching { dataSource.getCommunityPackages(query) }.getOrElse { emptyList() }
        val mappedRemote = remote.map { it.toDomain() }
        val combined = (localPackages.value + mappedRemote).distinctBy { it.id }
        if (query.isNullOrBlank()) return combined
        val q = query.trim().lowercase()
        return combined.filter { pkg ->
            pkg.title.lowercase().contains(q) ||
                    (pkg.description?.lowercase()?.contains(q) == true) ||
                    pkg.packageName.lowercase().contains(q) ||
                    pkg.flags.any { it.flagName.lowercase().contains(q) }
        }
    }

    override suspend fun getPackageDetails(id: Long): CommunityPackage? {
        val local = localPackages.value.find { it.id == id }
        if (local != null) return local
        return runCatching { dataSource.getCommunityPackage(id).toDomain() }.getOrNull()
    }

    override suspend fun submitPackage(
        title: String,
        description: String,
        packageName: String,
        flags: List<CommunityFlagItem>,
    ): Boolean {
        val netFlags = flags.map { it.toNetModel() }
        val request = CommunitySubmitRequestNetModel(
            title = title,
            description = description,
            packageName = packageName,
            flags = netFlags,
        )
        val remoteSuccess = runCatching { dataSource.submitCommunityPackage(request) }.getOrDefault(false)
        if (remoteSuccess) {
            val newLocal = CommunityPackage(
                id = System.currentTimeMillis(),
                title = title,
                description = description,
                packageName = packageName,
                author = "User",
                flags = flags,
                createdAt = System.currentTimeMillis(),
            )
            localPackages.value = listOf(newLocal) + localPackages.value
        }
        return remoteSuccess
    }

    override suspend fun reportPackage(packageId: Long, reason: String, comment: String?): Boolean {
        val request = CommunityReportRequestNetModel(
            packageId = packageId,
            reason = reason,
            comment = comment,
        )
        return runCatching { dataSource.reportCommunityPackage(request) }.getOrDefault(false)
    }


    override suspend fun isDisclaimerDismissed(): Boolean {
        return prefs.getBoolean(KEY_DISCLAIMER_DISMISSED, false)
    }

    override suspend fun setDisclaimerDismissed(dismissed: Boolean) {
        prefs.edit().putBoolean(KEY_DISCLAIMER_DISMISSED, dismissed).apply()
    }

    private fun CommunityPackageNetModel.toDomain() = CommunityPackage(
        id = id,
        title = title,
        description = description,
        packageName = packageName,
        author = author,
        flags = flags.map { it.toDomain() },
        createdAt = createdAt,
    )

    private fun CommunityFlagItemNetModel.toDomain() = CommunityFlagItem(
        flagName = flagName,
        valueType = valueType,
        value = value,
        packageName = packageName,
    )

    private fun CommunityFlagItem.toNetModel() = CommunityFlagItemNetModel(
        flagName = flagName,
        valueType = valueType,
        value = value,
        packageName = packageName,
    )

    private companion object {
        const val PREFS_NAME = "community_preferences"
        const val KEY_DISCLAIMER_DISMISSED = "disclaimer_dismissed"
    }
}
