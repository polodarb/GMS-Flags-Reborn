package ua.polodarb.gmsflags.presentation.feature.onboarding.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * Wraps [content] in a spring-driven entrance: the child fades and rises into place.
 *
 * The reveal replays every time the composable enters the composition, so nesting these inside an
 * [androidx.compose.animation.AnimatedContent] step swap produces a fresh staggered cascade per
 * step. Order children with an increasing [delayMillis] to build the stagger.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun OnboardingReveal(
    modifier: Modifier = Modifier,
    delayMillis: Int = 0,
    content: @Composable () -> Unit,
) {
    val spatialSpec = MaterialTheme.motionScheme.slowSpatialSpec<Float>()
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        if (delayMillis > 0) delay(delayMillis.toLong())
        progress.animateTo(targetValue = 1f, animationSpec = spatialSpec)
    }

    Box(
        modifier = modifier
            .layout { measurable, constraints ->
                val placeable = measurable.measure(constraints)
                layout(placeable.width, placeable.height) { placeable.place(0, 0) }
            }
            .graphicsLayer {
                val value = progress.value
                alpha = value.coerceIn(0f, 1f)
                translationY = (1f - value) * 20.dp.toPx()
            },
    ) {
        content()
    }
}
