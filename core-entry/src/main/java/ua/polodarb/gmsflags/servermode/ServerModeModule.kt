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
import ua.polodarb.gmsflags.remoteconfig.RemoteFetch
import ua.polodarb.gmsflags.remoteconfig.RemoteFlagStore
import ua.polodarb.gmsflags.remoteconfig.SharedPreferencesRemoteFlagStore
import ua.polodarb.gmsflags.remoteconfig.StickyRemoteValue
import kotlin.coroutines.resume

private const val KEY_OFFLINE_MODE = "android_offline_mode"
private const val KEY_OFFLINE_MODE_CACHE = "offline_mode_json"
private const val DEBUG_MINIMUM_FETCH_INTERVAL_SECONDS = 0L

val serverModeModule = module {
    single<RemoteFlagStore> { SharedPreferencesRemoteFlagStore(androidContext()) }
    single<ServerModeRepository> {
        val remoteConfig = Firebase.remoteConfig.withDebugFetchInterval()
        DefaultServerModeRepository(
            sticky = StickyRemoteValue(
                key = KEY_OFFLINE_MODE_CACHE,
                store = get(),
                fetch = { remoteConfig.fetchOfflineModePayload() },
                parse = ServerModeJson::parse,
            ),
        )
    }
}

internal fun FirebaseRemoteConfig.withDebugFetchInterval(): FirebaseRemoteConfig = apply {
    if (!BuildConfig.DEBUG) return@apply
    setConfigSettingsAsync(
        remoteConfigSettings {
            minimumFetchIntervalInSeconds = DEBUG_MINIMUM_FETCH_INTERVAL_SECONDS
        },
    )
}

private suspend fun FirebaseRemoteConfig.fetchOfflineModePayload(): RemoteFetch {
    val fetched = suspendCancellableCoroutine { continuation ->
        fetchAndActivate().addOnCompleteListener { task ->
            if (continuation.isActive) continuation.resume(task.isSuccessful)
        }
    }
    if (!fetched) return RemoteFetch.Failed
    return RemoteFetch.Fetched(getString(KEY_OFFLINE_MODE).trim())
}
