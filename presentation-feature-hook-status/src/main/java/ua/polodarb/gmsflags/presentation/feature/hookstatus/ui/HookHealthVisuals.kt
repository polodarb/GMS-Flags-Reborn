package ua.polodarb.gmsflags.presentation.feature.hookstatus.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.PauseCircleOutline
import androidx.compose.material.icons.outlined.Power
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import ua.polodarb.gmsflags.domain.hookstatus.HookHealth
import ua.polodarb.gmsflags.presentation.feature.hookstatus.R

internal data class HookHealthVisuals(
    val label: String,
    val icon: ImageVector,
    val contentColor: Color,
    val containerColor: Color,
)

@Composable
internal fun hookHealthVisuals(health: HookHealth): HookHealthVisuals {
    val scheme = MaterialTheme.colorScheme
    return when (health) {
        HookHealth.NotChecked -> HookHealthVisuals(
            stringResource(R.string.hook_status_not_checked),
            Icons.Outlined.HourglassEmpty,
            scheme.onSurfaceVariant,
            scheme.surfaceContainerHighest,
        )
        HookHealth.NoOverrides -> HookHealthVisuals(
            stringResource(R.string.hook_status_no_overrides),
            Icons.Outlined.PauseCircleOutline,
            scheme.onSurfaceVariant,
            scheme.surfaceContainerHighest,
        )
        HookHealth.Paused -> HookHealthVisuals(
            stringResource(R.string.hook_status_paused),
            Icons.Outlined.PauseCircleOutline,
            scheme.onTertiaryContainer,
            scheme.tertiaryContainer,
        )
        HookHealth.RestartRequired -> HookHealthVisuals(
            stringResource(R.string.hook_status_restart_required),
            Icons.Outlined.RestartAlt,
            scheme.onTertiary,
            scheme.tertiary,
        )
        HookHealth.HookReady -> HookHealthVisuals(
            stringResource(R.string.hook_status_hook_ready),
            Icons.Outlined.Power,
            scheme.onSecondary,
            scheme.secondary,
        )
        HookHealth.Working -> HookHealthVisuals(
            stringResource(R.string.hook_status_working),
            Icons.Outlined.Verified,
            scheme.onPrimary,
            scheme.primary,
        )
        HookHealth.Partial -> HookHealthVisuals(
            stringResource(R.string.hook_status_partial),
            Icons.Outlined.CheckCircle,
            scheme.onTertiary,
            scheme.tertiary,
        )
        HookHealth.Error -> HookHealthVisuals(
            stringResource(R.string.hook_status_error),
            Icons.Outlined.ErrorOutline,
            scheme.onError,
            scheme.error,
        )
    }
}
