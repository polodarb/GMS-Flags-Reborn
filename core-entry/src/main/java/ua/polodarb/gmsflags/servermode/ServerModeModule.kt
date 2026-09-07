package ua.polodarb.gmsflags.servermode

import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfig
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import ua.polodarb.gmsflags.data.repository.servermode.ServerModeRepository
import kotlin.coroutines.resume

private const val KEY_OFFLINE_MODE = "android_offline_mode"

val serverModeModule = module {
    single<ServerModeStore> { SharedPreferencesServerModeStore(androidContext()) }
    single<ServerModeRepository> {
        DefaultServerModeRepository(
            store = get(),
            fetchRawConfig = { Firebase.remoteConfig.fetchOfflineModePayload() },
        )
    }
}

private suspend fun FirebaseRemoteConfig.fetchOfflineModePayload(): String? {
    suspendCancellableCoroutine { continuation ->
        fetchAndActivate().addOnCompleteListener {
            if (continuation.isActive) continuation.resume(Unit)
        }
    }
    return getString(KEY_OFFLINE_MODE).takeIf { it.isNotBlank() }
}
