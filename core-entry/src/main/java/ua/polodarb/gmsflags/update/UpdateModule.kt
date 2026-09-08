package ua.polodarb.gmsflags.update

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import ua.polodarb.gmsflags.data.repository.update.UpdatePolicyRepository
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
    viewModel { AppUpdateViewModel(get(), get()) }
}
