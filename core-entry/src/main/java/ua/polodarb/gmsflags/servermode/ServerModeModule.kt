package ua.polodarb.gmsflags.servermode

import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import ua.polodarb.gmsflags.BuildConfig
import ua.polodarb.gmsflags.data.repository.servermode.ServerModeRepository
import kotlin.coroutines.resume

private const val KEY_OFFLINE_MODE = "android_offline_mode"
private const val DEBUG_MINIMUM_FETCH_INTERVAL_SECONDS = 0L

val serverModeModule = module {
    single<ServerModeStore> { SharedPreferencesServerModeStore(androidContext()) }
    single<ServerModeRepository> {
        val remoteConfig = Firebase.remoteConfig.withDebugFetchInterval()
        DefaultServerModeRepository(
            store = get(),
            fetchRawConfig = { remoteConfig.fetchOfflineModePayload() },
        )
    }
}

private fun FirebaseRemoteConfig.withDebugFetchInterval(): FirebaseRemoteConfig = apply {
    if (!BuildConfig.DEBUG) return@apply
    setConfigSettingsAsync(
        remoteConfigSettings {
            minimumFetchIntervalInSeconds = DEBUG_MINIMUM_FETCH_INTERVAL_SECONDS
        },
    )
}

private suspend fun FirebaseRemoteConfig.fetchOfflineModePayload(): ServerModeFetch {
    val fetched = suspendCancellableCoroutine { continuation ->
        fetchAndActivate().addOnCompleteListener { task ->
            if (continuation.isActive) continuation.resume(task.isSuccessful)
        }
    }
    if (!fetched) return ServerModeFetch.Failed
    return ServerModeFetch.Fetched(getString(KEY_OFFLINE_MODE).trim())
}
