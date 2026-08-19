package ua.polodarb.gmsflags.domain.community

interface SubmitCommunityPackageUseCase {
    suspend operator fun invoke(
        title: String,
        description: String,
        packageName: String,
        flags: List<CommunityFlagItem>,
    ): Boolean
}
