package ua.polodarb.gmsflags.domain.settings.di

import org.koin.dsl.module
import ua.polodarb.gmsflags.domain.settings.DeleteAllOverrides
import ua.polodarb.gmsflags.domain.settings.DeleteAllOverridesUseCase
import ua.polodarb.gmsflags.domain.settings.DeleteApplicationOverrides
import ua.polodarb.gmsflags.domain.settings.DeleteApplicationOverridesUseCase
import ua.polodarb.gmsflags.domain.settings.ObserveOverrideControl
import ua.polodarb.gmsflags.domain.settings.ObserveOverrideControlUseCase
import ua.polodarb.gmsflags.domain.settings.RefreshOverrideControl
import ua.polodarb.gmsflags.domain.settings.RefreshOverrideControlUseCase
import ua.polodarb.gmsflags.domain.settings.SetOverridesPaused
import ua.polodarb.gmsflags.domain.settings.SetOverridesPausedUseCase
import ua.polodarb.gmsflags.domain.settings.ObserveAnalyticsConsent
import ua.polodarb.gmsflags.domain.settings.ObserveAnalyticsConsentUseCase
import ua.polodarb.gmsflags.domain.settings.SetAnalyticsConsent
import ua.polodarb.gmsflags.domain.settings.SetAnalyticsConsentUseCase

val settingsDomainModule = module {
    factory<ObserveOverrideControl> { ObserveOverrideControlUseCase(get()) }
    factory<RefreshOverrideControl> { RefreshOverrideControlUseCase(get()) }
    factory<SetOverridesPaused> { SetOverridesPausedUseCase(get()) }
    factory<DeleteAllOverrides> { DeleteAllOverridesUseCase(get()) }
    factory<DeleteApplicationOverrides> { DeleteApplicationOverridesUseCase(get()) }
    factory<ObserveAnalyticsConsent> { ObserveAnalyticsConsentUseCase(get()) }
    factory<SetAnalyticsConsent> { SetAnalyticsConsentUseCase(get()) }
}
