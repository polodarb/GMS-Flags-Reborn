package ua.polodarb.gmsflags.presentation.feature.hookstatus.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import ua.polodarb.gmsflags.domain.hookstatus.HookHealth
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing

@Composable
internal fun HookHealthBadge(
    health: HookHealth,
    modifier: Modifier = Modifier,
    showIcon: Boolean = true,
) {
    val visuals = hookHealthVisuals(health)
    Surface(
        modifier = modifier
            .height(HookStatusDimensions.StatusBadgeHeight)
            .clip(MaterialTheme.shapes.small),
        shape = MaterialTheme.shapes.small,
        color = visuals.containerColor,
        contentColor = visuals.contentColor,
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = GmsSpacing.Medium,
                vertical = GmsSpacing.ExtraSmall,
            ),
            horizontalArrangement = Arrangement.spacedBy(GmsSpacing.Small),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (showIcon) {
                Icon(
                    imageVector = visuals.icon,
                    contentDescription = null,
                    modifier = Modifier.size(HookStatusDimensions.CompactIcon),
                )
            }
            Text(
                text = visuals.label,
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}
