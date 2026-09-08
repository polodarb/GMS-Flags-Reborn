package ua.polodarb.gmsflags.presentation.feature.settings.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import ua.polodarb.gmsflags.presentation.feature.settings.faq.FaqViewModel
import ua.polodarb.gmsflags.presentation.feature.settings.feedback.SendFeedbackViewModel
import ua.polodarb.gmsflags.presentation.feature.settings.overrides.OverridesViewModel
import ua.polodarb.gmsflags.presentation.feature.settings.overview.SettingsViewModel
import ua.polodarb.gmsflags.presentation.feature.settings.ui.OverrideNoticeViewModel

val presentationFeatureSettingsModule = module {
    viewModel { SettingsViewModel(get(), get(), get(), get(), get(), get(), get()) }
    viewModel { OverridesViewModel(get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel { OverrideNoticeViewModel(get()) }
    viewModel { FaqViewModel(get(), get()) }
    viewModel { SendFeedbackViewModel(get(), get()) }
}
