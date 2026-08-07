package ua.polodarb.gmsflags.presentation.feature.apps.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import ua.polodarb.gmsflags.presentation.feature.apps.ui.AppsViewModel

val presentationFeatureAppsModule = module {
    viewModel {
        AppsViewModel(
            getApplicationsSnapshot = get(),
            errorResolver = get(),
            analytics = get(),
        )
    }
}
