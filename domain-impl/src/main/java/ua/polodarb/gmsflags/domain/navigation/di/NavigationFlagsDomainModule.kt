package ua.polodarb.gmsflags.domain.navigation.di

import org.koin.dsl.module
import ua.polodarb.gmsflags.domain.navigation.ObserveGmsInsightHidden
import ua.polodarb.gmsflags.domain.navigation.ObserveGmsInsightHiddenUseCase
import ua.polodarb.gmsflags.domain.navigation.RefreshNavigationFlags
import ua.polodarb.gmsflags.domain.navigation.RefreshNavigationFlagsUseCase

val navigationFlagsDomainModule = module {
    factory<ObserveGmsInsightHidden> { ObserveGmsInsightHiddenUseCase(repository = get()) }
    factory<RefreshNavigationFlags> { RefreshNavigationFlagsUseCase(repository = get()) }
}
