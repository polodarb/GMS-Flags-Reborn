package ua.polodarb.gmsflags.presentation.feature.flagdetails.ui.components.bottom

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ua.polodarb.gmsflags.presentation.core.ui.haptic.rememberHapticClick
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsDimensions
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing
import ua.polodarb.gmsflags.presentation.feature.flagdetails.ui.model.FlagDetailsMenuAction
import ua.polodarb.gmsflags.presentation.feature.flagdetails.ui.model.FlagDetailsMenuSection

internal object TransformMenuActionList {
    fun expandedHeight(actionCount: Int, sectionCount: Int): Dp =
        GmsDimensions.MinimumTouchTarget * actionCount +
            GmsSpacing.Large +
            GmsSpacing.Medium * (sectionCount - 1).coerceAtLeast(0)
}

@Composable
internal fun TransformMenuActionList(
    sections: List<FlagDetailsMenuSection>,
    onAction: (FlagDetailsMenuAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(vertical = GmsSpacing.Small),
    ) {
        sections.forEachIndexed { sectionIndex, section ->
            section.actions.forEach { action ->
                TransformMenuActionRow(action = action, onClick = { onAction(action) })
            }
            if (sectionIndex != sections.lastIndex) {
                TransformMenuSectionDivider()
            }
        }
    }
}

@Composable
private fun TransformMenuActionRow(
    action: FlagDetailsMenuAction,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(GmsDimensions.MinimumTouchTarget)
            .padding(horizontal = GmsSpacing.Small)
            .clip(RoundedCornerShape(GmsSpacing.Large))
            .clickable(enabled = action.enabled, onClick = rememberHapticClick(onClick = onClick))
            .padding(horizontal = GmsSpacing.Medium),
        horizontalArrangement = Arrangement.spacedBy(GmsSpacing.Medium),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val alpha = if (action.enabled) 1f else DISABLED_ALPHA
        Icon(
            imageVector = action.icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha),
        )
        Text(
            text = androidx.compose.ui.res.stringResource(action.labelRes),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha),
        )
    }
}

@Composable
private fun TransformMenuSectionDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(GmsSpacing.Medium),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = GmsSpacing.Large)
                .height(2.dp)
                .background(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(
                        alpha = DIVIDER_ALPHA,
                    ),
                    shape = RoundedCornerShape(GmsSpacing.Micro),
                ),
        )
    }
}

private const val DISABLED_ALPHA = 0.38f
private const val DIVIDER_ALPHA = 0.4f
