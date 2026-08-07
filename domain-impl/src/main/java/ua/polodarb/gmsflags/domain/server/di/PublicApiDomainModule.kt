package ua.polodarb.gmsflags.domain.server.di

import org.koin.dsl.module
import ua.polodarb.gmsflags.domain.server.content.GetApplicationRecommendations
import ua.polodarb.gmsflags.domain.server.content.GetApplicationRecommendationsUseCase
import ua.polodarb.gmsflags.domain.server.content.GetFaq
import ua.polodarb.gmsflags.domain.server.content.GetFaqUseCase
import ua.polodarb.gmsflags.domain.server.content.GetHomeContent
import ua.polodarb.gmsflags.domain.server.content.GetHomeContentUseCase
import ua.polodarb.gmsflags.domain.server.content.GetRecommendationDetails
import ua.polodarb.gmsflags.domain.server.content.GetRecommendationDetailsUseCase
import ua.polodarb.gmsflags.domain.server.content.GetRecommendationExperience
import ua.polodarb.gmsflags.domain.server.content.GetRecommendationExperienceUseCase
import ua.polodarb.gmsflags.domain.server.content.GetRecommendations
import ua.polodarb.gmsflags.domain.server.content.GetRecommendationsUseCase
import ua.polodarb.gmsflags.domain.server.content.GetRecommendationFeed
import ua.polodarb.gmsflags.domain.server.content.GetRecommendationFeedUseCase
import ua.polodarb.gmsflags.domain.server.content.GetServerApplication
import ua.polodarb.gmsflags.domain.server.content.GetServerApplicationUseCase
import ua.polodarb.gmsflags.domain.server.content.GetServerApplications
import ua.polodarb.gmsflags.domain.server.content.GetServerApplicationsUseCase
import ua.polodarb.gmsflags.domain.server.sync.ResolveHookCompatibility
import ua.polodarb.gmsflags.domain.server.sync.ResolveHookCompatibilityUseCase
import ua.polodarb.gmsflags.domain.server.sync.ResolveRemoteFlagConfiguration
import ua.polodarb.gmsflags.domain.server.sync.ResolveRemoteFlagConfigurationUseCase

val publicApiDomainModule = module {
    factory<GetHomeContent> { GetHomeContentUseCase(repository = get()) }
    factory<GetFaq> { GetFaqUseCase(repository = get()) }
    factory<GetServerApplications> { GetServerApplicationsUseCase(repository = get()) }
    factory<GetServerApplication> { GetServerApplicationUseCase(repository = get()) }
    factory<GetApplicationRecommendations> { GetApplicationRecommendationsUseCase(repository = get()) }
    factory<GetRecommendations> { GetRecommendationsUseCase(repository = get()) }
    factory<GetRecommendationFeed> {
        GetRecommendationFeedUseCase(
            repository = get(),
            supportedApplicationsRepository = get(),
            flagDetailsRepository = get(),
        )
    }
    factory<GetRecommendationDetails> { GetRecommendationDetailsUseCase(repository = get()) }
    factory<GetRecommendationExperience> {
        GetRecommendationExperienceUseCase(
            publicContentRepository = get(),
            supportedApplicationsRepository = get(),
        )
    }
    factory<ResolveRemoteFlagConfiguration> {
        ResolveRemoteFlagConfigurationUseCase(repository = get())
    }
    factory<ResolveHookCompatibility> { ResolveHookCompatibilityUseCase(repository = get()) }
}
