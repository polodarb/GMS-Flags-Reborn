package ua.polodarb.gmsflags.presentation.feature.onboarding.ui.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Security
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing
import ua.polodarb.gmsflags.presentation.feature.onboarding.R
import ua.polodarb.gmsflags.presentation.feature.onboarding.mvi.OnboardingState
import ua.polodarb.gmsflags.presentation.feature.onboarding.ui.components.OnboardingReveal
import ua.polodarb.gmsflags.presentation.feature.onboarding.ui.components.OnboardingStatusCard
import ua.polodarb.gmsflags.presentation.feature.onboarding.ui.components.OnboardingStepText

@Composable
internal fun RootAccessStep(state: OnboardingState) {
    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(GmsSpacing.ExtraLarge),
    ) {
        OnboardingReveal(delayMillis = 40) {
            OnboardingStepText(
                title = R.string.onboarding_root_title,
                body = R.string.onboarding_root_body,
            )
        }
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(GmsSpacing.Small),
        ) {
            if (state.rootRequestFailed) {
                OnboardingReveal(delayMillis = 40) {
                    OnboardingStatusCard(
                        icon = Icons.Rounded.Lock,
                        text = stringResource(R.string.onboarding_root_denied),
                        error = true,
                    )
                }
            }
            OnboardingReveal(delayMillis = 120) {
                OnboardingStatusCard(
                    icon = Icons.Rounded.Key,
                    text = stringResource(R.string.onboarding_root_reason_read),
                )
            }
            OnboardingReveal(delayMillis = 180) {
                OnboardingStatusCard(
                    icon = Icons.Rounded.Security,
                    text = stringResource(R.string.onboarding_root_reason_local),
                )
            }
        }
    }
}
