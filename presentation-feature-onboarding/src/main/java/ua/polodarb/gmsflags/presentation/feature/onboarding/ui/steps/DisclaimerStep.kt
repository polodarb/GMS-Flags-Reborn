package ua.polodarb.gmsflags.presentation.feature.onboarding.ui.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BackupTable
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing
import ua.polodarb.gmsflags.presentation.feature.onboarding.R
import ua.polodarb.gmsflags.presentation.feature.onboarding.ui.components.OnboardingReveal
import ua.polodarb.gmsflags.presentation.feature.onboarding.ui.components.OnboardingStatusCard
import ua.polodarb.gmsflags.presentation.feature.onboarding.ui.components.OnboardingStepText

@Composable
internal fun DisclaimerStep() {
    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(GmsSpacing.ExtraLarge),
    ) {
        OnboardingReveal(delayMillis = 40) {
            OnboardingStepText(
                title = R.string.onboarding_disclaimer_title,
                body = R.string.onboarding_disclaimer_body,
            )
        }
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(GmsSpacing.Small),
        ) {
            OnboardingReveal(delayMillis = 120) {
                OnboardingStatusCard(
                    icon = Icons.Rounded.Bolt,
                    text = stringResource(R.string.onboarding_disclaimer_point_impact),
                )
            }
            OnboardingReveal(delayMillis = 180) {
                OnboardingStatusCard(
                    icon = Icons.Rounded.BackupTable,
                    text = stringResource(R.string.onboarding_disclaimer_point_reversible),
                )
            }
            OnboardingReveal(delayMillis = 240) {
                OnboardingStatusCard(
                    icon = Icons.Rounded.VerifiedUser,
                    text = stringResource(R.string.onboarding_disclaimer_point_responsibility),
                )
            }
        }
    }
}
