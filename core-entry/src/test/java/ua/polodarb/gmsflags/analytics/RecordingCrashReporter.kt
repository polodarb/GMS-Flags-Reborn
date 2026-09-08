package ua.polodarb.gmsflags.analytics

class RecordingCrashReporter : CrashReporter {
    val loggedMessages = mutableListOf<String>()
    val recordedExceptions = mutableListOf<Throwable>()

    override fun log(message: String) {
        loggedMessages.add(message)
    }

    override fun recordException(throwable: Throwable) {
        recordedExceptions.add(throwable)
    }

    override fun setCustomKey(key: String, value: String) = Unit

    override fun setCollectionEnabled(enabled: Boolean) = Unit
}
