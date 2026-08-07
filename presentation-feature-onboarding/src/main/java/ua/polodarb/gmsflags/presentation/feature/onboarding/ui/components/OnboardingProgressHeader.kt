package ua.polodarb.gmsflags.presentation.feature.onboarding.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsDimensions
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing
import ua.polodarb.gmsflags.presentation.feature.onboarding.R
import ua.polodarb.gmsflags.presentation.feature.onboarding.mvi.OnboardingStep

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun OnboardingProgressHeader(
    step: OnboardingStep,
    onBack: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(GmsDimensions.HeaderHeight)
            .padding(horizontal = GmsSpacing.Small),
        contentAlignment = Alignment.Center,
    ) {
        if (step != OnboardingStep.Welcome) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(GmsDimensions.MinimumTouchTarget),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = stringResource(R.string.onboarding_back),
                )
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(GmsSpacing.Small),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OnboardingStep.entries.forEach { item ->
                val selected = item == step
                val width by animateDpAsState(
                    targetValue = if (selected) 30.dp else 8.dp,
                    animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
                    label = "progressWidth",
                )
                val color by animateColorAsState(
                    targetValue = if (item.ordinal <= step.ordinal) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerHighest
                    },
                    animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
                    label = "progressColor",
                )
                Surface(
                    modifier = Modifier.size(width = width, height = 8.dp),
                    shape = RoundedCornerShape(percent = 50),
                    color = color,
                    content = {},
                )
            }
        }
    }
}
