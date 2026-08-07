package ua.polodarb.gmsflags.presentation.core.ui.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.LocalGmsAdaptiveLayout

@Composable
fun GmsTopLevelScreen(
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
    headerAction: (@Composable RowScope.() -> Unit)? = null,
    supportingContent: (@Composable () -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val adaptiveLayout = LocalGmsAdaptiveLayout.current
    val headerState = LocalGmsTopLevelHeaderState.current
    if (headerState != null) {
        SideEffect {
            headerState.content = GmsTopLevelHeaderContent(onSettingsClick, headerAction)
        }
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceBright),
    ) {
        if (headerState == null) {
            GmsTopLevelHeaderRow(
                onSettingsClick = onSettingsClick,
                actionVisible = headerAction != null,
                action = headerAction,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
        }
        val globalNotice = LocalGmsTopLevelNotice.current
        if (globalNotice != null || supportingContent != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = adaptiveLayout.contentMaxWidth)
                    .align(Alignment.CenterHorizontally)
                    .padding(
                        start = adaptiveLayout.contentPadding,
                        end = adaptiveLayout.contentPadding,
                    ),
            ) {
                Column {
                    globalNotice?.invoke()
                    supportingContent?.invoke()
                }
            }
        }
        GmsContentContainer(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .widthIn(max = adaptiveLayout.contentMaxWidth)
                .align(Alignment.CenterHorizontally),
            content = content,
        )
    }
}

val LocalGmsTopLevelNotice = staticCompositionLocalOf<(@Composable () -> Unit)?> { null }
