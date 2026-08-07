package ua.polodarb.gmsflags.analytics

import com.google.firebase.crashlytics.FirebaseCrashlytics

internal class FirebaseCrashReporter(
    private val crashlytics: FirebaseCrashlytics,
) : CrashReporter {
    override fun log(message: String) = crashlytics.log(message)
    override fun recordException(throwable: Throwable) = crashlytics.recordException(throwable)
    override fun setCustomKey(key: String, value: String) = crashlytics.setCustomKey(key, value)
    override fun setCollectionEnabled(enabled: Boolean) =
        crashlytics.setCrashlyticsCollectionEnabled(enabled)
}
