package ua.polodarb.gmsflags.presentation.feature.settings.overview.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import ua.polodarb.gmsflags.presentation.core.ui.branding.GmsBrandGenerationPlacement
import ua.polodarb.gmsflags.presentation.core.ui.branding.GmsBrandWordmark

@Composable
internal fun SettingsBrandLockup(modifier: Modifier = Modifier) {
    GmsBrandWordmark(
        modifier = modifier,
        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
        generationColor = MaterialTheme.colorScheme.primary,
        generationPlacement = GmsBrandGenerationPlacement.Inline,
    )
}
