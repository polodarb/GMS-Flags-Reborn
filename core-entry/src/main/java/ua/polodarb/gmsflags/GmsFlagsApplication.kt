package ua.polodarb.gmsflags

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.core.Koin
import org.koin.core.context.startKoin
import org.koin.core.qualifier.named
import ua.polodarb.gmsflags.analytics.AnalyticsTracker
import ua.polodarb.gmsflags.analytics.CrashReporter
import ua.polodarb.gmsflags.analytics.PerformanceTracer
import ua.polodarb.gmsflags.data.network.APP_SIGNATURE_SHA256_QUALIFIER_NAME
import ua.polodarb.gmsflags.di.appModules
import ua.polodarb.gmsflags.domain.settings.ObserveAnalyticsConsent

class GmsFlagsApplication : Application() {
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        val koin = startKoin {
            androidContext(this@GmsFlagsApplication)
            modules(appModules)
        }.koin
        applyAnalyticsConsent(koin)
        reportAppSignatureTrust(koin)
    }

    private fun applyAnalyticsConsent(koin: Koin) {
        val observeConsent: ObserveAnalyticsConsent = koin.get()
        val tracker: AnalyticsTracker = koin.get()
        val crashReporter: CrashReporter = koin.get()
        val performanceTracer: PerformanceTracer = koin.get()
        appScope.launch {
            observeConsent().collectLatest { enabled ->
                tracker.setCollectionEnabled(enabled)
                crashReporter.setCollectionEnabled(enabled)
                performanceTracer.setCollectionEnabled(enabled)
            }
        }
    }

    private fun reportAppSignatureTrust(koin: Koin) {
        val crashReporter: CrashReporter = koin.get()
        val actualSignature: String? =
            koin.get(qualifier = named(APP_SIGNATURE_SHA256_QUALIFIER_NAME))
        crashReporter.setCustomKey(
            "app_signature_official",
            officialBuildStatus(BuildConfig.TRUSTED_APP_SIGNATURE_SHA256, actualSignature),
        )
    }
}

/**
 * "unknown" - nothing to compare (blank trusted constant, or the signature couldn't be read).
 * "true"    - the build's own signature matches the trusted release certificate.
 * "false"   - present but different, e.g. a debug build or a repackaged/re-signed one.
 */
internal fun officialBuildStatus(trusted: String?, actual: String?): String = when {
    trusted.isNullOrBlank() || actual.isNullOrBlank() -> "unknown"
    actual.equals(trusted, ignoreCase = true) -> "true"
    else -> "false"
}
