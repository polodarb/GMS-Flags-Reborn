package ua.polodarb.gmsflags.data.repository.settings.repository

import kotlinx.coroutines.flow.StateFlow

interface AnalyticsConsentRepository {
    val enabled: StateFlow<Boolean>
    fun setEnabled(enabled: Boolean)
}
