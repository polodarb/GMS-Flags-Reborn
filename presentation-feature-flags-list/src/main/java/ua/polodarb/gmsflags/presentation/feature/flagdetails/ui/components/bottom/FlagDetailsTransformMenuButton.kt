package ua.polodarb.gmsflags.presentation.feature.flagdetails.ui.components.bottom

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsDimensions
import ua.polodarb.gmsflags.presentation.feature.flagdetails.R
import ua.polodarb.gmsflags.presentation.feature.flagdetails.mvi.FlagDetailsEvent
import ua.polodarb.gmsflags.presentation.feature.flagdetails.ui.model.FlagDetailsMenuSection
import ua.polodarb.gmsflags.presentation.feature.flagdetails.ui.model.flagDetailsMenuSections

@Composable
internal fun FlagDetailsTransformMenuButton(
    enabled: Boolean,
    onEvent: (FlagDetailsEvent) -> Unit,
    modifier: Modifier = Modifier,
    sections: List<FlagDetailsMenuSection> = flagDetailsMenuSections,
    buttonIcon: ImageVector = Icons.Default.MoreVert,
    buttonContentDescriptionRes: Int = R.string.flag_actions,
) {
    val actionCount = sections.sumOf { it.actions.size }
    val expandedHeight = TransformMenuActionList.expandedHeight(
        actionCount = actionCount,
        sectionCount = sections.size,
    )

    BottomBarTransformMenu(
        enabled = enabled,
        expandedHeight = expandedHeight,
        alignment = BottomBarTransformMenuAlignment.End,
        modifier = modifier.size(GmsDimensions.DetailsBottomBarHeight),
        triggerContent = {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = buttonIcon,
                    contentDescription = stringResource(buttonContentDescriptionRes),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
    ) { dismiss ->
        TransformMenuActionList(
            sections = sections,
            onAction = { action ->
                dismiss()
                onEvent(action.event)
            },
        )
    }
}
