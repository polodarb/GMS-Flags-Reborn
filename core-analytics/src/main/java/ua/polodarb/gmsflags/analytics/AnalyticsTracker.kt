package ua.polodarb.gmsflags.analytics

/** Records product analytics. */
interface AnalyticsTracker {
    fun track(event: AnalyticsEvent)

    fun setUserProperty(property: AnalyticsUserProperty, value: String)

    /** Reflects the user's consent; when false, nothing is collected. */
    fun setCollectionEnabled(enabled: Boolean)
}

enum class AnalyticsUserProperty(val key: String) {
    RootAvailable("root_available"),
    XposedModuleActive("xposed_module_active"),
}
