package ua.polodarb.gmsflags.domain.community

import ua.polodarb.gmsflags.data.repository.community.CommunityRepository

class ReportCommunityPackageUseCaseImpl(
    private val repository: CommunityRepository,
) : ReportCommunityPackageUseCase {
    override suspend fun invoke(packageId: Long, reason: String, comment: String?): Boolean {
        return repository.reportPackage(packageId, reason, comment)
    }
}
