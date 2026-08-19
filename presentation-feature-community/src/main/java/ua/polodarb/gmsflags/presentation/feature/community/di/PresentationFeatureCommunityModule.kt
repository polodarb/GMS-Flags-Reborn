package ua.polodarb.gmsflags.presentation.feature.community.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import ua.polodarb.gmsflags.domain.apps.GetSupportedApplications
import ua.polodarb.gmsflags.domain.community.GetCommunityPackagesUseCase
import ua.polodarb.gmsflags.domain.community.SubmitCommunityPackageUseCase
import ua.polodarb.gmsflags.domain.flags.ApplyFlagOverrides
import ua.polodarb.gmsflags.presentation.feature.community.ui.CommunityViewModel

val presentationFeatureCommunityModule = module {
    viewModel {
        CommunityViewModel(
            getCommunityPackages = get<GetCommunityPackagesUseCase>(),
            submitCommunityPackage = get<SubmitCommunityPackageUseCase>(),
            applyFlagOverrides = get<ApplyFlagOverrides>(),
            getSupportedApplications = get<GetSupportedApplications>(),
        )
    }
}
