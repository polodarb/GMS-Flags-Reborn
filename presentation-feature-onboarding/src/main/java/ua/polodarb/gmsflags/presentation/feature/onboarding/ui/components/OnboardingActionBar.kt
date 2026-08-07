package ua.polodarb.gmsflags.presentation.feature.onboarding.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.annotation.StringRes
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsDimensions
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.LocalGmsAdaptiveLayout
import ua.polodarb.gmsflags.presentation.feature.onboarding.R
import ua.polodarb.gmsflags.presentation.feature.onboarding.mvi.OnboardingEvent
import ua.polodarb.gmsflags.presentation.feature.onboarding.mvi.OnboardingState
import ua.polodarb.gmsflags.presentation.feature.onboarding.mvi.OnboardingStep

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun OnboardingActionBar(
    state: OnboardingState,
    onEvent: (OnboardingEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val layout = LocalGmsAdaptiveLayout.current
    val fadeInSpec = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()
    val fadeOutSpec = MaterialTheme.motionScheme.fastEffectsSpec<Float>()
    val slideSpec = MaterialTheme.motionScheme.defaultSpatialSpec<IntOffset>()
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceBright,
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .sizeIn(maxWidth = minOf(layout.contentMaxWidth, 560.dp))
                    .padding(
                        start = layout.contentPadding,
                        top = GmsSpacing.Small,
                        end = layout.contentPadding,
                        bottom = GmsSpacing.Small,
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(GmsSpacing.Small),
            ) {
                val loading = state.requestingRoot || state.completing
                Button(
                    onClick = {
                        onEvent(
                            when (state.step) {
                                OnboardingStep.Welcome -> OnboardingEvent.ContinueFromWelcome
                                OnboardingStep.Disclaimer -> OnboardingEvent.ContinueFromDisclaimer
                                OnboardingStep.RootAccess -> OnboardingEvent.RequestRoot
                                OnboardingStep.Notifications -> OnboardingEvent.RequestNotifications
                            }
                        )
                    },
                    enabled = !loading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = GmsDimensions.PrimaryActionHeight),
                ) {
                    val labelRes = if (loading) null else primaryActionLabel(state)
                    AnimatedContent(
                        targetState = labelRes,
                        transitionSpec = { fadeIn(fadeInSpec) togetherWith fadeOut(fadeOutSpec) },
                        label = "onboardingActionLabel",
                    ) { target ->
                        if (target == null) {
                            LoadingIndicator()
                        } else {
                            Text(
                                text = stringResource(target),
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }

                AnimatedContent(
                    targetState = state.step,
                    transitionSpec = {
                        (fadeIn(fadeInSpec) + slideInVertically(slideSpec) { it / 3 })
                            .togetherWith(fadeOut(fadeOutSpec))
                    },
                    label = "onboardingActionSecondary",
                ) { step ->
                    when (step) {
                        OnboardingStep.Welcome -> LegalLine()
                        OnboardingStep.Disclaimer -> Unit
                        OnboardingStep.RootAccess -> Unit
                        OnboardingStep.Notifications -> TextButton(
                            onClick = { onEvent(OnboardingEvent.SkipNotifications) },
                            enabled = !state.completing,
                        ) {
                            Text(stringResource(R.string.onboarding_notifications_skip))
                        }
                    }
                }
            }
        }
    }
}

@StringRes
private fun primaryActionLabel(state: OnboardingState): Int = when (state.step) {
    OnboardingStep.Welcome -> R.string.onboarding_welcome_action
    OnboardingStep.Disclaimer -> R.string.onboarding_disclaimer_action
    OnboardingStep.RootAccess -> R.string.onboarding_root_action
    OnboardingStep.Notifications -> if (state.notificationRequestDenied) {
        R.string.onboarding_notifications_try_again
    } else {
        R.string.onboarding_notifications_action
    }
}

@Composable
private fun LegalLine() {
    val termsUrl = stringResource(R.string.onboarding_terms_url)
    val privacyUrl = stringResource(R.string.onboarding_privacy_url)
    val linkStyle = SpanStyle(
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.SemiBold,
    )
    Text(
        text = buildAnnotatedString {
            append(stringResource(R.string.onboarding_legal_prefix))
            append(" ")
            withLink(
                LinkAnnotation.Url(
                    url = termsUrl,
                    styles = TextLinkStyles(style = linkStyle),
                )
            ) {
                append(stringResource(R.string.onboarding_terms))
            }
            append(" ")
            append(stringResource(R.string.onboarding_legal_and))
            append(" ")
            withLink(
                LinkAnnotation.Url(
                    url = privacyUrl,
                    styles = TextLinkStyles(style = linkStyle),
                )
            ) {
                append(stringResource(R.string.onboarding_privacy))
            }
            append(stringResource(R.string.onboarding_legal_suffix))
        },
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(top = GmsSpacing.ExtraSmall),
    )
}
