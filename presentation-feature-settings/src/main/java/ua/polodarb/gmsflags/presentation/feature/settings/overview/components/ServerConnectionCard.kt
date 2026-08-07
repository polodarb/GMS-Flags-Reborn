package ua.polodarb.gmsflags.presentation.feature.settings.overview.components

import android.content.res.Resources
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing
import ua.polodarb.gmsflags.presentation.core.ui.state.localizedDescription
import ua.polodarb.gmsflags.presentation.feature.settings.R
import ua.polodarb.gmsflags.presentation.feature.settings.overview.ServerConnectionState

@Composable
internal fun ServerConnectionCard(
    state: ServerConnectionState,
    resources: Resources,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
                alignment = Alignment.TopCenter,
            ),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Crossfade(targetState = state, label = "serverConnection") { connection ->
            when (connection) {
                ServerConnectionState.Checking -> ServerConnectionContent(
                    status = stringResource(R.string.settings_server_checking),
                    description = stringResource(R.string.settings_server_checking_description),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    checking = true,
                )
                ServerConnectionState.Available -> ServerConnectionContent(
                    status = stringResource(R.string.settings_server_available),
                    description = stringResource(R.string.settings_server_available_description),
                    color = MaterialTheme.colorScheme.primary,
                )
                is ServerConnectionState.Unavailable -> ServerConnectionContent(
                    status = stringResource(R.string.settings_server_unavailable),
                    description = connection.error.localizedDescription(resources),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun ServerConnectionContent(
    status: String,
    description: String,
    color: Color,
    checking: Boolean = false,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(GmsSpacing.Large),
        verticalArrangement = Arrangement.spacedBy(GmsSpacing.ExtraSmall),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(GmsSpacing.Small),
        ) {
            if (checking) {
                CircularProgressIndicator(
                    modifier = Modifier.size(10.dp),
                    strokeWidth = 1.5.dp,
                    color = color,
                )
            } else {
                Surface(modifier = Modifier.size(10.dp), shape = CircleShape, color = color) {}
            }
            Text(
                text = stringResource(R.string.settings_server_connection) + ": " + status,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
