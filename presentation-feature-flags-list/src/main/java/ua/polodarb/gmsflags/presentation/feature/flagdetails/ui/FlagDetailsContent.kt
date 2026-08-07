package ua.polodarb.gmsflags.presentation.feature.flagdetails.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ua.polodarb.gmsflags.analytics.AnalyticsScreen
import ua.polodarb.gmsflags.analytics.TrackScreenView
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.LocalGmsAdaptiveLayout
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing
import ua.polodarb.gmsflags.presentation.feature.flagdetails.mvi.FlagDetailsEvent
import ua.polodarb.gmsflags.presentation.feature.flagdetails.mvi.FlagDetailsState
import ua.polodarb.gmsflags.presentation.feature.flagdetails.mvi.visibleFlags
import ua.polodarb.gmsflags.presentation.feature.flagdetails.ui.components.bottom.FlagDetailsBottomBar
import ua.polodarb.gmsflags.presentation.feature.flagdetails.ui.components.header.FlagDetailsHeader
import ua.polodarb.gmsflags.presentation.feature.flagdetails.ui.components.header.FlagTypeTabs
import ua.polodarb.gmsflags.presentation.feature.flagdetails.ui.components.layout.FlagDetailsContainer
import ua.polodarb.gmsflags.presentation.feature.flagdetails.ui.components.list.FlagDetailsStateContent
import ua.polodarb.gmsflags.presentation.feature.flagdetails.ui.sheets.BooleanControlHelpSheet
import ua.polodarb.gmsflags.presentation.feature.flagdetails.ui.overlays.FlagDetailsOverlays
import ua.polodarb.gmsflags.domain.apps.XposedScopeStatus
import ua.polodarb.gmsflags.presentation.core.ui.xposed.GmsScopeWarningVisibility
import ua.polodarb.gmsflags.presentation.core.ui.xposed.GmsPairipHelpSheet
import ua.polodarb.gmsflags.presentation.core.ui.xposed.GmsXposedScopeActionChip
import ua.polodarb.gmsflags.presentation.core.ui.xposed.GmsXposedScopeHelpSheet
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.res.stringResource
import ua.polodarb.gmsflags.presentation.feature.flagdetails.R

@Composable
internal fun FlagDetailsContent(
    state: FlagDetailsState,
    onEvent: (FlagDetailsEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    TrackScreenView(AnalyticsScreen.FlagDetails)

    val adaptiveLayout = LocalGmsAdaptiveLayout.current
    val visibleFlags = remember(
        state.flags,
        state.selectedType,
        state.filter,
        state.effectiveQuery,
    ) {
        state.visibleFlags()
    }

    BackHandler(
        enabled = state.selectionMode || state.inlineEditor != null || state.searchVisible,
    ) {
        onEvent(
            when {
                state.selectionMode -> FlagDetailsEvent.ExitSelection
                state.inlineEditor != null -> FlagDetailsEvent.InlineEditorDismissed
                else -> FlagDetailsEvent.SearchToggled
            },
        )
    }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceBright,
        topBar = {
            FlagDetailsHeader(state = state, onEvent = onEvent)
        },
    ) { contentPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = contentPadding.calculateTopPadding(),
                    bottom = GmsSpacing.None,
                ),
        ) {
            FlagDetailsContainer(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = adaptiveLayout.contentMaxWidth)
                    .align(Alignment.Center),
                bottomBar = {
                    FlagDetailsBottomBar(state = state, onEvent = onEvent)
                },
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    GmsScopeWarningVisibility(
                        visible = state.xposedScopeStatus == XposedScopeStatus.Excluded &&
                            !state.pairipIncompatible,
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    start = GmsSpacing.Large,
                                    end = GmsSpacing.Large,
                                    top = GmsSpacing.Large,
                                ),
                        ) {
                            GmsXposedScopeActionChip(
                                label = stringResource(R.string.flag_details_scope_action),
                                onClick = { onEvent(FlagDetailsEvent.ScopeHelpClicked) },
                            )
                        }
                    }
                    GmsScopeWarningVisibility(visible = state.pairipIncompatible) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    start = GmsSpacing.Large,
                                    end = GmsSpacing.Large,
                                    top = GmsSpacing.Large,
                                ),
                        ) {
                            GmsXposedScopeActionChip(
                                label = stringResource(R.string.flag_details_pairip_action),
                                onClick = { onEvent(FlagDetailsEvent.PairipHelpClicked) },
                            )
                        }
                    }
                    AnimatedVisibility(visible = !state.selectionMode) {
                        FlagTypeTabs(
                            selectedType = state.selectedType,
                            enabled = !state.operationInProgress,
                            onTypeSelected = { onEvent(FlagDetailsEvent.TypeSelected(it)) },
                            modifier = Modifier.padding(vertical = GmsSpacing.Large),
                        )
                    }
                    FlagDetailsStateContent(
                        state = state,
                        visibleFlags = visibleFlags,
                        onEvent = onEvent,
                        modifier = Modifier
                            .weight(1f),
                    )
                }
            }
        }
    }

    FlagDetailsOverlays(
        dialog = state.dialog,
        packageName = state.phenotypePackageName,
        operationInProgress = state.operationInProgress,
        bulkOperationInProgress = state.bulkOperationInProgress,
        onDismiss = { onEvent(FlagDetailsEvent.DialogDismissed) },
        onEditorNameChanged = { onEvent(FlagDetailsEvent.EditorNameChanged(it)) },
        onEditorTypeChanged = { onEvent(FlagDetailsEvent.EditorTypeChanged(it)) },
        onEditorValueChanged = { onEvent(FlagDetailsEvent.EditorValueChanged(it)) },
        onEditorSave = { onEvent(FlagDetailsEvent.EditorSaved) },
        onEditorReset = { onEvent(FlagDetailsEvent.EditorReset) },
        onDeleteAll = { onEvent(FlagDetailsEvent.DeleteAllOverridesConfirmed) },
        onExportNameChanged = { onEvent(FlagDetailsEvent.ExportFileNameChanged(it)) },
        onExport = { onEvent(FlagDetailsEvent.ExportConfirmed) },
        onReportDescriptionChanged = { onEvent(FlagDetailsEvent.ReportDescriptionChanged(it)) },
        onReport = { onEvent(FlagDetailsEvent.ReportConfirmed) },
    )
    if (state.scopeHelpVisible) {
        GmsXposedScopeHelpSheet(
            applicationName = state.applicationName,
            onDismiss = { onEvent(FlagDetailsEvent.ScopeHelpDismissed) },
        )
    }
    if (state.pairipHelpVisible) {
        GmsPairipHelpSheet(
            applicationName = state.applicationName,
            onDismiss = { onEvent(FlagDetailsEvent.PairipHelpDismissed) },
        )
    }
    if (state.booleanControlHelpVisible) {
        BooleanControlHelpSheet(
            onDismiss = { onEvent(FlagDetailsEvent.BooleanControlHelpDismissed) },
        )
    }
}
