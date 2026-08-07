package ua.polodarb.gmsflags.analytics

/** Used in debug builds and tests: honours the interfaces, collects nothing. */
object NoOpAnalyticsTracker : AnalyticsTracker {
    override fun track(event: AnalyticsEvent) = Unit
    override fun setUserProperty(property: AnalyticsUserProperty, value: String) = Unit
    override fun setCollectionEnabled(enabled: Boolean) = Unit
}

object NoOpCrashReporter : CrashReporter {
    override fun log(message: String) = Unit
    override fun recordException(throwable: Throwable) = Unit
    override fun setCustomKey(key: String, value: String) = Unit
    override fun setCollectionEnabled(enabled: Boolean) = Unit
}

object NoOpPerformanceTracer : PerformanceTracer {
    override fun start(name: String): PerformanceTrace = NoOpTrace
    override fun setCollectionEnabled(enabled: Boolean) = Unit

    private object NoOpTrace : PerformanceTrace {
        override fun putMetric(name: String, value: Long) = Unit
        override fun stop() = Unit
    }
}
