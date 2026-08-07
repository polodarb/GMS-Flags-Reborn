package ua.polodarb.gmsflags.analytics

import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace

internal class FirebasePerformanceTracer(
    private val performance: FirebasePerformance,
) : PerformanceTracer {
    override fun start(name: String): PerformanceTrace {
        val trace = performance.newTrace(name).apply { start() }
        return FirebaseTrace(trace)
    }

    override fun setCollectionEnabled(enabled: Boolean) {
        performance.isPerformanceCollectionEnabled = enabled
    }

    private class FirebaseTrace(private val trace: Trace) : PerformanceTrace {
        override fun putMetric(name: String, value: Long) = trace.putMetric(name, value)
        override fun stop() = trace.stop()
    }
}
