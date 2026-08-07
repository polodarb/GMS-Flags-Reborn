package ua.polodarb.gmsflags.presentation.feature.flagdetails.ui.components.fab

import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Segment
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.ToggleFloatingActionButtonDefaults.animateIcon
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.unit.dp
import ua.polodarb.gmsflags.presentation.feature.flagdetails.R
import ua.polodarb.gmsflags.presentation.feature.flagdetails.mvi.FlagDetailsEvent
import ua.polodarb.gmsflags.presentation.feature.flagdetails.ui.model.flagDetailsMenuSections

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun FlagDetailsFabMenu(
    enabled: Boolean,
    onEvent: (FlagDetailsEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val menuDescription = stringResource(R.string.flag_actions)
    val closeMenu = stringResource(R.string.flag_action_close_menu)
    val expandedDescription = stringResource(R.string.flag_actions_expanded)
    val collapsedDescription = stringResource(R.string.flag_actions_collapsed)
    val actions = flagDetailsMenuSections.flatMap { it.actions }

    BackHandler(expanded) { expanded = false }

    FloatingActionButtonMenu(
        expanded = expanded,
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        button = {
            TooltipBox(
                positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                    if (expanded) TooltipAnchorPosition.Start else TooltipAnchorPosition.Above,
                ),
                tooltip = { PlainTooltip { Text(menuDescription) } },
                state = rememberTooltipState(),
            ) {
                ToggleFloatingActionButton(
                    checked = expanded,
                    onCheckedChange = { if (enabled) expanded = !expanded },
                    containerSize = {
                        72.0.dp
                    },
                    modifier = Modifier.semantics {
                        traversalIndex = -1f
                        stateDescription = if (expanded) {
                            expandedDescription
                        } else {
                            collapsedDescription
                        }
                        contentDescription = menuDescription
                    },
                ) {
                    val icon by remember {
                        derivedStateOf {
                            if (checkedProgress > 0.5f) Icons.Rounded.Close else Icons.AutoMirrored.Rounded.Segment
                        }
                    }
                    Icon(
                        painter = rememberVectorPainter(icon),
                        contentDescription = null,
                        modifier = Modifier.animateIcon({ checkedProgress }),
                    )
                }
            }
        },
    ) {
        actions.forEachIndexed { index, action ->
            FloatingActionButtonMenuItem(
                modifier = Modifier.semantics {
                    isTraversalGroup = true
                    if (index == actions.lastIndex) {
                        customActions = listOf(
                            CustomAccessibilityAction(closeMenu) {
                                expanded = false
                                true
                            }
                        )
                    }
                },
                onClick = {
                    expanded = false
                    onEvent(action.event)
                },
                icon = { Icon(action.icon, contentDescription = null) },
                text = {
                    Text(
                        text = stringResource(action.labelRes),
                        style = MaterialTheme.typography.bodyMediumEmphasized
                    )
                },
            )
        }
    }
}
