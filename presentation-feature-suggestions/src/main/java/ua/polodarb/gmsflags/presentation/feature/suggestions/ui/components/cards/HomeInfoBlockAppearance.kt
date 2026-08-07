package ua.polodarb.gmsflags.presentation.feature.suggestions.ui.components.cards

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import ua.polodarb.gmsflags.presentation.feature.suggestions.ui.model.HomeInfoBlockTypeUiModel

internal data class InfoBlockAppearance(
    val icon: ImageVector,
    val containerColor: Color,
    val contentColor: Color,
)

@Composable
internal fun HomeInfoBlockTypeUiModel.appearance(): InfoBlockAppearance = when (this) {
    HomeInfoBlockTypeUiModel.Info -> InfoBlockAppearance(
        Icons.Outlined.Info,
        MaterialTheme.colorScheme.secondaryContainer,
        MaterialTheme.colorScheme.onSecondaryContainer,
    )
    HomeInfoBlockTypeUiModel.Warning -> InfoBlockAppearance(
        Icons.Outlined.WarningAmber,
        MaterialTheme.colorScheme.tertiaryContainer,
        MaterialTheme.colorScheme.onTertiaryContainer,
    )
    HomeInfoBlockTypeUiModel.Success -> InfoBlockAppearance(
        Icons.Outlined.CheckCircle,
        MaterialTheme.colorScheme.primaryContainer,
        MaterialTheme.colorScheme.onPrimaryContainer,
    )
    HomeInfoBlockTypeUiModel.Promo -> InfoBlockAppearance(
        Icons.Outlined.Campaign,
        MaterialTheme.colorScheme.primaryContainer,
        MaterialTheme.colorScheme.onPrimaryContainer,
    )
    HomeInfoBlockTypeUiModel.Danger -> InfoBlockAppearance(
        Icons.Outlined.ErrorOutline,
        MaterialTheme.colorScheme.errorContainer,
        MaterialTheme.colorScheme.onErrorContainer,
    )
    HomeInfoBlockTypeUiModel.Unknown -> InfoBlockAppearance(
        Icons.Outlined.Info,
        MaterialTheme.colorScheme.surfaceContainerHigh,
        MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
