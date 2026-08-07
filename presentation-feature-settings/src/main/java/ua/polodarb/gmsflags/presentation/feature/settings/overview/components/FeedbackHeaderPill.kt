package ua.polodarb.gmsflags.presentation.feature.settings.overview.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Feedback
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing
import ua.polodarb.gmsflags.presentation.core.ui.haptic.rememberHapticClick
import ua.polodarb.gmsflags.presentation.feature.settings.R

@Composable
internal fun FeedbackHeaderPill(onClick: () -> Unit) {
    Surface(
        onClick = rememberHapticClick(onClick = onClick),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
    ) {
        Box(
            modifier = Modifier.padding(
                horizontal = GmsSpacing.Medium,
                vertical = GmsSpacing.Small,
            ),
        ) {
            Icon(
                imageVector = Icons.Outlined.Feedback,
                contentDescription = stringResource(R.string.settings_feedback_link),
                modifier = Modifier.size(18.dp),
            )
        }
    }
}
