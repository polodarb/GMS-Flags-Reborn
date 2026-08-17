package ua.polodarb.gmsflags.presentation.feature.flagdetails.ui.components.bottom

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsDimensions
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing

internal enum class BottomBarTransformMenuAlignment { Start, End }

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
internal fun BottomBarTransformMenu(
    enabled: Boolean,
    expandedHeight: Dp,
    alignment: BottomBarTransformMenuAlignment,
    modifier: Modifier = Modifier,
    triggerContent: @Composable BoxScope.() -> Unit,
    menuContent: @Composable (dismiss: () -> Unit) -> Unit,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    var popupVisible by remember { mutableStateOf(false) }
    var anchorSize by remember { mutableStateOf(IntSize.Zero) }
    val progress = remember { Animatable(0f) }
    val expandSpec = MaterialTheme.motionScheme.defaultSpatialSpec<Float>()
    val collapseSpec = MaterialTheme.motionScheme.fastEffectsSpec<Float>()

    LaunchedEffect(expanded) {
        if (expanded) {
            progress.snapTo(0f)
            popupVisible = true
            progress.animateTo(1f, expandSpec)
        } else if (popupVisible) {
            progress.animateTo(0f, collapseSpec)
            popupVisible = false
        }
    }

    Box(
        modifier = modifier.onSizeChanged { anchorSize = it },
    ) {
        Surface(
            onClick = { expanded = true },
            enabled = enabled,
            modifier = Modifier.fillMaxSize(),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            content = { Box(content = triggerContent) },
        )

        if (popupVisible && anchorSize != IntSize.Zero) {
            BottomBarTransformMenuPopup(
                progress = progress.value,
                anchorSize = anchorSize,
                expandedHeight = expandedHeight,
                alignment = alignment,
                onDismiss = { expanded = false },
                onAction = {
                    popupVisible = false
                    expanded = false
                },
                content = menuContent,
            )
        }
    }
}

@Composable
private fun BottomBarTransformMenuPopup(
    progress: Float,
    anchorSize: IntSize,
    expandedHeight: Dp,
    alignment: BottomBarTransformMenuAlignment,
    onDismiss: () -> Unit,
    onAction: () -> Unit,
    content: @Composable (dismiss: () -> Unit) -> Unit,
) {
    val density = LocalDensity.current
    val windowWidth = LocalConfiguration.current.screenWidthDp.dp
    val collapsedWidth = with(density) { anchorSize.width.toDp() }
    val collapsedHeight = with(density) { anchorSize.height.toDp() }
    val expandedWidth = maxOf(
        collapsedWidth,
        minOf(GmsDimensions.DetailsMenuMaxWidth, windowWidth - GmsSpacing.Huge),
    )
    val drawingMargin = GmsSpacing.ExtraLarge
    val drawingMarginPx = with(density) { drawingMargin.roundToPx() }
    val imeHeightPx = WindowInsets.ime.getBottom(density)
    val imeReservePx = if (imeHeightPx > 0) {
        imeHeightPx + with(density) { GmsSpacing.Large.roundToPx() }
    } else {
        0
    }
    val imeReserve = with(density) { imeReservePx.toDp() }
    val popupPositionProvider = remember(drawingMarginPx, alignment, imeReservePx) {
        BottomBarPopupPositionProvider(drawingMarginPx, alignment, imeReservePx)
    }
    val fittingHeight = (expandedHeight - imeReserve).coerceAtLeast(collapsedHeight)
    val animatedWidth = lerp(collapsedWidth, expandedWidth, progress)
    val animatedHeight = lerp(collapsedHeight, fittingHeight, progress)
    val animatedCorner = lerp(
        collapsedHeight / 2,
        GmsSpacing.ExtraLarge,
        progress.coerceIn(0f, 1f),
    )
    val animatedElevation = lerp(
        0.dp,
        GmsDimensions.ModalElevation,
        progress.coerceIn(0f, 1f),
    )
    val contentAlpha = ((progress - CONTENT_REVEAL_DELAY) / CONTENT_REVEAL_DURATION)
        .coerceIn(0f, 1f)

    Popup(
        popupPositionProvider = popupPositionProvider,
        onDismissRequest = onDismiss,
        properties = PopupProperties(
            focusable = true,
            clippingEnabled = false,
        ),
    ) {
        Box(
            modifier = Modifier.requiredSize(
                width = expandedWidth + drawingMargin * 2,
                height = fittingHeight + drawingMargin * 2,
            ),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(drawingMargin),
                contentAlignment = alignment.toComposeAlignment(),
            ) {
                Surface(
                    modifier = Modifier
                        .wrapContentSize(alignment.toComposeAlignment(), unbounded = true)
                        .requiredSize(
                            width = animatedWidth.coerceAtLeast(collapsedWidth),
                            height = animatedHeight.coerceAtLeast(collapsedHeight),
                        ),
                    shape = RoundedCornerShape(animatedCorner),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shadowElevation = animatedElevation,
                ) {
                    if (progress > CONTENT_REVEAL_DELAY) {
                        FixedTransformMenuContent(
                            contentWidth = expandedWidth,
                            contentHeight = fittingHeight,
                            alignment = alignment,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer { alpha = contentAlpha },
                        ) {
                            content(onAction)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FixedTransformMenuContent(
    contentWidth: Dp,
    contentHeight: Dp,
    alignment: BottomBarTransformMenuAlignment,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Layout(
        modifier = modifier,
        content = content,
    ) { measurables, constraints ->
        val contentWidthPx = contentWidth.roundToPx()
        val contentHeightPx = contentHeight.roundToPx()
        val placeable = measurables.single().measure(
            Constraints.fixed(contentWidthPx, contentHeightPx),
        )

        layout(constraints.maxWidth, constraints.maxHeight) {
            placeable.placeRelative(
                x = when (alignment) {
                    BottomBarTransformMenuAlignment.Start -> 0
                    BottomBarTransformMenuAlignment.End -> constraints.maxWidth - contentWidthPx
                },
                y = constraints.maxHeight - contentHeightPx,
            )
        }
    }
}

private class BottomBarPopupPositionProvider(
    private val drawingMarginPx: Int,
    private val alignment: BottomBarTransformMenuAlignment,
    private val imeReservePx: Int,
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val desiredX = when (alignment) {
            BottomBarTransformMenuAlignment.Start -> anchorBounds.left - drawingMarginPx
            BottomBarTransformMenuAlignment.End ->
                anchorBounds.right - popupContentSize.width + drawingMarginPx
        }
        val desiredY = anchorBounds.bottom - popupContentSize.height + drawingMarginPx
        val highestY = windowSize.height - imeReservePx - popupContentSize.height + drawingMarginPx
        return IntOffset(x = desiredX, y = minOf(desiredY, highestY))
    }
}

private fun BottomBarTransformMenuAlignment.toComposeAlignment(): Alignment = when (this) {
    BottomBarTransformMenuAlignment.Start -> Alignment.BottomStart
    BottomBarTransformMenuAlignment.End -> Alignment.BottomEnd
}

private const val CONTENT_REVEAL_DELAY = 0.2f
private const val CONTENT_REVEAL_DURATION = 0.8f

private fun Dp.coerceAtLeast(minimumValue: Dp): Dp =
    if (this < minimumValue) minimumValue else this
