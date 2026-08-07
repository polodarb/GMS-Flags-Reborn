package ua.polodarb.gmsflags.presentation.core.ui.shape

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Position of a card within a visually connected group. Drives the corner treatment so a group of
 * adjacent cards reads as one section: the outer edges of the group keep large corners while the
 * edges that touch a neighbour tighten to a small radius (Material 3 Expressive grouped containers).
 */
enum class GmsGroupPosition { Single, Top, Middle, Bottom }

/** Resolve the [GmsGroupPosition] of the item at [index] in a group of [count] items. */
fun gmsGroupPositionOf(index: Int, count: Int): GmsGroupPosition = when {
    count <= 1 -> GmsGroupPosition.Single
    index == 0 -> GmsGroupPosition.Top
    index == count - 1 -> GmsGroupPosition.Bottom
    else -> GmsGroupPosition.Middle
}

/**
 * Corner shape for a card at [position] inside a connected group. Corners on an edge shared with a
 * neighbour use [connected]; corners on the group's outer edge use [outer]. A [GmsGroupPosition.Single]
 * card (no neighbours) is fully rounded with [outer].
 */
fun gmsGroupedCardShape(
    position: GmsGroupPosition,
    outer: Dp = 20.dp,
    connected: Dp = 6.dp,
): RoundedCornerShape = when (position) {
    GmsGroupPosition.Single -> RoundedCornerShape(outer)
    GmsGroupPosition.Top -> RoundedCornerShape(
        topStart = outer,
        topEnd = outer,
        bottomStart = connected,
        bottomEnd = connected,
    )
    GmsGroupPosition.Middle -> RoundedCornerShape(connected)
    GmsGroupPosition.Bottom -> RoundedCornerShape(
        topStart = connected,
        topEnd = connected,
        bottomStart = outer,
        bottomEnd = outer,
    )
}
