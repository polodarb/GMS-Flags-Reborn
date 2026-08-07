package ua.polodarb.gmsflags.presentation.feature.settings.overview.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.toPath
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.Morph
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun AnimatedSettingsLogo(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val motion = rememberInfiniteTransition(label = "settingsHero")
    val orbit by motion.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 36_000, easing = LinearEasing),
        ),
        label = "heroOrbit",
    )
    val reverseOrbit by motion.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 48_000, easing = LinearEasing),
        ),
        label = "heroReverseOrbit",
    )
    val morphProgress by motion.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 7_000),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "heroMorph",
    )

    val largeMorph = remember { Morph(MaterialShapes.SoftBoom, MaterialShapes.Flower) }
    val mediumMorph = remember { Morph(MaterialShapes.PuffyDiamond, MaterialShapes.Cookie9Sided) }
    val smallMorph = remember { Morph(MaterialShapes.Clover4Leaf, MaterialShapes.Cookie4Sided) }
    val distantMorph = remember { Morph(MaterialShapes.Ghostish, MaterialShapes.PixelCircle) }
    val largePath = remember { Path() }
    val mediumPath = remember { Path() }
    val smallPath = remember { Path() }
    val distantPath = remember { Path() }
    val colors = MaterialTheme.colorScheme
    val lightThemeAlphaBoost = if (isSystemInDarkTheme()) 1f else 1.22f

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(168.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.matchParentSize()) {
            val largeDepth = depthAt(orbit + 30f)
            val mediumDepth = depthAt(reverseOrbit + 145f)
            val smallDepth = depthAt(orbit + 245f)
            val distantDepth = depthAt(reverseOrbit + 285f)

            val largePosition = floatingPosition(
                angleDegrees = orbit,
                anchor = Offset(size.width * 0.17f, size.height * 0.5f),
                horizontalTravel = size.width * 0.07f,
                verticalTravel = size.height * 0.14f,
            )
            val mediumPosition = floatingPosition(
                angleDegrees = reverseOrbit + 112f,
                anchor = Offset(size.width * 0.82f, size.height * 0.32f),
                horizontalTravel = size.width * 0.06f,
                verticalTravel = size.height * 0.12f,
            )
            val smallPosition = floatingPosition(
                angleDegrees = orbit + 228f,
                anchor = Offset(size.width * 0.79f, size.height * 0.76f),
                horizontalTravel = size.width * 0.08f,
                verticalTravel = size.height * 0.08f,
            )
            val distantPosition = floatingPosition(
                angleDegrees = reverseOrbit + 268f,
                anchor = Offset(size.width * 0.23f, size.height * 0.18f),
                horizontalTravel = size.width * 0.08f,
                verticalTravel = size.height * 0.07f,
            )

            drawMorphShape(
                path = largeMorph.toPath(morphProgress, largePath),
                position = largePosition,
                diameter = lerp(88.dp.toPx(), 108.dp.toPx(), largeDepth),
                rotation = orbit * 0.55f,
                color = colors.primaryContainer.copy(
                    alpha = boostedAlpha(lerp(0.42f, 0.78f, largeDepth), lightThemeAlphaBoost),
                ),
            )
            drawMorphShape(
                path = mediumMorph.toPath(1f - morphProgress, mediumPath),
                position = mediumPosition,
                diameter = lerp(54.dp.toPx(), 68.dp.toPx(), mediumDepth),
                rotation = reverseOrbit * 0.7f,
                color = colors.tertiaryContainer.copy(
                    alpha = boostedAlpha(lerp(0.32f, 0.66f, mediumDepth), lightThemeAlphaBoost),
                ),
            )
            drawMorphShape(
                path = smallMorph.toPath(morphProgress, smallPath),
                position = smallPosition,
                diameter = lerp(32.dp.toPx(), 42.dp.toPx(), smallDepth),
                rotation = orbit,
                color = colors.secondaryContainer.copy(
                    alpha = boostedAlpha(lerp(0.24f, 0.56f, smallDepth), lightThemeAlphaBoost),
                ),
            )
            drawMorphShape(
                path = distantMorph.toPath(1f - morphProgress, distantPath),
                position = distantPosition,
                diameter = lerp(18.dp.toPx(), 26.dp.toPx(), distantDepth),
                rotation = reverseOrbit * 0.82f,
                color = colors.surfaceContainerHighest.copy(
                    alpha = boostedAlpha(
                        lerp(0.22f, 0.46f, distantDepth),
                        lightThemeAlphaBoost,
                    ),
                ),
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp),
            content = content,
        )
    }
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
        y = anchor.y + sin(radians * 1.37).toFloat() * verticalTravel,
    )
}

private fun depthAt(angleDegrees: Float): Float {
    val radians = Math.toRadians(angleDegrees.toDouble())
    return (sin(radians).toFloat() + 1f) / 2f
}

private fun lerp(start: Float, end: Float, fraction: Float): Float =
    start + (end - start) * fraction

private fun boostedAlpha(alpha: Float, boost: Float): Float = (alpha * boost).coerceAtMost(1f)

private fun DrawScope.drawMorphShape(
    path: Path,
    position: Offset,
    diameter: Float,
    rotation: Float,
    color: Color,
) {
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
