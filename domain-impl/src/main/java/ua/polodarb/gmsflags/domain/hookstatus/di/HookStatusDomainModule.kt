package ua.polodarb.gmsflags.domain.hookstatus.di

import org.koin.dsl.module
import ua.polodarb.gmsflags.domain.hookstatus.GetHookStatus
import ua.polodarb.gmsflags.domain.hookstatus.GetHookStatusUseCase
import ua.polodarb.gmsflags.domain.hookstatus.GetPairipIncompatiblePackages
import ua.polodarb.gmsflags.domain.hookstatus.GetPairipIncompatiblePackagesUseCase
import ua.polodarb.gmsflags.domain.hookstatus.RestartHookTarget
import ua.polodarb.gmsflags.domain.hookstatus.RestartHookTargetUseCase

val hookStatusDomainModule = module {
    factory<GetHookStatus> { GetHookStatusUseCase(get(), get()) }
    factory<GetPairipIncompatiblePackages> { GetPairipIncompatiblePackagesUseCase(get()) }
    factory<RestartHookTarget> { RestartHookTargetUseCase(get()) }
}
