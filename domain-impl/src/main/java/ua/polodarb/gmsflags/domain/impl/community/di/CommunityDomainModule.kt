package ua.polodarb.gmsflags.domain.impl.community.di

import org.koin.dsl.module
import ua.polodarb.gmsflags.domain.community.GetCommunityPackagesUseCase
import ua.polodarb.gmsflags.domain.community.SubmitCommunityPackageUseCase
import ua.polodarb.gmsflags.domain.impl.community.GetCommunityPackagesUseCaseImpl
import ua.polodarb.gmsflags.domain.impl.community.SubmitCommunityPackageUseCaseImpl

val communityDomainModule = module {
    factory<GetCommunityPackagesUseCase> { GetCommunityPackagesUseCaseImpl(repository = get()) }
    factory<SubmitCommunityPackageUseCase> { SubmitCommunityPackageUseCaseImpl(repository = get()) }
}
