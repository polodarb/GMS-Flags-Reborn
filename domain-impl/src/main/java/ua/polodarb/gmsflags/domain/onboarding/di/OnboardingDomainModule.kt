package ua.polodarb.gmsflags.domain.onboarding.di

import org.koin.dsl.module
import ua.polodarb.gmsflags.domain.onboarding.CompleteOnboarding
import ua.polodarb.gmsflags.domain.onboarding.CompleteOnboardingUseCase
import ua.polodarb.gmsflags.domain.onboarding.ObserveOnboardingCompletion
import ua.polodarb.gmsflags.domain.onboarding.ObserveOnboardingCompletionUseCase
import ua.polodarb.gmsflags.domain.onboarding.RequestRootAccess
import ua.polodarb.gmsflags.domain.onboarding.RequestRootAccessUseCase

val onboardingDomainModule = module {
    factory<ObserveOnboardingCompletion> { ObserveOnboardingCompletionUseCase(get()) }
    factory<CompleteOnboarding> { CompleteOnboardingUseCase(get()) }
    factory<RequestRootAccess> { RequestRootAccessUseCase(get()) }
}
