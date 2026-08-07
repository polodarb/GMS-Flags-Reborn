package ua.polodarb.gmsflags.presentation.core.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally

private const val DEFAULT_SLIDE_RATIO = 0.075f

private fun slideOffset(fullWidth: Int): Int = (fullWidth * DEFAULT_SLIDE_RATIO).toInt()

fun fullScreenEnterTransition(): EnterTransition =
    slideInHorizontally(
        initialOffsetX = { fullWidth -> slideOffset(fullWidth) },
        animationSpec = tween(
            durationMillis = 350,
            delayMillis = 150,
            easing = CubicBezierEasing(0f, 0f, 0.3f, 1f),
        ),
    ) + fadeIn(
        animationSpec = tween(
            durationMillis = 250,
            delayMillis = 250,
            easing = CubicBezierEasing(0f, 0f, 0.3f, 1f),
        ),
        initialAlpha = 0f,
    )

fun fullScreenExitTransition(): ExitTransition =
    slideOutHorizontally(
        targetOffsetX = { fullWidth -> -slideOffset(fullWidth) },
        animationSpec = tween(
            durationMillis = 350,
            easing = CubicBezierEasing(0.4f, 0f, 0.3f, 1f),
        ),
    ) + fadeOut(
        animationSpec = tween(
            durationMillis = 250,
            easing = CubicBezierEasing(0.4f, 0f, 0.3f, 1f),
        ),
        targetAlpha = 0f,
    )

fun fullScreenPopEnterTransition(): EnterTransition =
    slideInHorizontally(
        initialOffsetX = { fullWidth -> -slideOffset(fullWidth) },
        animationSpec = tween(
            durationMillis = 350,
            delayMillis = 150,
            easing = CubicBezierEasing(0f, 0f, 0.3f, 1f),
        ),
    ) + fadeIn(
        animationSpec = tween(
            durationMillis = 250,
            delayMillis = 250,
            easing = CubicBezierEasing(0f, 0f, 0.3f, 1f),
        ),
        initialAlpha = 0f,
    )

fun fullScreenPopExitTransition(): ExitTransition =
    slideOutHorizontally(
        targetOffsetX = { fullWidth -> slideOffset(fullWidth) },
        animationSpec = tween(
            durationMillis = 350,
            easing = CubicBezierEasing(0.4f, 0f, 0.3f, 1f),
        ),
    ) + fadeOut(
        animationSpec = tween(
            durationMillis = 250,
            easing = CubicBezierEasing(0.4f, 0f, 0.3f, 1f),
        ),
        targetAlpha = 0f,
    )
