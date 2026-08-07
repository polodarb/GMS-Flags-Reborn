package ua.polodarb.gmsflags.presentation.core.ui.scrollbar

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsDimensions
import kotlin.math.ceil
import kotlin.math.roundToInt

private const val MinThumbLengthFraction = 0.08f
private const val MaxThumbLengthFraction = 0.6f
private const val HideDelayMillis = 400L
private const val DragTickMillis = 10L
private val TouchTargetWidth = 24.dp

private class DragScrollAccumulator {
    var pendingContentDeltaPx: Float = 0f
}

private fun firstVisibleRow(state: LazyGridState): Int =
    state.layoutInfo.visibleItemsInfo
        .firstOrNull { it.index == state.firstVisibleItemIndex }
        ?.row
        ?: 0

/**
 * A draggable fast-scroll thumb for long [androidx.compose.foundation.lazy.grid.LazyVerticalGrid]
 * lists.
 */
@Composable
fun GridScrollbar(
    state: LazyGridState,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable BoxScope.() -> Unit,
) {
    val thumbColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
    val density = LocalDensity.current

    var trackHeightPx by remember { mutableIntStateOf(0) }
    var isDragging by remember { mutableStateOf(false) }
    var shouldShow by remember { mutableStateOf(false) }
    val dragAccumulator = remember { DragScrollAccumulator() }

    val columns by remember {
        derivedStateOf {
            var count = 0
            for (item in state.layoutInfo.visibleItemsInfo) {
                if (item.column == count) count++ else break
            }
            count.coerceAtLeast(1)
        }
    }

    val rowAdvancePx by remember {
        derivedStateOf {
            val rows = state.layoutInfo.visibleItemsInfo
                .groupBy { it.row }
                .toSortedMap()
                .map { (_, items) -> items.minOf { it.offset.y } }
            if (rows.size >= 2) {
                (rows.last() - rows.first()).toFloat() / (rows.size - 1)
            } else {
                state.layoutInfo.visibleItemsInfo.firstOrNull()?.size?.height?.toFloat() ?: 1f
            }
        }
    }

    val totalRows by remember {
        derivedStateOf {
            ceil(state.layoutInfo.totalItemsCount.toFloat() / columns.toFloat())
        }
    }
    val estimatedContentHeightPx by remember {
        derivedStateOf { rowAdvancePx * totalRows }
    }
    val viewportHeightPx by remember {
        derivedStateOf { state.layoutInfo.viewportSize.height.toFloat() }
    }
    val canScroll by remember {
        derivedStateOf { estimatedContentHeightPx > viewportHeightPx && totalRows > 0f }
    }
    val thumbSizeFraction by remember {
        derivedStateOf {
            (viewportHeightPx / estimatedContentHeightPx.coerceAtLeast(1f))
                .coerceIn(MinThumbLengthFraction, MaxThumbLengthFraction)
        }
    }
    val thumbOffsetFraction by remember {
        derivedStateOf {
            val scrolledPx = firstVisibleRow(state) * rowAdvancePx + state.firstVisibleItemScrollOffset
            val maxScrollPx = (estimatedContentHeightPx - viewportHeightPx).coerceAtLeast(1f)
            (scrolledPx / maxScrollPx).coerceIn(0f, 1f)
        }
    }

    val thumbActive = enabled && (state.isScrollInProgress || isDragging)
    LaunchedEffect(thumbActive) {
        if (thumbActive) {
            shouldShow = true
        } else {
            delay(HideDelayMillis)
            shouldShow = false
        }
    }
    val alpha by animateFloatAsState(targetValue = if (shouldShow) 1f else 0f, label = "gridScrollbarAlpha")

    LaunchedEffect(isDragging) {
        if (isDragging) {
            while (isActive) {
                val delta = dragAccumulator.pendingContentDeltaPx
                if (delta != 0f) {
                    dragAccumulator.pendingContentDeltaPx = 0f
                    val maxOffsetPx = (estimatedContentHeightPx - viewportHeightPx).coerceAtLeast(0f)
                    val currentOffsetPx = firstVisibleRow(state) * rowAdvancePx +
                        state.firstVisibleItemScrollOffset
                    val targetOffsetPx = (currentOffsetPx + delta).coerceIn(0f, maxOffsetPx)
                    val targetRow = (targetOffsetPx / rowAdvancePx).toInt().coerceAtLeast(0)
                    val targetRowOffsetPx = (targetOffsetPx - targetRow * rowAdvancePx)
                        .roundToInt()
                        .coerceAtLeast(0)
                    state.scrollToItem(index = targetRow * columns, scrollOffset = targetRowOffsetPx)
                }
                delay(DragTickMillis)
            }
        }
    }

    Box(modifier = modifier) {
        content()
        if (enabled && canScroll) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .padding(vertical = GmsDimensions.ScrollbarVerticalInset)
                    .width(TouchTargetWidth)
                    .onSizeChanged { trackHeightPx = it.height }
                    .draggable(
                        orientation = Orientation.Vertical,
                        state = rememberDraggableState { deltaPx ->
                            if (trackHeightPx <= 0) return@rememberDraggableState
                            val contentDeltaPx = deltaPx *
                                (estimatedContentHeightPx / trackHeightPx.toFloat())
                            dragAccumulator.pendingContentDeltaPx += contentDeltaPx
                        },
                        onDragStarted = { isDragging = true },
                        onDragStopped = {
                            isDragging = false
                            dragAccumulator.pendingContentDeltaPx = 0f
                        },
                    ),
            ) {
                val thumbHeightPx = thumbSizeFraction * trackHeightPx
                val thumbOffsetPx = thumbOffsetFraction * (trackHeightPx - thumbHeightPx)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset { IntOffset(0, thumbOffsetPx.roundToInt()) }
                        .width(GmsDimensions.ScrollbarThickness)
                        .height(with(density) { thumbHeightPx.toDp() })
                        .alpha(alpha)
                        .clip(CircleShape)
                        .background(thumbColor),
                )
            }
        }
    }
}
