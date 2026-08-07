package ua.polodarb.gmsflags.presentation.feature.hookstatus.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import ua.polodarb.gmsflags.domain.hookstatus.HookApplicationStatus
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing
import ua.polodarb.gmsflags.presentation.feature.hookstatus.ui.HookStatusDimensions
import ua.polodarb.gmsflags.presentation.feature.hookstatus.ui.hookHealthVisuals

@Composable
internal fun HookStatusHero(
    status: HookApplicationStatus,
    modifier: Modifier = Modifier,
) {
    val visuals = hookHealthVisuals(status.health)
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Column(
            modifier = Modifier.padding(GmsSpacing.Large),
            verticalArrangement = Arrangement.spacedBy(GmsSpacing.Large),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(GmsSpacing.ExtraSmall)) {
                Text(
                    text = status.applicationName,
                    style = MaterialTheme.typography.headlineSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = status.androidPackageName,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                color = visuals.containerColor,
                contentColor = visuals.contentColor,
            ) {
                Row(
                    modifier = Modifier.padding(
                        horizontal = GmsSpacing.Large,
                        vertical = GmsSpacing.Medium,
                    ),
                    horizontalArrangement = Arrangement.spacedBy(GmsSpacing.Medium),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = visuals.icon,
                        contentDescription = null,
                        modifier = Modifier.size(HookStatusDimensions.HeroIcon),
                    )
                    Text(
                        text = visuals.label,
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
        }
    }
}
