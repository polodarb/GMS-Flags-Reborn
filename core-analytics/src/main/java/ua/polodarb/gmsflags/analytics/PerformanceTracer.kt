package ua.polodarb.gmsflags.analytics

/** Custom performance traces around slow paths (flag reads, feed load, apply). */
interface PerformanceTracer {
    fun start(name: String): PerformanceTrace

    /** Reflects the user's consent; when false, nothing is collected. */
    fun setCollectionEnabled(enabled: Boolean)
}

interface PerformanceTrace {
    fun putMetric(name: String, value: Long)
    fun stop()
}

/** Runs [block] inside a trace, stopping it even if the block throws. */
inline fun <T> PerformanceTracer.trace(name: String, block: () -> T): T {
    val trace = start(name)
    try {
        return block()
    } finally {
        trace.stop()
    }
}
