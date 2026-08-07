package ua.polodarb.gmsflags.presentation.feature.onboarding.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing

@Composable
internal fun OnboardingStatusCard(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier,
    error: Boolean = false,
    accent: Color = MaterialTheme.colorScheme.primary,
    onAccent: Color = MaterialTheme.colorScheme.onPrimary,
) {
    val container = if (error) {
        MaterialTheme.colorScheme.errorContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh
    }
    val onContainer = if (error) {
        MaterialTheme.colorScheme.onErrorContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val iconContainer = if (error) MaterialTheme.colorScheme.error else accent
    val onIconContainer = if (error) MaterialTheme.colorScheme.onError else onAccent

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = container,
        contentColor = onContainer,
    ) {
        Row(
            modifier = Modifier.padding(GmsSpacing.Large),
            horizontalArrangement = Arrangement.spacedBy(GmsSpacing.Large),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = iconContainer,
                contentColor = onIconContainer,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
            )
        }
    }
}
