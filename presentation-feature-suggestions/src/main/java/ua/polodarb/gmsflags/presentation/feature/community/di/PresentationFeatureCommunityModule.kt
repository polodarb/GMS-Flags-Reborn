package ua.polodarb.gmsflags.presentation.feature.community.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import ua.polodarb.gmsflags.data.repository.community.CommunityRepository
import ua.polodarb.gmsflags.data.repository.flags.FlagDetailsRepository
import ua.polodarb.gmsflags.presentation.feature.community.ui.CommunityViewModel

val presentationFeatureCommunityModule = module {
    viewModel {
        CommunityViewModel(
            communityRepository = get<CommunityRepository>(),
            flagDetailsRepository = get<FlagDetailsRepository>(),
        )
    }
}

