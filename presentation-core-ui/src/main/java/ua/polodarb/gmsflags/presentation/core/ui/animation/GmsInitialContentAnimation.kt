package ua.polodarb.gmsflags.presentation.core.ui.animation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@Composable
fun GmsInitialContentAnimation(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    var visible by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(
            animationSpec = tween(220, delayMillis = 90),
        ) + slideInVertically(
            animationSpec = tween(220, delayMillis = 90),
            initialOffsetY = { it / 20 },
        ),
    ) {
        content()
    }
}
