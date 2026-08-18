package ua.polodarb.gmsflags.domain.community

interface GetCommunityPackagesUseCase {
    suspend operator fun invoke(query: String? = null): List<CommunityPackage>
}
