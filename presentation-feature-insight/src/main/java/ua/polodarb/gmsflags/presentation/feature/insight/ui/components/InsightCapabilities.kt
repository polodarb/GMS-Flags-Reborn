package ua.polodarb.gmsflags.presentation.feature.insight.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.ImageSearch
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing
import ua.polodarb.gmsflags.presentation.feature.insight.R

@Composable
internal fun InsightCapabilities() {
    Column(verticalArrangement = Arrangement.spacedBy(GmsSpacing.Medium)) {
        Text(
            text = stringResource(R.string.insight_what_it_does),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = GmsSpacing.Small),
        )
        Capability(
            icon = Icons.Outlined.Flag,
            title = stringResource(R.string.insight_capability_flags_title),
            description = stringResource(R.string.insight_capability_flags_description),
            badge = stringResource(R.string.insight_badge_soon),
        )
        Capability(
            icon = Icons.Outlined.ImageSearch,
            title = stringResource(R.string.insight_capability_changes_title),
            description = stringResource(R.string.insight_capability_changes_description),
        )
        Capability(
            icon = Icons.Outlined.Description,
            title = stringResource(R.string.insight_capability_manifest_title),
            description = stringResource(R.string.insight_capability_manifest_description),
        )
    }
}

@Composable
private fun Capability(
    icon: ImageVector,
    title: String,
    description: String,
    badge: String? = null,
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = MaterialTheme.shapes.large,
    ) {
        Row(
            modifier = Modifier.padding(GmsSpacing.Medium),
            horizontalArrangement = Arrangement.spacedBy(GmsSpacing.Medium),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(GmsSpacing.ExtraSmall)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(GmsSpacing.Small),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = title, style = MaterialTheme.typography.titleMedium)
                    if (badge != null) {
                        SoonBadge(badge)
                    }
                }
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun SoonBadge(text: String) {
    Text(
        text = text,
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.tertiaryContainer)
            .padding(horizontal = GmsSpacing.Small, vertical = GmsSpacing.Micro),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onTertiaryContainer,
    )
}
