package ua.polodarb.gmsflags.update

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import ua.polodarb.gmsflags.BuildConfig
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import ua.polodarb.gmsflags.data.repository.update.UpdatePolicyRepository
import ua.polodarb.gmsflags.domain.update.AppUpdatePolicy
import ua.polodarb.gmsflags.domain.update.AppUpdateType
import ua.polodarb.gmsflags.domain.update.DismissSoftAppUpdate
import ua.polodarb.gmsflags.domain.update.DismissSoftAppUpdateUseCase
import ua.polodarb.gmsflags.domain.update.GetAppUpdatePolicy
import ua.polodarb.gmsflags.domain.update.GetAppUpdatePolicyUseCase

val updateModule = module {
    single<UpdatePolicyRepository> {
        FirebaseUpdatePolicyRepository(get<FirebaseRemoteConfig>(), androidContext())
    }
    single<GetAppUpdatePolicy> { GetAppUpdatePolicyUseCase(get()) }
    single<DismissSoftAppUpdate> { DismissSoftAppUpdateUseCase(get()) }
    viewModel { AppUpdateViewModel(get(), get(), debugForcedNoticePolicy()) }
}

/** Debug-only preview of the update notice while there is no matching remote config. */
private const val DEBUG_FORCE_UPDATE_NOTICE = true

private fun debugForcedNoticePolicy(): AppUpdatePolicy? {
    if (!BuildConfig.DEBUG || !DEBUG_FORCE_UPDATE_NOTICE) return null
    return AppUpdatePolicy(
        policyId = "debug-forced-notice",
        title = "Update available",
        description = "GMS Flags 1.2.0 is ready to install.",
        updateUrl = "https://github.com/polodarb/GMS-Flags-Reborn/releases/latest",
        type = AppUpdateType.SOFT,
        softCooldownHours = 24,
        primaryButtonTitle = "Update",
        secondaryButtonTitle = "Later",
    )
}
