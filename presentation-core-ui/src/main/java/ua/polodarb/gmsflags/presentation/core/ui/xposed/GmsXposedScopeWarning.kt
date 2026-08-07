package ua.polodarb.gmsflags.presentation.core.ui.xposed

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsDimensions
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing

@Composable
fun GmsScopeWarningVisibility(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = scopeWarningEnterTransition(),
        exit = scopeWarningExitTransition(),
    ) {
        content()
    }
}

@Composable
fun GmsXposedScopeWarningBadge(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.error,
        contentColor = MaterialTheme.colorScheme.onError,
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = GmsSpacing.Medium,
                vertical = GmsSpacing.Small,
            ),
            horizontalArrangement = Arrangement.spacedBy(GmsSpacing.Small),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                modifier = Modifier.size(GmsDimensions.SelectionIconSize),
            )
            Text(text = label, style = MaterialTheme.typography.labelLarge)
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier.size(GmsDimensions.SelectionIconSize),
            )
        }
    }
}

@Composable
fun GmsXposedScopeActionChip(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.error,
        contentColor = MaterialTheme.colorScheme.onError,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = GmsSpacing.Large,
                    vertical = GmsSpacing.Medium,
                ),
            horizontalArrangement = Arrangement.spacedBy(GmsSpacing.Small),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                modifier = Modifier.size(GmsDimensions.SelectionIconSize),
            )
            Text(
                text = label,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelLarge,
            )
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier.size(GmsDimensions.SelectionIconSize),
            )
        }
    }
}

private fun scopeWarningEnterTransition(): EnterTransition =
    fadeIn(animationSpec = tween(SCOPE_WARNING_ENTER_MILLIS)) +
        expandVertically(animationSpec = tween(SCOPE_WARNING_ENTER_MILLIS))

private fun scopeWarningExitTransition(): ExitTransition =
    fadeOut(animationSpec = tween(SCOPE_WARNING_EXIT_MILLIS)) +
        shrinkVertically(animationSpec = tween(SCOPE_WARNING_EXIT_MILLIS))

private const val SCOPE_WARNING_ENTER_MILLIS = 220
private const val SCOPE_WARNING_EXIT_MILLIS = 160
