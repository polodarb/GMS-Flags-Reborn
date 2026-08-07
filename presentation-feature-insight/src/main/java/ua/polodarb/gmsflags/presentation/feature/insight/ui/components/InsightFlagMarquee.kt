package ua.polodarb.gmsflags.presentation.feature.insight.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing

@Composable
internal fun InsightFlagMarquee(modifier: Modifier = Modifier) {
    val fadeColor = MaterialTheme.colorScheme.primaryContainer
    Column(
        modifier = modifier
            .clipToBounds()
            .drawWithCache {
                val fade = Brush.horizontalGradient(
                    0f to fadeColor,
                    0.18f to Color.Transparent,
                    0.82f to Color.Transparent,
                    1f to fadeColor,
                )
                onDrawWithContent {
                    drawContent()
                    drawRect(fade)
                }
            }
            .clearAndSetSemantics { },
        verticalArrangement = Arrangement.spacedBy(GmsSpacing.Small),
    ) {
        MarqueeRow(MarqueeRowOne, durationMillis = 26_000, reverse = false)
        MarqueeRow(MarqueeRowTwo, durationMillis = 34_000, reverse = true)
    }
}

@Composable
private fun MarqueeRow(items: List<String>, durationMillis: Int, reverse: Boolean) {
    var contentWidth by remember { mutableIntStateOf(0) }
    val transition = rememberInfiniteTransition(label = "flag-marquee")
    val progress = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "flag-marquee-offset",
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentWidth(align = Alignment.Start, unbounded = true)
            .onSizeChanged { contentWidth = it.width }
            .graphicsLayer {
                val half = contentWidth / 2f
                val travelled = progress.value * half
                translationX = if (reverse) travelled - half else -travelled
            },
        horizontalArrangement = Arrangement.spacedBy(GmsSpacing.Small),
    ) {
        repeat(2) {
            items.forEach { label -> MarqueeChip(label) }
        }
    }
}

@Composable
private fun MarqueeChip(label: String) {
    Text(
        text = label,
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.07f),
                shape = MaterialTheme.shapes.small,
            )
            .padding(horizontal = GmsSpacing.Medium, vertical = GmsSpacing.Small),
        style = MaterialTheme.typography.labelLarge,
        fontFamily = FontFamily.Monospace,
        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.55f),
        maxLines = 1,
    )
}

private val MarqueeRowOne = listOf(
    "NearbySharing__enable_quick_share_v2",
    "Auth__passkey_autofill",
    "Cast__enable_remote_display",
    "FastPair__half_sheet_v3",
    "Games__enable_profile_v2",
    "Ads__enable_topics_api",
)

private val MarqueeRowTwo = listOf(
    "Wallet__enable_smart_tap",
    "Fitness__enable_sleep_insights",
    "Chimera__module_prefetch",
    "Location__enable_ble_scanning",
    "Backup__enable_e2e_encryption",
    "Instant__enable_web_apk",
)

