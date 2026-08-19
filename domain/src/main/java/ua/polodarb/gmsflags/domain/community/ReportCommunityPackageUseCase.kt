package ua.polodarb.gmsflags.domain.community

fun interface ReportCommunityPackageUseCase {
    suspend operator fun invoke(packageId: Long, reason: String, comment: String?): Boolean
}
