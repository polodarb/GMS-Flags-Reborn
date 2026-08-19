package ua.polodarb.gmsflags.presentation.feature.community.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import ua.polodarb.gmsflags.presentation.core.message.UiMessageType
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing
import ua.polodarb.gmsflags.presentation.core.ui.layout.GmsTopLevelScreen
import ua.polodarb.gmsflags.presentation.core.ui.loading.GmsLoadingIndicator
import ua.polodarb.gmsflags.presentation.core.ui.search.GmsSearchField

import ua.polodarb.gmsflags.presentation.core.ui.search.GmsSearchHeaderAction
import ua.polodarb.gmsflags.presentation.core.ui.snackbar.LocalGmsSnackbarHostState
import ua.polodarb.gmsflags.presentation.core.ui.state.GmsEmptyContent
import ua.polodarb.gmsflags.presentation.feature.community.R
import ua.polodarb.gmsflags.presentation.feature.community.mvi.CommunityEffect
import ua.polodarb.gmsflags.presentation.feature.community.mvi.CommunityEvent
import ua.polodarb.gmsflags.presentation.feature.community.mvi.CommunityState
import ua.polodarb.gmsflags.presentation.feature.community.ui.components.CommunityCard
import ua.polodarb.gmsflags.presentation.feature.community.ui.components.CommunityDetailsSheet
import ua.polodarb.gmsflags.presentation.feature.community.ui.components.CommunityDisclaimerDialog
import ua.polodarb.gmsflags.presentation.feature.community.ui.components.CommunitySubmitDialog

@Composable
fun CommunityScreen(
    onSettingsSelected: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: CommunityViewModel = koinViewModel()
    val state by viewModel.viewState.collectAsStateWithLifecycle()
    val snackbar = LocalGmsSnackbarHostState.current
    val flagsInstalledMsg = stringResource(R.string.community_flags_installed)

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is CommunityEffect.ShowSnackbar -> snackbar.showSnackbar(
                    message = effect.message,
                    type = UiMessageType.Info,
                )
                CommunityEffect.FlagsInstalled -> snackbar.showSnackbar(
                    message = flagsInstalledMsg,
                    type = UiMessageType.Success,
                )
                CommunityEffect.OpenSettings -> onSettingsSelected()
            }
        }
    }

    CommunityContent(
        state = state,
        onEvent = viewModel::setEvent,
        modifier = modifier,
    )
}

@Composable
private fun CommunityContent(
    state: CommunityState,
    onEvent: (CommunityEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler(enabled = state.searchVisible) {
        onEvent(CommunityEvent.SearchToggled)
    }

    GmsTopLevelScreen(
        onSettingsClick = { onEvent(CommunityEvent.SettingsClicked) },
        modifier = modifier,
        headerAction = {
            GmsSearchHeaderAction(
                searchVisible = state.searchVisible,
                onClick = { onEvent(CommunityEvent.SearchToggled) },
            )
        },
        supportingContent = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(),
            ) {
                if (state.searchVisible) {
                    GmsSearchField(
                        query = state.searchQuery,
                        onQueryChange = { onEvent(CommunityEvent.QueryChanged(it)) },
                        placeholder = stringResource(R.string.community_search_placeholder),
                    )
                }
            }
        },
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                state.isLoading -> GmsLoadingIndicator(modifier = Modifier.align(Alignment.Center))
                state.packages.isEmpty() -> GmsEmptyContent(
                    title = stringResource(R.string.community_empty_title),
                    description = stringResource(R.string.community_empty_description),
                    modifier = Modifier.align(Alignment.Center),
                )
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(GmsSpacing.Medium),
                ) {
                    items(
                        items = state.packages,
                        key = { it.id },
                    ) { packageItem ->
                        CommunityCard(
                            packageItem = packageItem,
                            onClick = { onEvent(CommunityEvent.PackageSelected(packageItem)) },
                        )
                    }
                }
            }
        }
    }


    val selectedPackage = state.selectedPackage
    if (selectedPackage != null) {
        CommunityDetailsSheet(
            packageItem = selectedPackage,
            onDismiss = { onEvent(CommunityEvent.PackageSheetDismissed) },
            onInstallFlags = { onEvent(CommunityEvent.InstallFlags(selectedPackage)) },
            onReport = { reason -> onEvent(CommunityEvent.ReportPackage(selectedPackage.id, reason)) },
        )
    }

    if (state.showDisclaimer) {
        CommunityDisclaimerDialog(
            onDismiss = { onEvent(CommunityEvent.DisclaimerDismissed) },
            onConfirm = { onEvent(CommunityEvent.DisclaimerDismissed) },
        )
    }

    if (state.showSubmitDialog) {
        CommunitySubmitDialog(
            initialPackageName = "",
            initialFlags = emptyList(),
            onDismiss = { onEvent(CommunityEvent.SubmitDialogDismissed) },
            onAddFlag = { _, _, _ -> },
            onRemoveFlag = { _ -> },
            onSubmit = { title, desc, pkg ->
                onEvent(
                    CommunityEvent.SubmitPackage(
                        title = title,
                        description = desc,
                        packageName = pkg,
                        flags = emptyList(),
                    )
                )
            },
        )
    }
}
