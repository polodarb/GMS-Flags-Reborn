package ua.polodarb.gmsflags.presentation.feature.community.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import ua.polodarb.gmsflags.presentation.core.message.UiMessageType
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing
import ua.polodarb.gmsflags.presentation.core.ui.loading.GmsLoadingIndicator
import ua.polodarb.gmsflags.presentation.core.ui.loading.GmsPullToRefreshBox
import ua.polodarb.gmsflags.presentation.core.ui.search.GmsSearchField
import ua.polodarb.gmsflags.presentation.core.ui.snackbar.LocalGmsSnackbarHostState
import ua.polodarb.gmsflags.presentation.core.ui.state.GmsEmptyContent
import ua.polodarb.gmsflags.presentation.feature.community.mvi.CommunityEffect
import ua.polodarb.gmsflags.presentation.feature.community.mvi.CommunityEvent
import ua.polodarb.gmsflags.presentation.feature.community.ui.components.CommunityCard
import ua.polodarb.gmsflags.presentation.feature.community.ui.components.CommunityDetailsSheet
import ua.polodarb.gmsflags.presentation.feature.community.ui.components.CommunitySubmitDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(
    onSettingsSelected: () -> Unit = {},
) {
    val viewModel: CommunityViewModel = koinViewModel()
    val state by viewModel.viewState.collectAsStateWithLifecycle()
    val snackbar = LocalGmsSnackbarHostState.current

    LaunchedEffect(viewModel) {
        viewModel.setEvent(CommunityEvent.Refresh)
        viewModel.effect.collect { effect ->
            when (effect) {
                is CommunityEffect.ShowSnackbar -> snackbar.showSnackbar(
                    message = effect.message,
                    type = UiMessageType.Info,
                )

                CommunityEffect.FlagsInstalled -> snackbar.showSnackbar(
                    message = "Flags installed successfully",
                    type = UiMessageType.Success,
                )
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Community",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.setEvent(CommunityEvent.OpenSubmitDialog()) },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ) {
                Icon(imageVector = Icons.Rounded.Add, contentDescription = "Add Flag Package")
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            GmsSearchField(
                query = state.searchQuery,
                onQueryChange = { viewModel.setEvent(CommunityEvent.SearchQueryChanged(it)) },
                placeholder = "Search flags, titles or descriptions...",
                requestFocus = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = GmsSpacing.Medium, vertical = GmsSpacing.Small),
            )

            GmsPullToRefreshBox(
                isRefreshing = state.isLoading,
                onRefresh = { viewModel.setEvent(CommunityEvent.Refresh) },
                modifier = Modifier.weight(1f),
            ) {
                when {
                    state.isLoading && state.packages.isEmpty() -> {
                        GmsLoadingIndicator()
                    }
                    state.filteredPackages.isEmpty() -> {
                        GmsEmptyContent(
                            title = "No community flags found",
                            description = "Be the first to share a flag package with the community!",
                        )
                    }
                    else -> {
                        LazyColumn(
                            contentPadding = PaddingValues(GmsSpacing.Medium),
                            verticalArrangement = Arrangement.spacedBy(GmsSpacing.Small),
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            items(
                                items = state.filteredPackages,
                                key = { it.id },
                            ) { item ->
                                CommunityCard(
                                    packageItem = item,
                                    onClick = { viewModel.setEvent(CommunityEvent.PackageSelected(item)) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    state.selectedPackage?.let { selected ->
        CommunityDetailsSheet(
            packageItem = selected,
            showDisclaimer = state.showDisclaimerDialog,
            showReportDialog = state.showReportDialog,
            onDismiss = { viewModel.setEvent(CommunityEvent.DismissDetails) },
            onDismissDisclaimer = { viewModel.setEvent(CommunityEvent.DismissDisclaimer) },
            onReport = { reason -> viewModel.setEvent(CommunityEvent.SubmitReport(reason)) },
            onOpenReportDialog = { viewModel.setEvent(CommunityEvent.ReportClicked(selected.id)) },
            onInstall = { viewModel.setEvent(CommunityEvent.InstallFlags(selected)) },
        )
    }

    if (state.showSubmitDialog) {
        CommunitySubmitDialog(
            initialPackageName = state.submitPackageName,
            initialFlags = state.submitFlags,
            onDismiss = { viewModel.setEvent(CommunityEvent.CloseSubmitDialog) },
            onAddFlag = { name, type, value -> viewModel.setEvent(CommunityEvent.AddFlagToSubmit(name, type, value)) },
            onRemoveFlag = { index -> viewModel.setEvent(CommunityEvent.RemoveFlagFromSubmit(index)) },
            onSubmit = { title, desc, pkg -> viewModel.setEvent(CommunityEvent.SubmitPackage(title, desc, pkg)) },
        )
    }
}
