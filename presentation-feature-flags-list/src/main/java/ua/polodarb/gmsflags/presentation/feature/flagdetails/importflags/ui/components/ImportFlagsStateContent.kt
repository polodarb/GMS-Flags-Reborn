package ua.polodarb.gmsflags.presentation.feature.flagdetails.importflags.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.LocalGmsAdaptiveLayout
import ua.polodarb.gmsflags.presentation.feature.flagdetails.R
import ua.polodarb.gmsflags.presentation.feature.flagdetails.importflags.mvi.ImportFlagsEvent
import ua.polodarb.gmsflags.presentation.feature.flagdetails.importflags.mvi.ImportFlagsState
import ua.polodarb.gmsflags.presentation.feature.flagdetails.importflags.mvi.PackageOverrideTarget

private enum class ImportContentState { Loading, Error, Content }

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun ImportFlagsStateContent(
    state: ImportFlagsState,
    onEvent: (ImportFlagsEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val targetState = when {
        state.loading -> ImportContentState.Loading
        state.batch != null -> ImportContentState.Content
        else -> ImportContentState.Error
    }

    AnimatedContent(
        targetState = targetState,
        modifier = modifier,
        transitionSpec = { fadeIn().togetherWith(fadeOut()) },
        label = "import_flags_state",
    ) { contentState ->
        when (contentState) {
            ImportContentState.Loading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                LoadingIndicator()
            }
            ImportContentState.Error -> ImportFlagsError(
                message = stringResource(
                    state.errorMessageRes ?: R.string.import_flags_invalid_file
                ),
                onRetry = { onEvent(ImportFlagsEvent.RetryClicked) },
                onChooseAnother = {
                    onEvent(ImportFlagsEvent.ChooseAnotherFileClicked)
                },
            )
            ImportContentState.Content -> ImportFlagsList(
                state = state,
                onEvent = onEvent,
            )
        }
    }
}

@Composable
private fun ImportFlagsList(
    state: ImportFlagsState,
    onEvent: (ImportFlagsEvent) -> Unit,
) {
    val adaptiveLayout = LocalGmsAdaptiveLayout.current
    val batch = requireNotNull(state.batch)

    LazyVerticalGrid(
        columns = GridCells.Adaptive(adaptiveLayout.flagCardMinWidth),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(adaptiveLayout.contentPadding),
        horizontalArrangement = Arrangement.spacedBy(GmsSpacing.Large),
        verticalArrangement = Arrangement.spacedBy(GmsSpacing.Large),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            ImportFlagsSummary(
                androidPackageName = state.androidPackageName,
                packageName = state.homogeneousPackageName,
                flagCount = batch.flags.size,
                selectedCount = state.selectedCount,
                skippedCount = batch.skippedFlags,
                allSelected = state.allSelected,
                enabled = !state.applying,
                onSelectAll = { onEvent(ImportFlagsEvent.SelectAllClicked) },
                onClearSelection = {
                    onEvent(ImportFlagsEvent.ClearSelectionClicked)
                },
                onPackageClick = {
                    onEvent(
                        ImportFlagsEvent.PackageOverrideRequested(PackageOverrideTarget.WholeBatch)
                    )
                },
            )
        }
        items(
            items = batch.flags,
            key = { "${it.type}:${it.name}" },
            contentType = { it.type },
        ) { flag ->
            ImportedFlagCard(
                flag = flag,
                selected = flag.key in state.selectedFlags,
                enabled = !state.applying,
                onSelectedChange = { selected ->
                    onEvent(ImportFlagsEvent.FlagSelectionChanged(flag.key, selected))
                },
                resolvedPackageName = state.effectivePackages[flag.key],
                showPackageLine = state.homogeneousPackageName == null,
                onPackageClick = {
                    onEvent(
                        ImportFlagsEvent.PackageOverrideRequested(
                            PackageOverrideTarget.SingleFlag(flag.key)
                        )
                    )
                },
            )
        }
    }
}

@Composable
private fun ImportFlagsError(
    message: String,
    onRetry: () -> Unit,
    onChooseAnother: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(GmsSpacing.ExtraLarge),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(GmsSpacing.Large),
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(
                    GmsSpacing.Small,
                    Alignment.CenterHorizontally,
                ),
            ) {
                Button(onClick = onRetry) {
                    Text(stringResource(R.string.import_flags_retry))
                }
                Button(onClick = onChooseAnother) {
                    Text(stringResource(R.string.import_flags_choose_another))
                }
            }
        }
    }
}
