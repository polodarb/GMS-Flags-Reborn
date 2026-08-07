package ua.polodarb.gmsflags.presentation.feature.onboarding.ui.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing
import ua.polodarb.gmsflags.presentation.feature.onboarding.R
import ua.polodarb.gmsflags.presentation.feature.onboarding.ui.components.OnboardingReveal
import ua.polodarb.gmsflags.presentation.feature.onboarding.ui.components.OnboardingStatusCard

@Composable
private fun BrandLockup() {
    Column(verticalArrangement = Arrangement.spacedBy(GmsSpacing.Small)) {
        Text(
            text = buildAnnotatedString {
                append(stringResource(R.string.onboarding_app_name))
                append(" ")
                withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                    append(stringResource(R.string.onboarding_version_label))
                }
            },
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = stringResource(R.string.onboarding_welcome_body),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
internal fun WelcomeStep() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(GmsSpacing.ExtraLarge),
    ) {
        OnboardingReveal(delayMillis = 40) {
            BrandLockup()
        }
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(GmsSpacing.Small),
        ) {
            OnboardingReveal(delayMillis = 150) {
                OnboardingStatusCard(
                    icon = Icons.Rounded.Search,
                    text = stringResource(R.string.onboarding_feature_discover),
                    accent = MaterialTheme.colorScheme.primary,
                    onAccent = MaterialTheme.colorScheme.onPrimary,
                )
            }
            OnboardingReveal(delayMillis = 210) {
                OnboardingStatusCard(
                    icon = Icons.Rounded.Bolt,
                    text = stringResource(R.string.onboarding_feature_override),
                    accent = MaterialTheme.colorScheme.secondary,
                    onAccent = MaterialTheme.colorScheme.onSecondary,
                )
            }
            OnboardingReveal(delayMillis = 270) {
                OnboardingStatusCard(
                    icon = Icons.Rounded.RestartAlt,
                    text = stringResource(R.string.onboarding_feature_restore),
                    accent = MaterialTheme.colorScheme.tertiary,
                    onAccent = MaterialTheme.colorScheme.onTertiary,
                )
            }
        }
    }
}
