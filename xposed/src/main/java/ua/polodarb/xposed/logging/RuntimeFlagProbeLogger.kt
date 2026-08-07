package ua.polodarb.xposed.logging

import java.util.concurrent.atomic.AtomicInteger

internal class RuntimeFlagProbeLogger(
    private val prefix: String,
) {
    private val count = AtomicInteger(0)

    fun log(message: String) {
        if (count.getAndIncrement() < MAX_LOGS) {
            XposedLogger.logD("$prefix: $message")
        }
    }

    private companion object {
        private const val MAX_LOGS = 12
    }
}
