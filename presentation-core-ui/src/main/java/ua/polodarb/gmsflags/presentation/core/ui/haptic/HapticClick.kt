package ua.polodarb.gmsflags.presentation.core.ui.haptic

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

/**
 * Wraps [onClick] so every tap fires a light haptic first, matching GMS Insight.
 *
 * [HapticFeedbackType.TextHandleMove] is the light tick used for ordinary taps; pass a stronger
 * type (e.g. [HapticFeedbackType.LongPress]) for long-press or destructive actions.
 */
@Composable
fun rememberHapticClick(
    hapticType: HapticFeedbackType = HapticFeedbackType.TextHandleMove,
    onClick: () -> Unit,
): () -> Unit {
    val haptic = LocalHapticFeedback.current
    val currentOnClick = rememberUpdatedState(onClick)
    return remember(haptic, hapticType) {
        {
            haptic.performHapticFeedback(hapticType)
            currentOnClick.value()
        }
    }
}
