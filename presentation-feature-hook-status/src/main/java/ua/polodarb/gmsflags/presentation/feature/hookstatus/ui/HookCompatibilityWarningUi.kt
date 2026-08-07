package ua.polodarb.gmsflags.presentation.feature.hookstatus.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import ua.polodarb.gmsflags.domain.hookstatus.HookCompatibilityWarning
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing
import ua.polodarb.gmsflags.presentation.feature.hookstatus.R

internal fun HookCompatibilityWarning.titleResource(): Int = when (this) {
    HookCompatibilityWarning.PairipCore -> R.string.hook_status_pairip_title
}

internal fun HookCompatibilityWarning.descriptionResource(): Int = when (this) {
    HookCompatibilityWarning.PairipCore -> R.string.hook_status_pairip_support
}

@Composable
internal fun HookCompatibilityWarningBadge(
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .height(HookStatusDimensions.StatusBadgeHeight)
            .clip(MaterialTheme.shapes.small),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = GmsSpacing.Medium,
                vertical = GmsSpacing.ExtraSmall,
            ),
            horizontalArrangement = Arrangement.spacedBy(GmsSpacing.Small),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.WarningAmber,
                contentDescription = null,
                modifier = Modifier.size(HookStatusDimensions.CompactIcon),
            )
            Text(
                text = stringResource(R.string.hook_status_compatibility_badge),
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

@Composable
internal fun HookCompatibilityWarningNotice(
    warning: HookCompatibilityWarning,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
    ) {
        Row(
            modifier = Modifier.padding(GmsSpacing.Large),
            horizontalArrangement = Arrangement.spacedBy(GmsSpacing.Medium),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(imageVector = Icons.Outlined.WarningAmber, contentDescription = null)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(GmsSpacing.ExtraSmall),
            ) {
                Text(
                    text = stringResource(warning.titleResource()),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = stringResource(warning.descriptionResource()),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}
