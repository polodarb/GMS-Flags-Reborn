package ua.polodarb.gmsflags.domain.impl.community

import ua.polodarb.gmsflags.domain.community.CommunityFlagItem
import ua.polodarb.gmsflags.data.repository.community.CommunityRepository

import ua.polodarb.gmsflags.domain.community.SubmitCommunityPackageUseCase

internal class SubmitCommunityPackageUseCaseImpl(
    private val repository: CommunityRepository,
) : SubmitCommunityPackageUseCase {
    override suspend fun invoke(
        title: String,
        description: String,
        packageName: String,
        flags: List<CommunityFlagItem>,
    ): Boolean {
        return repository.submitPackage(
            title = title,
            description = description,
            packageName = packageName,
            flags = flags,
        )
    }
}
