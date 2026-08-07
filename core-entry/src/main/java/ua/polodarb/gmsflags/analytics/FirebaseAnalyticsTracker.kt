package ua.polodarb.gmsflags.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

internal class FirebaseAnalyticsTracker(
    private val firebaseAnalytics: FirebaseAnalytics,
) : AnalyticsTracker {
    override fun track(event: AnalyticsEvent) {
        firebaseAnalytics.logEvent(event.name, event.params.toBundle())
    }

    override fun setUserProperty(property: AnalyticsUserProperty, value: String) {
        firebaseAnalytics.setUserProperty(property.key, value)
    }

    override fun setCollectionEnabled(enabled: Boolean) {
        firebaseAnalytics.setAnalyticsCollectionEnabled(enabled)
    }

    private fun Map<String, Any>.toBundle(): Bundle = Bundle().apply {
        forEach { (key, value) ->
            when (value) {
                is String -> putString(key, value)
                is Int -> putLong(key, value.toLong())
                is Long -> putLong(key, value)
                is Double -> putDouble(key, value)
                is Float -> putDouble(key, value.toDouble())
                is Boolean -> putString(key, value.toString())
                else -> putString(key, value.toString())
            }
        }
    }
}
