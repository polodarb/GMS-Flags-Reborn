package ua.polodarb.gmsflags.domain.apps

fun interface GetSupportedApplicationsSnapshot {
    suspend operator fun invoke(): Result<SupportedApplicationsSnapshot>
}
