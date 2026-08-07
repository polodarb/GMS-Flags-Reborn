package ua.polodarb.gmsflags.presentation.feature.hookstatus.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import ua.polodarb.gmsflags.presentation.feature.hookstatus.details.HookStatusDetailsViewModel
import ua.polodarb.gmsflags.presentation.feature.hookstatus.overview.HookStatusViewModel

val presentationFeatureHookStatusModule = module {
    viewModel {
        HookStatusViewModel(
            getHookStatus = get(),
            errorResolver = get(),
        )
    }
    viewModel { parameters ->
        HookStatusDetailsViewModel(
            androidPackageName = parameters.get(),
            getHookStatus = get(),
            restartHookTarget = get(),
            deleteApplicationOverrides = get(),
            errorResolver = get(),
            analytics = get(),
        )
    }
}
