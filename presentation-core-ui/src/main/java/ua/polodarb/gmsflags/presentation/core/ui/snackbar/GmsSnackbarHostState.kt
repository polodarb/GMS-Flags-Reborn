package ua.polodarb.gmsflags.presentation.core.ui.snackbar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.channels.Channel
import ua.polodarb.gmsflags.presentation.core.message.UiMessageType

enum class GmsSnackbarDuration(val millis: Long) {
    Short(2_500L),
    Long(4_000L),
    Undo(3_000L),
}

internal data class GmsSnackbarMessage(
    val text: String,
    val type: UiMessageType,
    val duration: GmsSnackbarDuration,
    val actionLabel: String? = null,
    val onAction: (() -> Unit)? = null,
)

@Stable
class GmsSnackbarHostState {
    internal val messages = Channel<GmsSnackbarMessage>(capacity = Channel.CONFLATED)

    suspend fun showSnackbar(
        message: String,
        type: UiMessageType = UiMessageType.Info,
        duration: GmsSnackbarDuration = GmsSnackbarDuration.Short,
        actionLabel: String? = null,
        onAction: (() -> Unit)? = null,
    ) {
        messages.send(GmsSnackbarMessage(message, type, duration, actionLabel, onAction))
    }
}

@Composable
fun rememberGmsSnackbarHostState(): GmsSnackbarHostState = remember {
    GmsSnackbarHostState()
}

val LocalGmsSnackbarHostState = staticCompositionLocalOf<GmsSnackbarHostState> {
    GmsSnackbarHostState()
}
