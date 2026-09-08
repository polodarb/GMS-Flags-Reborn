package ua.polodarb.gmsflags.di

import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.core.module.dsl.viewModel
import ua.polodarb.gmsflags.core.root.di.coreRootModule
import ua.polodarb.gmsflags.data.network.APP_SIGNATURE_SHA256_QUALIFIER_NAME
import ua.polodarb.gmsflags.data.network.ServerEnvironment
import ua.polodarb.gmsflags.data.network.impl.di.dataNetworkModule
import ua.polodarb.gmsflags.data.phenotype.di.phenotypeDataModule
import ua.polodarb.gmsflags.data.repository.impl.di.dataRepositoryModule
import ua.polodarb.gmsflags.data.repository.report.datasource.ReportDiagnosticsCollector
import ua.polodarb.gmsflags.data.repository.servermode.ServerModeRepository
import ua.polodarb.gmsflags.domain.apps.IsOfficialAppBuild
import ua.polodarb.gmsflags.domain.apps.IsOfficialAppBuildUseCase
import ua.polodarb.gmsflags.domain.apps.di.appsDomainModule
import ua.polodarb.gmsflags.domain.navigation.di.navigationFlagsDomainModule
import ua.polodarb.gmsflags.navigationflags.navigationFlagsModule
import ua.polodarb.gmsflags.domain.server.di.publicApiDomainModule
import ua.polodarb.gmsflags.domain.flags.di.flagsDomainModule
import ua.polodarb.gmsflags.domain.flags.di.NEEDLE_TRUSTED_PUBLIC_KEY_QUALIFIER
import ua.polodarb.gmsflags.presentation.feature.apps.di.presentationFeatureAppsModule
import ua.polodarb.gmsflags.presentation.feature.insight.di.presentationFeatureInsightModule
import ua.polodarb.gmsflags.presentation.feature.suggestions.di.presentationFeatureSuggestionsModule
import ua.polodarb.gmsflags.presentation.feature.flagdetails.di.presentationFeatureFlagDetailsModule
import ua.polodarb.gmsflags.BuildConfig
import ua.polodarb.gmsflags.analytics.analyticsModule
import ua.polodarb.gmsflags.update.updateModule
import ua.polodarb.gmsflags.domain.hookstatus.di.hookStatusDomainModule
import ua.polodarb.gmsflags.presentation.feature.hookstatus.di.presentationFeatureHookStatusModule
import ua.polodarb.gmsflags.presentation.core.error.DefaultErrorResolver
import ua.polodarb.gmsflags.presentation.core.error.ErrorResolver
import ua.polodarb.gmsflags.presentation.core.ui.application.ApplicationIconProvider
import ua.polodarb.gmsflags.presentation.core.ui.application.CachedApplicationIconProvider
import ua.polodarb.gmsflags.domain.settings.di.settingsDomainModule
import ua.polodarb.gmsflags.domain.servermode.di.serverModeDomainModule
import ua.polodarb.gmsflags.servermode.serverModeModule
import ua.polodarb.gmsflags.presentation.feature.settings.di.presentationFeatureSettingsModule
import ua.polodarb.gmsflags.domain.onboarding.di.onboardingDomainModule
import ua.polodarb.gmsflags.domain.report.di.reportsDomainModule
import ua.polodarb.gmsflags.presentation.feature.onboarding.di.presentationFeatureOnboardingModule
import ua.polodarb.gmsflags.startup.AppStartupViewModel
import ua.polodarb.xposed.info.BuildConfig as XposedInfoBuildConfig

val appModules = listOf(
    module {
        single { ServerEnvironment(BuildConfig.SERVER_BASE_URL) }
        viewModel {
            AppStartupViewModel(
                observeOnboardingCompletion = get(),
                requestRootAccess = get(),
                refreshServerMode = get(),
                hasCachedServerMode = { get<ServerModeRepository>().hasCachedValue },
                refreshNavigationFlags = get(),
            )
        }
        single<ErrorResolver> { DefaultErrorResolver() }
        single<ApplicationIconProvider> {
            CachedApplicationIconProvider(
                packageManager = get<android.content.Context>().packageManager,
            )
        }
        single<String?>(qualifier = NEEDLE_TRUSTED_PUBLIC_KEY_QUALIFIER) {
            XposedInfoBuildConfig.NEEDLE_TRUSTED_PUBLIC_KEY_BASE64.takeIf { it.isNotBlank() }
        }
        single<String?>(qualifier = named(APP_SIGNATURE_SHA256_QUALIFIER_NAME)) {
            get<ReportDiagnosticsCollector>().collect().appSignatureSha256
        }
        single<IsOfficialAppBuild> {
            IsOfficialAppBuildUseCase(
                trustedSignatureSha256 = BuildConfig.TRUSTED_APP_SIGNATURE_SHA256,
                actualSignatureSha256 = get(qualifier = named(APP_SIGNATURE_SHA256_QUALIFIER_NAME)),
                isDebugBuild = BuildConfig.DEBUG,
            )
        }
    },
    analyticsModule,
    updateModule,
    serverModeModule,
    navigationFlagsModule,
    coreRootModule,
    dataNetworkModule,
    phenotypeDataModule,
    dataRepositoryModule,
    appsDomainModule,
    flagsDomainModule,
    publicApiDomainModule,
    hookStatusDomainModule,
    settingsDomainModule,
    serverModeDomainModule,
    navigationFlagsDomainModule,
    onboardingDomainModule,
    reportsDomainModule,
    presentationFeatureSuggestionsModule,
    presentationFeatureAppsModule,
    presentationFeatureFlagDetailsModule,
    presentationFeatureInsightModule,
    presentationFeatureHookStatusModule,
    presentationFeatureSettingsModule,
    presentationFeatureOnboardingModule,
)
