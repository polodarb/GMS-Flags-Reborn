package ua.polodarb.gmsflags.presentation.core.ui.snackbar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.rounded.CheckCircleOutline
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import ua.polodarb.gmsflags.presentation.core.message.UiMessageType
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsDimensions
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing

@Composable
fun GmsSnackbarHost(
    hostState: GmsSnackbarHostState,
    modifier: Modifier = Modifier,
) {
    var displayedMessage by remember { mutableStateOf<GmsSnackbarMessage?>(null) }
    val visibility = remember { MutableTransitionState(false) }
    val haptic = LocalHapticFeedback.current
    var dismissRequested by remember { mutableStateOf(false) }

    LaunchedEffect(hostState) {
        for (message in hostState.messages) {
            if (message.type == UiMessageType.Error) {
                haptic.performHapticFeedback(HapticFeedbackType.Reject)
            }
            displayedMessage = message
            visibility.targetState = true
            dismissRequested = false
            withTimeoutOrNull(message.duration.millis) {
                snapshotFlow { dismissRequested }.first { requested -> requested }
            }
            visibility.targetState = false
            snapshotFlow { visibility.isIdle && !visibility.currentState }
                .first { exitFinished -> exitFinished }
            displayedMessage = null
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        AnimatedVisibility(
            visibleState = visibility,
            enter = slideInVertically { -it } + fadeIn(),
            exit = slideOutVertically { -it } + fadeOut(),
        ) {
            displayedMessage?.let { message ->
                GmsSnackbar(message, onActionClick = { dismissRequested = true })
            }
        }
    }
}

@Composable
private fun GmsSnackbar(message: GmsSnackbarMessage, onActionClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .statusBarsPadding()
            .padding(horizontal = GmsSpacing.Large, vertical = GmsSpacing.Large)
            .widthIn(max = GmsDimensions.SnackbarMaxWidth)
            .fillMaxWidth(),
        shape = RoundedCornerShape(GmsDimensions.SnackbarCornerRadius),
        color = MaterialTheme.colorScheme.inverseSurface,
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = GmsSpacing.Large,
                vertical = GmsSpacing.Medium,
            ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = when (message.type) {
                    UiMessageType.Info -> Icons.Outlined.Info
                    UiMessageType.Success -> Icons.Rounded.CheckCircleOutline
                    UiMessageType.Warning -> Icons.Rounded.WarningAmber
                    UiMessageType.Error -> Icons.Rounded.ErrorOutline
                },
                contentDescription = null,
                modifier = Modifier.size(GmsDimensions.SnackbarIconSize),
                tint = MaterialTheme.colorScheme.inverseOnSurface,
            )
            Spacer(Modifier.width(GmsSpacing.Medium))
            Text(
                text = message.text,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.inverseOnSurface,
                style = MaterialTheme.typography.labelLarge,
            )
            if (message.actionLabel != null) {
                TextButton(
                    onClick = {
                        message.onAction?.invoke()
                        onActionClick()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.inversePrimary,
                    ),
                ) {
                    Text(message.actionLabel, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}
