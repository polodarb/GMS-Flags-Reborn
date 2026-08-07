package ua.polodarb.gmsflags.presentation.feature.onboarding.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.toPath
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import org.koin.compose.koinInject
import ua.polodarb.gmsflags.presentation.core.ui.application.ApplicationIconProvider
import ua.polodarb.gmsflags.presentation.core.ui.application.GmsApplicationIcon
import ua.polodarb.gmsflags.presentation.feature.onboarding.R
import ua.polodarb.gmsflags.presentation.feature.onboarding.mvi.OnboardingStep
import kotlin.math.cos
import kotlin.math.sin

private const val HeroHeightDp = 236

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun OnboardingHero(
    step: OnboardingStep,
    modifier: Modifier = Modifier,
) {
    val motion = rememberInfiniteTransition(label = "onboardingHero")
    val orbit by motion.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 34_000, easing = LinearEasing),
        ),
        label = "heroOrbit",
    )
    val reverseOrbit by motion.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 46_000, easing = LinearEasing),
        ),
        label = "heroReverseOrbit",
    )

    val entrance = remember { Animatable(0f) }
    val entranceSpec = MaterialTheme.motionScheme.slowSpatialSpec<Float>()
    LaunchedEffect(Unit) {
        entrance.animateTo(targetValue = 1f, animationSpec = entranceSpec)
    }

    var renderedStep by remember { mutableStateOf(step) }
    var centerMorph by remember {
        val shape = centerShape(step)
        mutableStateOf(Morph(shape, shape))
    }
    val morphProgress = remember { Animatable(1f) }
    val morphSpec = MaterialTheme.motionScheme.defaultSpatialSpec<Float>()
    LaunchedEffect(step) {
        if (step != renderedStep) {
            centerMorph = Morph(centerShape(renderedStep), centerShape(step))
            renderedStep = step
            morphProgress.snapTo(0f)
            morphProgress.animateTo(targetValue = 1f, animationSpec = morphSpec)
        }
    }

    val accentLarge = remember { Morph(MaterialShapes.SoftBoom, MaterialShapes.SoftBoom) }
    val accentMedium = remember { Morph(MaterialShapes.Clover4Leaf, MaterialShapes.Clover4Leaf) }
    val accentSmall = remember { Morph(MaterialShapes.Cookie4Sided, MaterialShapes.Cookie4Sided) }
    val accentLargePath = remember { Path() }
    val accentMediumPath = remember { Path() }
    val accentSmallPath = remember { Path() }
    val centerPath = remember { Path() }

    val colors = MaterialTheme.colorScheme
    val alphaBoost = if (isSystemInDarkTheme()) 1f else 1.08f

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(HeroHeightDp.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.matchParentSize()) {
            val enter = entrance.value.coerceIn(0f, 1f)
            val largeDepth = depthAt(orbit + 40f)
            val mediumDepth = depthAt(reverseOrbit + 150f)
            val smallDepth = depthAt(orbit + 250f)

            drawHeroShape(
                path = accentLarge.toPath(0f, accentLargePath),
                position = floatingPosition(
                    angleDegrees = orbit,
                    anchor = Offset(size.width * 0.13f, size.height * 0.34f),
                    horizontalTravel = size.width * 0.04f,
                    verticalTravel = size.height * 0.09f,
                ),
                diameter = lerp(78.dp.toPx(), 96.dp.toPx(), largeDepth) * enter,
                rotation = orbit,
                color = colors.tertiaryContainer.copy(
                    alpha = boostedAlpha(lerp(0.55f, 0.85f, largeDepth) * enter, alphaBoost),
                ),
            )
            drawHeroShape(
                path = accentMedium.toPath(0f, accentMediumPath),
                position = floatingPosition(
                    angleDegrees = reverseOrbit + 120f,
                    anchor = Offset(size.width * 0.87f, size.height * 0.66f),
                    horizontalTravel = size.width * 0.045f,
                    verticalTravel = size.height * 0.08f,
                ),
                diameter = lerp(58.dp.toPx(), 74.dp.toPx(), mediumDepth) * enter,
                rotation = reverseOrbit,
                color = colors.secondaryContainer.copy(
                    alpha = boostedAlpha(lerp(0.5f, 0.8f, mediumDepth) * enter, alphaBoost),
                ),
            )
            drawHeroShape(
                path = accentSmall.toPath(0f, accentSmallPath),
                position = floatingPosition(
                    angleDegrees = orbit + 235f,
                    anchor = Offset(size.width * 0.82f, size.height * 0.2f),
                    horizontalTravel = size.width * 0.04f,
                    verticalTravel = size.height * 0.06f,
                ),
                diameter = lerp(30.dp.toPx(), 42.dp.toPx(), smallDepth) * enter,
                rotation = orbit,
                color = colors.primary.copy(
                    alpha = boostedAlpha(lerp(0.35f, 0.6f, smallDepth) * enter, alphaBoost),
                ),
            )

            drawHeroShape(
                path = centerMorph.toPath(morphProgress.value, centerPath),
                position = center,
                diameter = 172.dp.toPx() * lerp(0.84f, 1f, enter),
                rotation = lerp(-6f, 2f, morphProgress.value),
                color = colors.primaryContainer,
            )
        }

        AnimatedContent(
            targetState = step,
            transitionSpec = {
                (fadeIn(tween(240)) + scaleIn(initialScale = 0.7f))
                    .togetherWith(fadeOut(tween(120)) + scaleOut(targetScale = 1.2f))
            },
            label = "heroContent",
            modifier = Modifier.graphicsLayer {
                val enter = entrance.value.coerceIn(0f, 1f)
                val scale = lerp(0.6f, 1f, enter)
                scaleX = scale
                scaleY = scale
                alpha = enter
            },
        ) { targetStep ->
            HeroContent(step = targetStep)
        }
    }
}

@Composable
private fun HeroContent(step: OnboardingStep) {
    when (step) {
        OnboardingStep.Welcome -> {
            val context = LocalContext.current
            val iconProvider: ApplicationIconProvider = koinInject()
            GmsApplicationIcon(
                packageName = context.packageName,
                applicationName = stringResource(R.string.onboarding_app_name),
                iconProvider = iconProvider,
                modifier = Modifier
                    .size(108.dp)
                    .graphicsLayer {
                        scaleX = 1.7f
                        scaleY = 1.7f
                    },
                foregroundOnly = true,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }

        OnboardingStep.Disclaimer -> Icon(
            imageVector = Icons.Rounded.Shield,
            contentDescription = null,
            modifier = Modifier.size(68.dp),
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
        )

        OnboardingStep.RootAccess -> Icon(
            imageVector = Icons.Rounded.AdminPanelSettings,
            contentDescription = null,
            modifier = Modifier.size(68.dp),
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
        )

        OnboardingStep.Notifications -> Icon(
            imageVector = Icons.Rounded.NotificationsActive,
            contentDescription = null,
            modifier = Modifier.size(68.dp),
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private fun centerShape(step: OnboardingStep): RoundedPolygon = when (step) {
    OnboardingStep.Welcome -> MaterialShapes.Cookie9Sided
    OnboardingStep.Disclaimer -> MaterialShapes.Cookie6Sided
    OnboardingStep.RootAccess -> MaterialShapes.PuffyDiamond
    OnboardingStep.Notifications -> MaterialShapes.SoftBoom
}

private fun DrawScope.floatingPosition(
    angleDegrees: Float,
    anchor: Offset,
    horizontalTravel: Float,
    verticalTravel: Float,
): Offset {
    val radians = Math.toRadians(angleDegrees.toDouble())
    return Offset(
        x = anchor.x + cos(radians).toFloat() * horizontalTravel,
        y = anchor.y + sin(radians).toFloat() * verticalTravel,
    )
}

private fun depthAt(angleDegrees: Float): Float {
    val radians = Math.toRadians(angleDegrees.toDouble())
    return (sin(radians).toFloat() + 1f) / 2f
}

private fun lerp(start: Float, end: Float, fraction: Float): Float =
    start + (end - start) * fraction

private fun boostedAlpha(alpha: Float, boost: Float): Float =
    (alpha * boost).coerceIn(0f, 1f)

private fun DrawScope.drawHeroShape(
    path: Path,
    position: Offset,
    diameter: Float,
    rotation: Float,
    color: Color,
) {
    if (diameter <= 0f) return
    withTransform({
        translate(
            left = position.x - diameter / 2f,
            top = position.y - diameter / 2f,
        )
        rotate(degrees = rotation, pivot = Offset(diameter / 2f, diameter / 2f))
        scale(scaleX = diameter, scaleY = diameter, pivot = Offset.Zero)
    }) {
        drawPath(path = path, color = color)
    }
}
