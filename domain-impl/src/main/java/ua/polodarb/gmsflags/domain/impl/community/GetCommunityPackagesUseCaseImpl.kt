package ua.polodarb.gmsflags.domain.impl.community

import ua.polodarb.gmsflags.domain.community.CommunityPackage
import ua.polodarb.gmsflags.data.repository.community.CommunityRepository

import ua.polodarb.gmsflags.domain.community.GetCommunityPackagesUseCase

internal class GetCommunityPackagesUseCaseImpl(
    private val repository: CommunityRepository,
) : GetCommunityPackagesUseCase {
    override suspend fun invoke(query: String?): List<CommunityPackage> {
        return repository.getPackages(query)
    }
}
