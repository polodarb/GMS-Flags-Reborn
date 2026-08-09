package ua.polodarb.gmsflags.presentation.feature.flagdetails.ui.components.list

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import ua.polodarb.gmsflags.domain.flags.PhenotypeFlag
import ua.polodarb.gmsflags.domain.flags.FlagType
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.LocalGmsAdaptiveLayout
import ua.polodarb.gmsflags.presentation.core.ui.scrollbar.GridScrollbar
import ua.polodarb.gmsflags.presentation.feature.flagdetails.mvi.FlagDetailsEvent
import ua.polodarb.gmsflags.presentation.feature.flagdetails.mvi.FlagDetailsState
import ua.polodarb.gmsflags.presentation.feature.flagdetails.mvi.InlineFlagEditor
import ua.polodarb.gmsflags.presentation.feature.flagdetails.mvi.SelectedFlag
import ua.polodarb.gmsflags.presentation.feature.flagdetails.mvi.AppRemoteContentState
import ua.polodarb.gmsflags.presentation.feature.flagdetails.mvi.FlagFilter
import ua.polodarb.gmsflags.presentation.feature.flagdetails.ui.components.server.AppRemoteContent
import ua.polodarb.gmsflags.presentation.feature.flagdetails.ui.components.server.hasVisibleContent

private val DragSelectAutoScrollEdge = 56.dp

private const val DragSelectMaxScrollSpeedPxPerTick = 10f

internal fun allowsDragSelection(target: SelectedFlag, openEditor: InlineFlagEditor?): Boolean =
    openEditor == null || openEditor.name != target.name || openEditor.type != target.type

private class DragSelectionSession {
    var anchorKey: SelectedFlag? = null
    var lastKey: SelectedFlag? = null
    var selecting: Boolean = true
    var pointerPosition: Offset? = null
}

@Composable
internal fun FlagsGrid(
    flags: List<PhenotypeFlag>,
    state: FlagDetailsState,
    onEvent: (FlagDetailsEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val adaptiveLayout = LocalGmsAdaptiveLayout.current
    val gridState = rememberLazyGridState()
    val haptic = LocalHapticFeedback.current
    val remoteOverviewState = if (
        state.selectedType == FlagType.Boolean &&
        state.filter == FlagFilter.All &&
        state.effectiveQuery.isBlank() &&
        !state.selectionMode
    ) {
        state.remoteContent
    } else {
        AppRemoteContentState.Unavailable
    }

    val orderedKeys = remember(flags) { flags.map { SelectedFlag(it.type, it.name) } }
    val keyIndex = remember(orderedKeys) {
        orderedKeys.withIndex().associate { (index, key) -> key to index }
    }
    val flagByGridKey = remember(flags) {
        flags.associate { "${it.type}:${it.name}" to SelectedFlag(it.type, it.name) }
    }
    val selectedFlagsState = rememberUpdatedState(state.selectedFlags)
    val inlineEditorState = rememberUpdatedState(state.inlineEditor)
    val onEventState = rememberUpdatedState(onEvent)
    val autoScrollThresholdPx = with(LocalDensity.current) { DragSelectAutoScrollEdge.toPx() }
    var autoScrollSpeed by remember { mutableFloatStateOf(0f) }
    val dragSession = remember { DragSelectionSession() }

    fun flagAt(offset: Offset): SelectedFlag? {
        val items = gridState.layoutInfo.visibleItemsInfo
        if (items.isEmpty()) return null
        val exact = items.firstOrNull { info ->
            val local = offset - Offset(info.offset.x.toFloat(), info.offset.y.toFloat())
            local.x in 0f..info.size.width.toFloat() && local.y in 0f..info.size.height.toFloat()
        }
        val hit = exact ?: items.minByOrNull { info ->
            val center = Offset(
                info.offset.x + info.size.width / 2f,
                info.offset.y + info.size.height / 2f,
            )
            (center - offset).getDistanceSquared()
        }
        return flagByGridKey[hit?.key as? String]
    }

    fun applySweep(position: Offset) {
        val anchor = dragSession.anchorKey ?: return
        val pointerKey = flagAt(position) ?: return
        if (pointerKey == dragSession.lastKey) return
        val anchorIndex = keyIndex.getValue(anchor)
        val previousIndex = keyIndex.getValue(dragSession.lastKey ?: anchor)
        val pointerIndex = keyIndex.getValue(pointerKey)
        val previousSweep = orderedKeys.subList(
            minOf(anchorIndex, previousIndex),
            maxOf(anchorIndex, previousIndex) + 1,
        ).toSet()
        val currentSweep = orderedKeys.subList(
            minOf(anchorIndex, pointerIndex),
            maxOf(anchorIndex, pointerIndex) + 1,
        ).toSet()
        val base = selectedFlagsState.value
        val updated = if (dragSession.selecting) {
            (base - previousSweep) + currentSweep
        } else {
            (base + previousSweep) - currentSweep
        }
        onEventState.value(FlagDetailsEvent.DragSelectionChanged(updated))
        dragSession.lastKey = pointerKey
    }

    LaunchedEffect(autoScrollSpeed) {
        if (autoScrollSpeed != 0f) {
            while (isActive) {
                gridState.scrollBy(autoScrollSpeed)
                dragSession.pointerPosition?.let(::applySweep)
                delay(10)
            }
        }
    }

    GridScrollbar(state = gridState, modifier = modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(adaptiveLayout.flagCardMinWidth),
            state = gridState,
            modifier = Modifier
                .widthIn(max = adaptiveLayout.contentMaxWidth)
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(horizontal = adaptiveLayout.contentPadding)
                .clip(MaterialTheme.shapes.large)
                .align(Alignment.Center)
                .pointerInput(orderedKeys, flagByGridKey) {
                    coroutineScope {
                        launch {
                            detectTapGestures(
                                onTap = { offset ->
                                    flagAt(offset)?.let { key ->
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onEventState.value(FlagDetailsEvent.FlagClicked(key.name))
                                    }
                                },
                            )
                        }
                        launch {
                            detectDragGesturesAfterLongPress(
                                onDragStart = { offset ->
                                    dragSession.pointerPosition = offset
                                    flagAt(offset)
                                        ?.takeIf {
                                            allowsDragSelection(it, inlineEditorState.value)
                                        }
                                        ?.let { key ->
                                            haptic.performHapticFeedback(
                                                HapticFeedbackType.LongPress,
                                            )
                                            dragSession.anchorKey = key
                                            dragSession.lastKey = key
                                            val selected = selectedFlagsState.value
                                            dragSession.selecting = key !in selected
                                            onEventState.value(
                                                FlagDetailsEvent.DragSelectionChanged(
                                                    if (dragSession.selecting) {
                                                        selected + key
                                                    } else {
                                                        selected - key
                                                    },
                                                ),
                                            )
                                        }
                                },
                                onDragCancel = {
                                    autoScrollSpeed = 0f
                                    dragSession.anchorKey = null
                                    dragSession.lastKey = null
                                    dragSession.pointerPosition = null
                                },
                                onDragEnd = {
                                    autoScrollSpeed = 0f
                                    dragSession.anchorKey = null
                                    dragSession.lastKey = null
                                    dragSession.pointerPosition = null
                                },
                                onDrag = { change, _ ->
                                    if (dragSession.anchorKey == null) {
                                        return@detectDragGesturesAfterLongPress
                                    }
                                    dragSession.pointerPosition = change.position
                                    val distanceFromBottom = gridState.layoutInfo.viewportSize
                                        .height - change.position.y
                                    val distanceFromTop = change.position.y
                                    val proximity = when {
                                        distanceFromBottom < autoScrollThresholdPx ->
                                            (autoScrollThresholdPx - distanceFromBottom) /
                                                autoScrollThresholdPx
                                        distanceFromTop < autoScrollThresholdPx ->
                                            -(autoScrollThresholdPx - distanceFromTop) /
                                                autoScrollThresholdPx
                                        else -> 0f
                                    }
                                    autoScrollSpeed = proximity * DragSelectMaxScrollSpeedPxPerTick
                                    applySweep(change.position)
                                },
                            )
                        }
                    }
                },
            contentPadding = PaddingValues(
                top = if (state.selectionMode) adaptiveLayout.contentPadding else GmsSpacing.None,
                bottom = adaptiveLayout.contentPadding,
            ),
            horizontalArrangement = Arrangement.spacedBy(GmsSpacing.Large),
            verticalArrangement = Arrangement.spacedBy(GmsSpacing.Large),
        ) {
            if (remoteOverviewState.hasVisibleContent) {
                item(
                    key = "app_remote_content",
                    span = { GridItemSpan(maxLineSpan) },
                    contentType = "app_remote_content",
                ) {
                    AppRemoteContent(
                        state = remoteOverviewState,
                        onRetry = { onEvent(FlagDetailsEvent.RemoteContentRetry) },
                        onRecommendationClick = {
                            onEvent(FlagDetailsEvent.RecommendationClicked(it))
                        },
                        modifier = Modifier.animateItem(),
                    )
                }
            }
            items(
                items = flags,
                key = { "${it.type}:${it.name}" },
                contentType = { it.type },
            ) { flag ->
                FlagCard(
                    flag = flag,
                    annotation = (state.remoteContent as? AppRemoteContentState.Ready)
                        ?.flagAnnotations
                        ?.get(flag.name),
                    selected = SelectedFlag(flag.type, flag.name) in state.selectedFlags,
                    selectionMode = state.selectionMode,
                    enabled = !state.operationInProgress,
                    onBooleanChanged = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onEvent(FlagDetailsEvent.BooleanChanged(flag.name, it))
                    },
                    onBooleanOverrideCleared = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onEvent(FlagDetailsEvent.BooleanOverrideCleared(flag.name))
                    },
                    inlineEditor = state.inlineEditor?.takeIf {
                        it.type == flag.type && it.name == flag.name
                    },
                    onInlineValueChanged = {
                        onEvent(FlagDetailsEvent.InlineEditorValueChanged(it))
                    },
                    onInlineSave = { onEvent(FlagDetailsEvent.InlineEditorSaved) },
                    onInlineReset = { onEvent(FlagDetailsEvent.InlineEditorReset) },
                    onInlineDismiss = { onEvent(FlagDetailsEvent.InlineEditorDismissed) },
                )
            }
        }
    }
}
