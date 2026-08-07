package ua.polodarb.gmsflags.presentation.feature.suggestions.ui.components.common

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize

internal const val ExpandCollapseDurationMs = 220

internal val expandCollapseSizeSpec = tween<IntSize>(ExpandCollapseDurationMs, easing = FastOutSlowInEasing)
internal val expandCollapsePlacementSpec = tween<IntOffset>(ExpandCollapseDurationMs, easing = FastOutSlowInEasing)
