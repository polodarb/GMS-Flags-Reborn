package ua.polodarb.gmsflags.presentation.feature.onboarding.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.LocalGmsAdaptiveLayout
import ua.polodarb.gmsflags.presentation.feature.onboarding.mvi.OnboardingEvent
import ua.polodarb.gmsflags.presentation.feature.onboarding.mvi.OnboardingState
import ua.polodarb.gmsflags.presentation.feature.onboarding.mvi.OnboardingStep
import ua.polodarb.gmsflags.presentation.feature.onboarding.ui.components.OnboardingActionBar
import ua.polodarb.gmsflags.presentation.feature.onboarding.ui.components.OnboardingHero
import ua.polodarb.gmsflags.presentation.feature.onboarding.ui.components.OnboardingProgressHeader
import ua.polodarb.gmsflags.presentation.feature.onboarding.ui.steps.DisclaimerStep
import ua.polodarb.gmsflags.presentation.feature.onboarding.ui.steps.NotificationsStep
import ua.polodarb.gmsflags.presentation.feature.onboarding.ui.steps.RootAccessStep
import ua.polodarb.gmsflags.presentation.feature.onboarding.ui.steps.WelcomeStep

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun OnboardingContent(
    state: OnboardingState,
    onEvent: (OnboardingEvent) -> Unit,
) {
    val layout = LocalGmsAdaptiveLayout.current
    val scrollState = rememberScrollState()
    LaunchedEffect(state.step) {
        scrollState.scrollTo(0)
    }
    val fadeInSpec = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()
    val fadeOutSpec = MaterialTheme.motionScheme.fastEffectsSpec<Float>()
    val slideSpec = MaterialTheme.motionScheme.slowSpatialSpec<IntOffset>()
    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceBright,
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            OnboardingProgressHeader(
                step = state.step,
                onBack = { onEvent(OnboardingEvent.Back) },
            )
        },
        bottomBar = {
            OnboardingActionBar(
                state = state,
                onEvent = onEvent,
                modifier = Modifier.navigationBarsPadding(),
            )
        },
    ) { contentPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = contentPadding.calculateTopPadding()),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .sizeIn(maxWidth = minOf(layout.contentMaxWidth, 640.dp))
                    .verticalScroll(scrollState)
                    .padding(
                        PaddingValues(
                            start = layout.contentPadding,
                            top = GmsSpacing.Small,
                            end = layout.contentPadding,
                            bottom = contentPadding.calculateBottomPadding() + GmsSpacing.ExtraLarge,
                        )
                    ),
                horizontalAlignment = Alignment.Start,
            ) {
                OnboardingHero(step = state.step)
                AnimatedContent(
                    targetState = state.step,
                    transitionSpec = {
                        (fadeIn(fadeInSpec) +
                            slideInVertically(slideSpec) { height -> height / 10 })
                            .togetherWith(
                                fadeOut(fadeOutSpec) +
                                    slideOutVertically(slideSpec) { height -> -height / 16 }
                            )
                            .using(SizeTransform(clip = false))
                    },
                    contentKey = { it },
                    modifier = Modifier.fillMaxWidth(),
                    label = "onboardingStepContent",
                ) { step ->
                    when (step) {
                        OnboardingStep.Welcome -> WelcomeStep()
                        OnboardingStep.Disclaimer -> DisclaimerStep()
                        OnboardingStep.RootAccess -> RootAccessStep(state = state)
                        OnboardingStep.Notifications -> NotificationsStep(state = state)
                    }
                }
            }
        }
    }
}
