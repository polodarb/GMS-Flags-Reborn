package ua.polodarb.gmsflags.domain.apps

fun interface GetSupportedApplications {
    suspend operator fun invoke(): Result<List<SupportedApplication>>
}
