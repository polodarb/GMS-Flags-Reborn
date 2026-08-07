package ua.polodarb.gmsflags.domain.apps

import ua.polodarb.gmsflags.data.repository.apps.repository.SupportedApplicationsRepository

class GetSupportedApplicationsUseCase(
    private val repository: SupportedApplicationsRepository,
) : GetSupportedApplications {
    override suspend fun invoke() = repository.getApplications().map { applications ->
        applications.normalized()
    }
}
