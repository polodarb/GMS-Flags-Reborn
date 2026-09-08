package ua.polodarb.gmsflags.domain.servermode.di

import org.koin.dsl.module
import ua.polodarb.gmsflags.domain.servermode.ObserveServerMode
import ua.polodarb.gmsflags.domain.servermode.ObserveServerModeUseCase
import ua.polodarb.gmsflags.domain.servermode.RefreshServerMode
import ua.polodarb.gmsflags.domain.servermode.RefreshServerModeUseCase

val serverModeDomainModule = module {
    factory<ObserveServerMode> { ObserveServerModeUseCase(repository = get()) }
    factory<RefreshServerMode> { RefreshServerModeUseCase(repository = get()) }
}
