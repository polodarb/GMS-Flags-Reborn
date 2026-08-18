package ua.polodarb.gmsflags.data.repository.community

import ua.polodarb.gmsflags.domain.community.CommunityFlagItem
import ua.polodarb.gmsflags.domain.community.CommunityPackage


interface CommunityRepository {

    suspend fun getPackages(query: String? = null): List<CommunityPackage>
    suspend fun getPackageDetails(id: Long): CommunityPackage?
    suspend fun submitPackage(title: String, description: String, packageName: String, flags: List<CommunityFlagItem>): Boolean
    suspend fun reportPackage(packageId: Long, reason: String, comment: String? = null): Boolean
    suspend fun isDisclaimerDismissed(): Boolean
    suspend fun setDisclaimerDismissed(dismissed: Boolean)
}
