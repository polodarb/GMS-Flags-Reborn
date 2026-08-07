package ua.polodarb.gmsflags.domain.settings

import kotlinx.coroutines.flow.StateFlow
import ua.polodarb.gmsflags.data.repository.settings.repository.AnalyticsConsentRepository

class ObserveAnalyticsConsentUseCase(
    private val repository: AnalyticsConsentRepository,
) : ObserveAnalyticsConsent {
    override fun invoke(): StateFlow<Boolean> = repository.enabled
}

class SetAnalyticsConsentUseCase(
    private val repository: AnalyticsConsentRepository,
) : SetAnalyticsConsent {
    override fun invoke(enabled: Boolean) = repository.setEnabled(enabled)
}
