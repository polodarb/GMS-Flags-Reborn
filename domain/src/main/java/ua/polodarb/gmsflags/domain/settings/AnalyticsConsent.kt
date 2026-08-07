package ua.polodarb.gmsflags.domain.settings

import kotlinx.coroutines.flow.StateFlow

/** Whether the user allows analytics/crash/performance collection. Default is on (opt-out). */
fun interface ObserveAnalyticsConsent {
    operator fun invoke(): StateFlow<Boolean>
}

fun interface SetAnalyticsConsent {
    operator fun invoke(enabled: Boolean)
}
