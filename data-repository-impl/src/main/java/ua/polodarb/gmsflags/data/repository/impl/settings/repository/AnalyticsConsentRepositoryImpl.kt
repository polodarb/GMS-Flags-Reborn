package ua.polodarb.gmsflags.data.repository.impl.settings.repository

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ua.polodarb.gmsflags.data.repository.settings.repository.AnalyticsConsentRepository

internal class AnalyticsConsentRepositoryImpl(context: Context) : AnalyticsConsentRepository {
    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    private val mutable = MutableStateFlow(prefs.getBoolean(KEY, DEFAULT_ENABLED))
    override val enabled: StateFlow<Boolean> = mutable.asStateFlow()

    override fun setEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY, enabled).apply()
        mutable.value = enabled
    }

    private companion object {
        const val PREFS = "analytics_consent"
        const val KEY = "enabled"
        const val DEFAULT_ENABLED = true
    }
}
