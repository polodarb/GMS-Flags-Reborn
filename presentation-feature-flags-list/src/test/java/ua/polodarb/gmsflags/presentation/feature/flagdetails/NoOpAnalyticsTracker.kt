package ua.polodarb.gmsflags.presentation.feature.flagdetails

import ua.polodarb.gmsflags.analytics.AnalyticsEvent
import ua.polodarb.gmsflags.analytics.AnalyticsTracker
import ua.polodarb.gmsflags.analytics.AnalyticsUserProperty

internal object NoOpAnalyticsTracker : AnalyticsTracker {
    override fun track(event: AnalyticsEvent) {}
    override fun setUserProperty(property: AnalyticsUserProperty, value: String) {}
    override fun setCollectionEnabled(enabled: Boolean) {}
}
