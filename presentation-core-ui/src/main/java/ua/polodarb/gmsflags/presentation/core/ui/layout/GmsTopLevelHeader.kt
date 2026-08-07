package ua.polodarb.gmsflags.presentation.core.ui.layout

import ua.polodarb.gmsflags.presentation.core.ui.branding.GmsBrandGenerationPlacement
import ua.polodarb.gmsflags.presentation.core.ui.branding.GmsBrandWordmark
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ua.polodarb.gmsflags.presentation.core.ui.R
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsDimensions
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.LocalGmsAdaptiveLayout

/**
 * What the currently visible top-level tab wants shown in the shared header: the gear action (which
 * differs per tab) and an optional leading action such as search. [action] is null when the tab has
 * no leading action, which is what the header animates on.
 */
class GmsTopLevelHeaderContent(
    val onSettingsClick: () -> Unit,
    val action: (@Composable RowScope.() -> Unit)?,
)

/** Lets the active tab report its header actions up to the single header hosted above the tabs. */
class GmsTopLevelHeaderState {
    var content by mutableStateOf<GmsTopLevelHeaderContent?>(null)
}

val LocalGmsTopLevelHeaderState = staticCompositionLocalOf<GmsTopLevelHeaderState?> { null }

/**
 * The one header rendered above the tab pager. Because it outlives tab switches (unlike a per-tab
 * header), the leading action can animate in and out as you move between tabs that do and don't
 * offer one.
 */
@Composable
fun GmsTopLevelHeaderHost(
    state: GmsTopLevelHeaderState,
    modifier: Modifier = Modifier,
) {
    val content = state.content
    val action = content?.action
    var lastAction by remember { mutableStateOf<(@Composable RowScope.() -> Unit)?>(null) }
    SideEffect { if (action != null) lastAction = action }

    GmsTopLevelHeaderRow(
        onSettingsClick = content?.onSettingsClick ?: {},
        actionVisible = action != null,
        action = lastAction,
        modifier = modifier,
    )
}

/** Shared markup for the top-level header, also used as an inline fallback with no host. */
@Composable
internal fun GmsTopLevelHeaderRow(
    onSettingsClick: () -> Unit,
    actionVisible: Boolean,
    action: (@Composable RowScope.() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val adaptiveLayout = LocalGmsAdaptiveLayout.current
    val effects = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = adaptiveLayout.contentMaxWidth)
            .height(GmsDimensions.HeaderHeight)
            .padding(horizontal = adaptiveLayout.contentPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        GmsBrandWordmark(
            modifier = Modifier.weight(1f),
            generationColor = MaterialTheme.colorScheme.primary,
            generationPlacement = GmsBrandGenerationPlacement.Inline,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(GmsSpacing.Small),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AnimatedVisibility(
                visible = actionVisible,
                enter = fadeIn(effects) + scaleIn(effects, initialScale = 0.85f),
                exit = fadeOut(effects) + scaleOut(effects, targetScale = 0.85f),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    action?.invoke(this)
                }
            }
            GmsHeaderActionButton(
                icon = Icons.Outlined.Settings,
                contentDescription = stringResource(R.string.gms_settings),
                onClick = onSettingsClick,
            )
        }
    }
}
