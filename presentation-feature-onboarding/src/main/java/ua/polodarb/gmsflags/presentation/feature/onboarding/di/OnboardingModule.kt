package ua.polodarb.gmsflags.presentation.feature.onboarding.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import ua.polodarb.gmsflags.presentation.feature.onboarding.OnboardingViewModel

val presentationFeatureOnboardingModule = module {
    viewModel { OnboardingViewModel(requestRootAccess = get(), completeOnboarding = get()) }
}
