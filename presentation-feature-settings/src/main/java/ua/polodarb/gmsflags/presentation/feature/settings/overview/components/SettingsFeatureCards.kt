package ua.polodarb.gmsflags.presentation.feature.settings.overview.components

import android.content.res.Resources
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.outlined.DataObject
import androidx.compose.material.icons.outlined.MonitorHeart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ua.polodarb.gmsflags.presentation.core.ui.haptic.rememberHapticClick
import ua.polodarb.gmsflags.domain.settings.OverrideControlState
import ua.polodarb.gmsflags.presentation.core.error.UiError
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing
import ua.polodarb.gmsflags.presentation.core.ui.state.localizedDescription
import ua.polodarb.gmsflags.presentation.feature.settings.R
import ua.polodarb.gmsflags.presentation.feature.settings.overview.SettingsState

@Composable
internal fun HookStatusCard(
    state: SettingsState,
    resources: Resources,
    onClick: () -> Unit,
) {
    val overview = state.hookStatus
    val status = when {
        state.hookStatusLoading -> FeatureStatus(
            stringResource(R.string.settings_hook_status_checking_description),
            MaterialTheme.colorScheme.onSurfaceVariant,
        )
        state.hookStatusError != null -> FeatureStatus(
            state.hookStatusError.localizedDescription(resources),
            MaterialTheme.colorScheme.error,
        )
        overview == null || !overview.moduleObserved -> FeatureStatus(
            stringResource(R.string.settings_hook_status_waiting_description),
            MaterialTheme.colorScheme.error,
        )
        overview.attentionApplicationCount > 0 -> FeatureStatus(
            stringResource(
                R.string.settings_hook_status_summary,
                overview.workingApplicationCount,
                overview.attentionApplicationCount,
            ),
            MaterialTheme.colorScheme.error,
        )
        else -> FeatureStatus(
            stringResource(
                R.string.settings_hook_status_working_count,
                overview.workingApplicationCount,
            ),
            MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }

    SettingsFeatureCard(
        title = stringResource(R.string.settings_hook_status),
        description = status.description,
        descriptionColor = status.descriptionColor,
        icon = Icons.Outlined.MonitorHeart,
        onClick = onClick,
    )
}

@Composable
internal fun OverridesStorageCard(
    overrideControl: OverrideControlState,
    error: UiError?,
    resources: Resources,
    onClick: () -> Unit,
) {
    val descriptionColor = if (error != null) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    SettingsFeatureCard(
        title = stringResource(R.string.settings_overrides_storage),
        description = error?.localizedDescription(resources)
            ?: stringResource(
                R.string.settings_overrides_storage_description,
                overrideControl.overrideCount,
            ),
        descriptionColor = descriptionColor,
        icon = Icons.Outlined.DataObject,
        onClick = onClick,
    )
}

@Composable
internal fun FaqCard(onClick: () -> Unit) {
    SettingsFeatureCard(
        title = stringResource(R.string.settings_faq),
        description = stringResource(R.string.settings_faq_description),
        descriptionColor = MaterialTheme.colorScheme.onSurfaceVariant,
        icon = Icons.AutoMirrored.Rounded.HelpOutline,
        onClick = onClick,
    )
}

@Composable
private fun SettingsFeatureCard(
    title: String,
    description: String,
    descriptionColor: Color,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    Surface(
        onClick = rememberHapticClick(onClick = onClick),
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
                alignment = Alignment.TopCenter,
            ),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Row(
            modifier = Modifier.padding(GmsSpacing.Large),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(15.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(Modifier.width(GmsSpacing.Medium))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(GmsSpacing.ExtraSmall),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = descriptionColor,
                )
            }
            Spacer(Modifier.width(GmsSpacing.Medium))
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private data class FeatureStatus(
    val description: String,
    val descriptionColor: Color,
)
