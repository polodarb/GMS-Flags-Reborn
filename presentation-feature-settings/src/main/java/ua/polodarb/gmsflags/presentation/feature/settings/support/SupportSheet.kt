package ua.polodarb.gmsflags.presentation.feature.settings.support

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsDimensions
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing
import ua.polodarb.gmsflags.presentation.feature.settings.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SupportSheet(onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current
    val kofiUrl = stringResource(R.string.settings_support_kofi_url)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        contentWindowInsets = { WindowInsets.safeDrawing.only(WindowInsetsSides.Top) },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(
                    start = GmsSpacing.ExtraLarge,
                    end = GmsSpacing.ExtraLarge,
                    bottom = GmsSpacing.Small,
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(GmsSpacing.ExtraLarge),
        ) {
            SupportHero()
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(GmsSpacing.Small),
            ) {
                Text(
                    text = stringResource(R.string.settings_support_sheet_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = stringResource(R.string.settings_support_sheet_body),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
            Button(
                onClick = {
                    runCatching { uriHandler.openUri(kofiUrl) }
                    coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) onDismiss()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(GmsDimensions.PrimaryActionHeight),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    text = stringResource(R.string.settings_support_sheet_action),
                    modifier = Modifier.padding(start = GmsSpacing.Small),
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SupportHero() {
    val transition = rememberInfiniteTransition(label = "supportHero")
    val beat by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 850, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "beat",
    )
    val drift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 26_000, easing = LinearEasing),
        ),
        label = "drift",
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(148.dp),
        contentAlignment = Alignment.Center,
    ) {
        DecorShape(
            shape = MaterialShapes.Cookie6Sided.toShape(),
            color = MaterialTheme.colorScheme.tertiaryContainer,
            alignment = Alignment.CenterStart,
            offsetX = 28.dp,
            offsetY = (-30).dp,
            size = 54.dp,
            rotation = drift,
        )
        DecorShape(
            shape = MaterialShapes.Clover4Leaf.toShape(),
            color = MaterialTheme.colorScheme.secondaryContainer,
            alignment = Alignment.CenterEnd,
            offsetX = (-22).dp,
            offsetY = 32.dp,
            size = 46.dp,
            rotation = -drift,
        )
        DecorShape(
            shape = MaterialShapes.Cookie4Sided.toShape(),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.28f),
            alignment = Alignment.TopEnd,
            offsetX = (-40).dp,
            offsetY = 18.dp,
            size = 26.dp,
            rotation = drift,
        )

        Surface(
            modifier = Modifier
                .size(84.dp)
                .graphicsLayer {
                    scaleX = beat
                    scaleY = beat
                },
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Rounded.Favorite,
                    contentDescription = null,
                    modifier = Modifier.size(38.dp),
                )
            }
        }
    }
}

@Composable
private fun BoxScope.DecorShape(
    shape: Shape,
    color: Color,
    alignment: Alignment,
    offsetX: Dp,
    offsetY: Dp,
    size: Dp,
    rotation: Float,
) {
    Surface(
        modifier = Modifier
            .align(alignment)
            .offset(x = offsetX, y = offsetY)
            .size(size)
            .graphicsLayer { rotationZ = rotation },
        shape = shape,
        color = color,
        content = {},
    )
}
