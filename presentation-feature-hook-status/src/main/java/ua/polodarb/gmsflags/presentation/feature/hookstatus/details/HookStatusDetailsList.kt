package ua.polodarb.gmsflags.presentation.feature.hookstatus.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ua.polodarb.gmsflags.domain.hookstatus.HookApplicationStatus
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing
import ua.polodarb.gmsflags.presentation.feature.hookstatus.ui.HookCompatibilityWarningNotice

@Composable
internal fun HookStatusDetailsList(
    status: HookApplicationStatus,
    technicalDetailsExpanded: Boolean,
    onEvent: (HookStatusDetailsEvent) -> Unit,
    contentPadding: PaddingValues,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(GmsSpacing.Large),
    ) {
        item(key = "overview") {
            HookStatusHero(status = status)
        }
        status.compatibilityWarnings.forEach { warning ->
            item(key = "compatibility-${warning.name}") {
                HookCompatibilityWarningNotice(warning = warning)
            }
        }
        item(key = "pipeline") {
            HookPipeline(status = status)
        }
        if (status.session == null) {
            item(key = "no-session") {
                HookNoSessionNotice()
            }
        }
        item(key = "technical") {
            HookTechnicalDetails(
                status = status,
                expanded = technicalDetailsExpanded,
                onToggle = { onEvent(HookStatusDetailsEvent.ToggleTechnicalDetails) },
            )
        }
    }
}
