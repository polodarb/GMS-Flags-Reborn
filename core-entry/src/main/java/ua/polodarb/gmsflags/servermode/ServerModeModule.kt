package ua.polodarb.gmsflags.servermode

import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import ua.polodarb.gmsflags.BuildConfig
import ua.polodarb.gmsflags.analytics.CrashReporter
import ua.polodarb.gmsflags.data.repository.servermode.ServerModeRepository
import ua.polodarb.gmsflags.remoteconfig.FetchFailure
import ua.polodarb.gmsflags.remoteconfig.REMOTE_CONFIG_LOG_PREFIX
import ua.polodarb.gmsflags.remoteconfig.RemoteFetch
import ua.polodarb.gmsflags.remoteconfig.RemoteFlagStore
import ua.polodarb.gmsflags.remoteconfig.SharedPreferencesRemoteFlagStore
import ua.polodarb.gmsflags.remoteconfig.StickyRemoteValue
import kotlin.coroutines.resume

internal const val KEY_OFFLINE_MODE = "android_offline_mode"
private const val KEY_OFFLINE_MODE_CACHE = "offline_mode_json"
private const val DEBUG_MINIMUM_FETCH_INTERVAL_SECONDS = 0L

val serverModeModule = module {
    single<RemoteFlagStore> { SharedPreferencesRemoteFlagStore(androidContext()) }
    single<FirebaseRemoteConfig> { Firebase.remoteConfig.withDebugFetchInterval() }
    single<ServerModeRepository> {
        val remoteConfig: FirebaseRemoteConfig = get()
        val crashReporter: CrashReporter = get()
        DefaultServerModeRepository(
            sticky = StickyRemoteValue(
                key = KEY_OFFLINE_MODE_CACHE,
                store = get(),
                fetch = { remoteConfig.fetchOfflineModePayload() },
                parse = { raw ->
                    ServerModeJson.parse(raw) { error ->
                        crashReporter.log("$REMOTE_CONFIG_LOG_PREFIX $KEY_OFFLINE_MODE payload unreadable")
                        crashReporter.recordException(error)
                    }
                },
                onFetchFailure = { failure ->
                    crashReporter.log("$REMOTE_CONFIG_LOG_PREFIX $KEY_OFFLINE_MODE fetch failed")
                    when (failure) {
                        is FetchFailure.Thrown -> crashReporter.recordException(failure.error)
                        FetchFailure.Reported -> Unit
                    }
                },
            ),
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

private suspend fun FirebaseRemoteConfig.fetchOfflineModePayload(): RemoteFetch {
    val fetched = suspendCancellableCoroutine { continuation ->
        fetchAndActivate().addOnCompleteListener { task ->
            if (continuation.isActive) continuation.resume(task.isSuccessful)
        }
    }
    if (!fetched) return RemoteFetch.Failed
    return RemoteFetch.Fetched(getString(KEY_OFFLINE_MODE).trim())
}
