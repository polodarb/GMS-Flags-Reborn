package ua.polodarb.gmsflags.data.repository.impl.di

import org.koin.dsl.module
import ua.polodarb.gmsflags.data.repository.apps.repository.SupportedApplicationsRepository
import ua.polodarb.gmsflags.data.repository.apps.repository.XposedScopeRepository
import ua.polodarb.gmsflags.data.repository.flags.FlagDetailsRepository
import ua.polodarb.gmsflags.data.repository.flags.FlagOverridesChangeBus
import ua.polodarb.gmsflags.data.repository.flags.HooksRepository
import ua.polodarb.gmsflags.data.repository.settings.repository.AnalyticsConsentRepository
import ua.polodarb.gmsflags.data.repository.impl.settings.repository.AnalyticsConsentRepositoryImpl
import ua.polodarb.gmsflags.data.repository.impl.flags.FlagOverridesChangeBusImpl
import ua.polodarb.gmsflags.data.repository.impl.apps.repository.SupportedApplicationsRepositoryImpl
import ua.polodarb.gmsflags.data.repository.impl.apps.repository.XposedScopeRepositoryImpl
import ua.polodarb.gmsflags.data.repository.impl.apps.reader.AndroidInstalledApplicationReader
import ua.polodarb.gmsflags.data.repository.impl.apps.reader.InstalledApplicationReader
import ua.polodarb.gmsflags.data.repository.impl.flags.FlagDetailsRepositoryImpl
import ua.polodarb.gmsflags.data.repository.impl.flags.HooksRepositoryImpl
import ua.polodarb.gmsflags.data.repository.impl.flags.RootTargetProcessRestarter
import ua.polodarb.gmsflags.data.repository.impl.flags.TargetProcessRestarter
import ua.polodarb.gmsflags.data.repository.impl.server.repository.PublicContentRepositoryImpl
import ua.polodarb.gmsflags.data.repository.impl.server.repository.RemoteConfigurationRepositoryImpl
import ua.polodarb.gmsflags.data.repository.impl.server.store.DataStoreAppliedRecommendationSetupStore
import ua.polodarb.gmsflags.data.repository.impl.server.store.DataStoreLocallyDemotedRecommendationsStore
import ua.polodarb.gmsflags.data.repository.server.PublicContentRepository
import ua.polodarb.gmsflags.data.repository.server.RemoteConfigurationRepository
import ua.polodarb.gmsflags.data.repository.report.datasource.ReportDiagnosticsCollector
import ua.polodarb.gmsflags.data.repository.report.repository.ReportsRepository
import ua.polodarb.gmsflags.data.repository.impl.report.datasource.AndroidReportDiagnosticsCollector
import ua.polodarb.gmsflags.data.repository.impl.report.repository.ReportsRepositoryImpl
import ua.polodarb.gmsflags.data.repository.impl.servermode.OfflinePublicContentRepository
import ua.polodarb.gmsflags.data.repository.impl.servermode.OfflineRemoteConfigurationRepository
import ua.polodarb.gmsflags.data.repository.impl.servermode.OfflineReportsRepository
import ua.polodarb.xposed.info.XposedTargetRegistry
import ua.polodarb.xposed.info.XposedTargets
import ua.polodarb.gmsflags.data.repository.hookstatus.repository.HookStatusRepository
import ua.polodarb.gmsflags.data.repository.impl.hookstatus.HookStatusRepositoryImpl
import android.content.Context
import android.os.Process
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.qualifier.named
import ua.polodarb.gmsflags.data.repository.settings.repository.OverrideControlRepository
import ua.polodarb.gmsflags.data.repository.impl.settings.repository.OverrideControlRepositoryImpl
import ua.polodarb.gmsflags.data.repository.impl.settings.datasource.SharedPreferencesOverrideControlPreferences
import ua.polodarb.gmsflags.data.repository.onboarding.OnboardingRepository
import ua.polodarb.gmsflags.data.repository.impl.onboarding.DataStoreOnboardingRepository
import ua.polodarb.gmsflags.domain.server.content.AppliedRecommendationSetupStore
import ua.polodarb.gmsflags.domain.server.content.LocallyDemotedRecommendationsStore

val dataRepositoryModule = module {
    single(named(ONBOARDING_DATA_STORE)) {
        PreferenceDataStoreFactory.create(
            scope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
            produceFile = { get<Context>().preferencesDataStoreFile("onboarding") },
        )
    }
    single(named(APPLIED_RECOMMENDATION_SETUPS_DATA_STORE)) {
        PreferenceDataStoreFactory.create(
            scope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
            produceFile = {
                get<Context>().preferencesDataStoreFile("applied_recommendation_setups")
            },
        )
    }
    single(named(DEMOTED_RECOMMENDATIONS_DATA_STORE)) {
        PreferenceDataStoreFactory.create(
            scope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
            produceFile = {
                get<Context>().preferencesDataStoreFile("demoted_recommendations")
            },
        )
    }
    single<OnboardingRepository> {
        DataStoreOnboardingRepository(dataStore = get(named(ONBOARDING_DATA_STORE)))
    }
    single<XposedTargetRegistry> { XposedTargets }
    single<InstalledApplicationReader> { AndroidInstalledApplicationReader(context = get()) }
    single<SupportedApplicationsRepository> {
        SupportedApplicationsRepositoryImpl(
            packageReader = get(),
            installedApplicationReader = get(),
            xposedTargetRegistry = get(),
            xposedScopeDataSource = get(),
            modulePackageName = get<Context>().packageName,
            userId = Process.myUid() / ANDROID_UIDS_PER_USER,
        )
    }
    single<XposedScopeRepository> {
        XposedScopeRepositoryImpl(
            dataSource = get(),
            modulePackageName = get<Context>().packageName,
            userId = Process.myUid() / ANDROID_UIDS_PER_USER,
        )
    }
    single<TargetProcessRestarter> { RootTargetProcessRestarter(commandExecutor = get()) }
    single<FlagOverridesChangeBus> { FlagOverridesChangeBusImpl() }
    single<AnalyticsConsentRepository> { AnalyticsConsentRepositoryImpl(get()) }
    single<FlagDetailsRepository> {
        FlagDetailsRepositoryImpl(
            dataSource = get(),
            targetRestarter = get(),
            changeBus = get(),
        )
    }
    single<HooksRepository> {
        HooksRepositoryImpl(
            dataSource = get(),
            targetRestarter = get(),
        )
    }
    single<PublicContentRepository> {
        OfflinePublicContentRepository(
            delegate = PublicContentRepositoryImpl(dataSource = get()),
            serverMode = get(),
        )
    }
    single<AppliedRecommendationSetupStore> {
        DataStoreAppliedRecommendationSetupStore(
            dataStore = get(named(APPLIED_RECOMMENDATION_SETUPS_DATA_STORE)),
        )
    }
    single<LocallyDemotedRecommendationsStore> {
        DataStoreLocallyDemotedRecommendationsStore(
            dataStore = get(named(DEMOTED_RECOMMENDATIONS_DATA_STORE)),
        )
    }
    single<ReportDiagnosticsCollector> {
        AndroidReportDiagnosticsCollector(context = get())
    }
    single<ReportsRepository> {
        OfflineReportsRepository(
            delegate = ReportsRepositoryImpl(
                dataSource = get(),
                diagnosticsCollector = get(),
                xposedLogsDataSource = get(),
            ),
            serverMode = get(),
        )
    }
    single<RemoteConfigurationRepository> {
        OfflineRemoteConfigurationRepository(
            delegate = RemoteConfigurationRepositoryImpl(dataSource = get()),
            serverMode = get(),
        )
    }
    single<HookStatusRepository> {
        HookStatusRepositoryImpl(dataSource = get(), targetRestarter = get())
    }
    single<OverrideControlRepository> {
        OverrideControlRepositoryImpl(
            applicationsRepository = get(),
            dataSource = get(),
            targetRestarter = get(),
            preferences = SharedPreferencesOverrideControlPreferences(
                get<Context>().getSharedPreferences(
                    "override_control",
                    Context.MODE_PRIVATE,
                ),
            ),
            appliedSetupStore = get(),
        )
    }
}

private const val ANDROID_UIDS_PER_USER = 100_000
private const val ONBOARDING_DATA_STORE = "onboardingDataStore"
private const val APPLIED_RECOMMENDATION_SETUPS_DATA_STORE = "appliedRecommendationSetupsDataStore"
private const val DEMOTED_RECOMMENDATIONS_DATA_STORE = "demotedRecommendationsDataStore"
