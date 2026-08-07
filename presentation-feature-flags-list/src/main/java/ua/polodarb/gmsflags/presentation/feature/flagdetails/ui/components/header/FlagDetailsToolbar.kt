package ua.polodarb.gmsflags.presentation.feature.flagdetails.ui.components.header

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.rounded.FilterAlt
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsDimensions
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.LocalGmsAdaptiveLayout
import ua.polodarb.gmsflags.presentation.feature.flagdetails.R
import ua.polodarb.gmsflags.presentation.feature.flagdetails.mvi.FlagDetailsEvent
import ua.polodarb.gmsflags.presentation.feature.flagdetails.mvi.FlagDetailsState

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun FlagDetailsToolbar(
    state: FlagDetailsState,
    onEvent: (FlagDetailsEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val adaptiveLayout = LocalGmsAdaptiveLayout.current
    val haptic = LocalHapticFeedback.current
    val title = if (state.selectionMode) {
        pluralStringResource(
            R.plurals.flag_details_selected_count,
            state.selectedFlags.size,
            state.selectedFlags.size,
        )
    } else {
        state.applicationName
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = adaptiveLayout.contentMaxWidth)
            .height(GmsDimensions.HeaderHeight)
            .padding(horizontal = adaptiveLayout.contentPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(GmsSpacing.Small),
    ) {
        DetailsHeaderActionButton(
            icon = if (state.selectionMode) Icons.Rounded.Close
                else Icons.AutoMirrored.Rounded.ArrowBack,
            contentDescription = stringResource(
                if (state.selectionMode) R.string.flag_details_close_selection
                else R.string.flag_details_back
            ),
            onClick = {
                onEvent(
                    if (state.selectionMode) FlagDetailsEvent.ExitSelection
                    else FlagDetailsEvent.BackClicked
                )
            },
        )
        Text(
            text = title,
            modifier = Modifier
                .weight(1f)
                .combinedClickable(
                    onClick = {},
                    onLongClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onEvent(FlagDetailsEvent.PackageNameLongClicked)
                    },
                ),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (state.selectionMode) {
            DetailsHeaderActionButton(
                icon = Icons.Rounded.ContentCopy,
                contentDescription = stringResource(R.string.flag_details_copy_selected),
                onClick = { onEvent(FlagDetailsEvent.CopySelectedFlagNamesClicked) },
            )
        } else {
            DetailsHeaderActionButton(
                icon = Icons.Outlined.Info,
                contentDescription = stringResource(R.string.flag_details_control_help),
                onClick = { onEvent(FlagDetailsEvent.BooleanControlHelpClicked) },
            )
            DetailsHeaderActionButton(
                icon = Icons.Rounded.FilterAlt,
                selected = state.filtersVisible,
                contentDescription = stringResource(R.string.flag_details_filter),
                onClick = { onEvent(FlagDetailsEvent.FiltersToggled) },
            )
            DetailsHeaderActionButton(
                icon = Icons.Rounded.Search,
                selected = state.searchVisible,
                contentDescription = stringResource(R.string.flag_details_search),
                onClick = { onEvent(FlagDetailsEvent.SearchToggled) },
            )
        }
    }
}
