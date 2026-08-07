package ua.polodarb.gmsflags.analytics

import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import com.google.firebase.crashlytics.crashlytics
import com.google.firebase.perf.performance
import org.koin.dsl.module
import ua.polodarb.gmsflags.BuildConfig

val analyticsModule = module {
    single<AnalyticsTracker> {
        if (BuildConfig.DEBUG) NoOpAnalyticsTracker
        else FirebaseAnalyticsTracker(Firebase.analytics)
    }
    single<CrashReporter> {
        if (BuildConfig.DEBUG) NoOpCrashReporter
        else FirebaseCrashReporter(Firebase.crashlytics)
    }
    single<PerformanceTracer> {
        if (BuildConfig.DEBUG) NoOpPerformanceTracer
        else FirebasePerformanceTracer(Firebase.performance)
    }
}
