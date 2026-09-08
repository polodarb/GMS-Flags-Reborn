package ua.polodarb.gmsflags.navigationflags

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.dsl.module
import ua.polodarb.gmsflags.analytics.CrashReporter
import ua.polodarb.gmsflags.data.repository.navigation.NavigationFlagsRepository
import ua.polodarb.gmsflags.remoteconfig.FetchFailure
import ua.polodarb.gmsflags.remoteconfig.REMOTE_CONFIG_LOG_PREFIX
import ua.polodarb.gmsflags.remoteconfig.RemoteFetch
import ua.polodarb.gmsflags.remoteconfig.StickyRemoteValue
import kotlin.coroutines.resume

internal const val KEY_HIDE_GMS_INSIGHT = "android_hide_gms_insight"
private const val KEY_HIDE_GMS_INSIGHT_CACHE = "gms_insight_hidden"

val navigationFlagsModule = module {
    single<NavigationFlagsRepository> {
        val remoteConfig: FirebaseRemoteConfig = get()
        val crashReporter: CrashReporter = get()
        DefaultNavigationFlagsRepository(
            sticky = StickyRemoteValue(
                key = KEY_HIDE_GMS_INSIGHT_CACHE,
                store = get(),
                fetch = { remoteConfig.fetchGmsInsightHiddenFlag() },
                parse = ::parseGmsInsightHidden,
                onFetchFailure = { failure ->
                    crashReporter.log("$REMOTE_CONFIG_LOG_PREFIX $KEY_HIDE_GMS_INSIGHT fetch failed")
                    when (failure) {
                        is FetchFailure.Thrown -> crashReporter.recordException(failure.error)
                        FetchFailure.Reported -> Unit
                    }
                },
            ),
        )
    }
}

private suspend fun FirebaseRemoteConfig.fetchGmsInsightHiddenFlag(): RemoteFetch {
    val fetched = suspendCancellableCoroutine { continuation ->
        fetchAndActivate().addOnCompleteListener { task ->
            if (continuation.isActive) continuation.resume(task.isSuccessful)
        }
    }
    if (!fetched) return RemoteFetch.Failed
    return RemoteFetch.Fetched(getBoolean(KEY_HIDE_GMS_INSIGHT).toString())
}
