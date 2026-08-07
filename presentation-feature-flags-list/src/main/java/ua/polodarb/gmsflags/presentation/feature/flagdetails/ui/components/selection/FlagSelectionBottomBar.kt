package ua.polodarb.gmsflags.presentation.feature.flagdetails.ui.components.selection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Report
import androidx.compose.material.icons.outlined.SelectAll
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.ToggleOff
import androidx.compose.material.icons.rounded.ToggleOn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import ua.polodarb.gmsflags.domain.flags.FlagType
import ua.polodarb.gmsflags.domain.flags.PhenotypeFlag
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsDimensions
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing
import ua.polodarb.gmsflags.presentation.feature.flagdetails.R
import ua.polodarb.gmsflags.presentation.feature.flagdetails.mvi.FlagDetailsEvent
import ua.polodarb.gmsflags.presentation.feature.flagdetails.mvi.FlagDetailsState
import ua.polodarb.gmsflags.presentation.feature.flagdetails.mvi.selectedFlagValues
import ua.polodarb.gmsflags.presentation.feature.flagdetails.ui.components.bottom.FlagDetailsTransformMenuButton
import ua.polodarb.gmsflags.presentation.feature.flagdetails.ui.model.FlagDetailsMenuAction
import ua.polodarb.gmsflags.presentation.feature.flagdetails.ui.model.FlagDetailsMenuSection

@Composable
internal fun FlagSelectionBottomBar(
    state: FlagDetailsState,
    onEvent: (FlagDetailsEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val hasSelectedBooleans = state.selectedFlags.any { it.type == FlagType.Boolean }
    val hasResettableSelection = state.selectedFlagValues().any(PhenotypeFlag::overridden)
    val sections = remember(hasSelectedBooleans, hasResettableSelection) {
        selectionMenuSections(hasSelectedBooleans, hasResettableSelection)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = GmsSpacing.Large, vertical = GmsSpacing.Medium),
        horizontalArrangement = Arrangement.spacedBy(GmsSpacing.Small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier
                .weight(1f)
                .height(GmsDimensions.DetailsBottomBarHeight),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
        ) {
            Box(
                modifier = Modifier.padding(horizontal = GmsSpacing.Large),
                contentAlignment = Alignment.CenterStart,
            ) {
                Text(
                    text = pluralStringResource(
                        R.plurals.flag_details_selected_count,
                        state.selectedFlags.size,
                        state.selectedFlags.size,
                    ),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        FlagDetailsTransformMenuButton(
            enabled = !state.operationInProgress,
            sections = sections,
            buttonIcon = Icons.Rounded.MoreVert,
            buttonContentDescriptionRes = R.string.selection_options,
            onEvent = onEvent,
        )
    }
}

private fun selectionMenuSections(
    hasSelectedBooleans: Boolean,
    hasResettableSelection: Boolean,
): List<FlagDetailsMenuSection> = listOf(
    FlagDetailsMenuSection(
        actions = listOf(
            FlagDetailsMenuAction(
                labelRes = R.string.selection_enable,
                icon = Icons.Rounded.ToggleOn,
                event = FlagDetailsEvent.SetSelectedBooleans(true),
                enabled = hasSelectedBooleans,
            ),
            FlagDetailsMenuAction(
                labelRes = R.string.selection_disable,
                icon = Icons.Rounded.ToggleOff,
                event = FlagDetailsEvent.SetSelectedBooleans(false),
                enabled = hasSelectedBooleans,
            ),
            FlagDetailsMenuAction(
                labelRes = R.string.selection_reset_default,
                icon = Icons.Rounded.Refresh,
                event = FlagDetailsEvent.ResetSelectedToDefault,
                enabled = hasResettableSelection,
            ),
            FlagDetailsMenuAction(
                labelRes = R.string.selection_select_all,
                icon = Icons.Outlined.SelectAll,
                event = FlagDetailsEvent.SelectAll,
            ),
        ),
    ),
    FlagDetailsMenuSection(
        actions = listOf(
            FlagDetailsMenuAction(
                labelRes = R.string.selection_report,
                icon = Icons.Outlined.Report,
                event = FlagDetailsEvent.ReportClicked,
            ),
            FlagDetailsMenuAction(
                labelRes = R.string.selection_share,
                icon = Icons.Outlined.Share,
                event = FlagDetailsEvent.ExportClicked,
            ),
        ),
    ),
)
