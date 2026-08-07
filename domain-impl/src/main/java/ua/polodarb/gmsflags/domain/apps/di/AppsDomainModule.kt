package ua.polodarb.gmsflags.domain.apps.di

import org.koin.dsl.module
import ua.polodarb.gmsflags.domain.apps.GetSupportedApplications
import ua.polodarb.gmsflags.domain.apps.GetSupportedApplicationsUseCase
import ua.polodarb.gmsflags.domain.apps.GetSupportedApplicationsSnapshot
import ua.polodarb.gmsflags.domain.apps.GetSupportedApplicationsSnapshotUseCase
import ua.polodarb.gmsflags.domain.apps.GetApplicationXposedScopeStatus
import ua.polodarb.gmsflags.domain.apps.GetApplicationXposedScopeStatusUseCase

val appsDomainModule = module {
    factory<GetSupportedApplications> {
        GetSupportedApplicationsUseCase(repository = get())
    }
    factory<GetSupportedApplicationsSnapshot> {
        GetSupportedApplicationsSnapshotUseCase(
            repository = get(),
            publicContentRepository = get(),
            getPairipIncompatiblePackages = get(),
        )
    }
    factory<GetApplicationXposedScopeStatus> {
        GetApplicationXposedScopeStatusUseCase(repository = get())
    }
}
