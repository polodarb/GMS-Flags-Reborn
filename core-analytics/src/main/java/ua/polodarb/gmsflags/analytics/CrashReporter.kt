package ua.polodarb.gmsflags.analytics

/** Non-fatal logging and crash reporting. */
interface CrashReporter {
    fun log(message: String)

    fun recordException(throwable: Throwable)

    fun setCustomKey(key: String, value: String)

    /** Reflects the user's consent; when false, nothing is collected. */
    fun setCollectionEnabled(enabled: Boolean)
}
