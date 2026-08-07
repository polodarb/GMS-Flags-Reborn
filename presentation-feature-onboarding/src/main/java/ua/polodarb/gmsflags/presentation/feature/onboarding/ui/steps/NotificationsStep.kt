package ua.polodarb.gmsflags.presentation.feature.onboarding.ui.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.NotificationsOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing
import ua.polodarb.gmsflags.presentation.feature.onboarding.R
import ua.polodarb.gmsflags.presentation.feature.onboarding.mvi.OnboardingState
import ua.polodarb.gmsflags.presentation.feature.onboarding.ui.components.OnboardingReveal
import ua.polodarb.gmsflags.presentation.feature.onboarding.ui.components.OnboardingStatusCard
import ua.polodarb.gmsflags.presentation.feature.onboarding.ui.components.OnboardingStepText

@Composable
internal fun NotificationsStep(state: OnboardingState) {
    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(GmsSpacing.ExtraLarge),
    ) {
        OnboardingReveal(delayMillis = 40) {
            OnboardingStepText(
                title = R.string.onboarding_notifications_title,
                body = R.string.onboarding_notifications_body,
            )
        }
        OnboardingReveal(delayMillis = 130) {
            NotificationPreview()
        }
        if (state.notificationRequestDenied) {
            OnboardingReveal(delayMillis = 40) {
                OnboardingStatusCard(
                    icon = Icons.Rounded.NotificationsOff,
                    text = stringResource(R.string.onboarding_notifications_denied),
                )
            }
        }
        if (state.completionFailed) {
            OnboardingReveal(delayMillis = 40) {
                OnboardingStatusCard(
                    icon = Icons.Rounded.NotificationsOff,
                    text = stringResource(R.string.onboarding_completion_failed),
                    error = true,
                )
            }
        }
    }
}

@Composable
private fun NotificationPreview() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Row(
            modifier = Modifier.padding(GmsSpacing.Large),
            horizontalArrangement = Arrangement.spacedBy(GmsSpacing.Large),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(52.dp),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.Campaign, contentDescription = null)
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(GmsSpacing.ExtraSmall),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(GmsSpacing.Small),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.onboarding_notification_preview_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    SoonBadge()
                }
                Text(
                    text = stringResource(R.string.onboarding_notification_preview_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun SoonBadge() {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.tertiary,
        contentColor = MaterialTheme.colorScheme.onTertiary,
    ) {
        Text(
            text = stringResource(R.string.onboarding_soon),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = GmsSpacing.Small, vertical = GmsSpacing.Micro),
        )
    }
}
