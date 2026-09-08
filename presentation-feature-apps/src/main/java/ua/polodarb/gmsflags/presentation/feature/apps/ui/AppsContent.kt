package ua.polodarb.gmsflags.presentation.feature.apps.ui

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ua.polodarb.gmsflags.presentation.core.ui.loading.GmsPullToRefreshBox
import ua.polodarb.gmsflags.presentation.core.ui.layout.GmsTopLevelScreen
import ua.polodarb.gmsflags.presentation.feature.apps.R
import ua.polodarb.gmsflags.presentation.feature.apps.mvi.AppsEvent
import ua.polodarb.gmsflags.presentation.feature.apps.mvi.AppsState
import ua.polodarb.gmsflags.presentation.feature.apps.ui.components.AppsStateContent
import ua.polodarb.gmsflags.presentation.feature.apps.ui.components.ModuleScopeWarningCard
import ua.polodarb.gmsflags.presentation.feature.apps.ui.components.OfflineBadge
import ua.polodarb.gmsflags.presentation.feature.apps.ui.components.UnsupportedAppSheet
import ua.polodarb.gmsflags.domain.apps.XposedModuleStatus
import ua.polodarb.gmsflags.presentation.core.ui.xposed.GmsPairipHelpSheet
import ua.polodarb.gmsflags.presentation.core.ui.xposed.GmsScopeWarningVisibility
import ua.polodarb.gmsflags.presentation.core.ui.xposed.GmsXposedScopeHelpMode
import ua.polodarb.gmsflags.presentation.core.ui.xposed.GmsXposedScopeHelpSheet
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.animation.animateContentSize
import ua.polodarb.gmsflags.presentation.feature.apps.ui.components.AppsSearchBar
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.IntSize
import ua.polodarb.gmsflags.analytics.AnalyticsScreen
import ua.polodarb.gmsflags.analytics.TrackScreenView
import ua.polodarb.gmsflags.presentation.core.ui.search.GmsSearchHeaderAction

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AppsContent(
    state: AppsState,
    onEvent: (AppsEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    TrackScreenView(AnalyticsScreen.Apps)

    val searchSizeSpec = MaterialTheme.motionScheme.slowSpatialSpec<IntSize>()
    val searchEffectsSpec = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()

    BackHandler(enabled = state.searchVisible) {
        onEvent(AppsEvent.SearchToggled)
    }

    GmsTopLevelScreen(
        onSettingsClick = { onEvent(AppsEvent.SettingsClicked) },
        modifier = modifier,
        headerAction = {
            if (state.offline) {
                OfflineBadge(
                    label = state.offlineBadge,
                    modifier = Modifier.padding(end = GmsSpacing.Small),
                )
            }
            GmsSearchHeaderAction(
                searchVisible = state.searchVisible,
                onClick = { onEvent(AppsEvent.SearchToggled) },
            )
        },
        supportingContent = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(
                        animationSpec = searchSizeSpec,
                        alignment = Alignment.TopCenter,
                    ),
            ) {
                GmsScopeWarningVisibility(
                    visible = state.moduleStatus == XposedModuleStatus.Disabled,
                ) {
                    ModuleScopeWarningCard(
                        onClick = { onEvent(AppsEvent.ModuleScopeHelpClicked) },
                        modifier = Modifier.padding(bottom = GmsSpacing.Small),
                    )
                }
                AnimatedVisibility(
                    visible = state.searchVisible,
                    enter = fadeIn(animationSpec = searchEffectsSpec),
                    exit = fadeOut(animationSpec = searchEffectsSpec),
                ) {
                    AppsSearchBar(
                        query = state.query,
                        onQueryChanged = { onEvent(AppsEvent.QueryChanged(it)) },
                    )
                }
            }
        },
    ) {
        GmsPullToRefreshBox(
            isRefreshing = state.refreshing,
            onRefresh = { onEvent(AppsEvent.Refresh) },
        ) {
            AppsStateContent(
                state = state,
                onRetry = { onEvent(AppsEvent.Retry) },
                onApplicationClick = {
                    onEvent(AppsEvent.ApplicationClicked(it.androidPackageName))
                },
                onScopeHelpClick = {
                    onEvent(AppsEvent.ScopeHelpClicked(it.androidPackageName))
                },
                onPairipHelpClick = {
                    onEvent(AppsEvent.PairipHelpClicked(it.androidPackageName))
                },
            )
        }
    }

    state.scopeHelpApplication?.let { application ->
        GmsXposedScopeHelpSheet(
            applicationName = application.name,
            onDismiss = { onEvent(AppsEvent.ScopeHelpDismissed) },
        )
    }
    if (state.moduleScopeHelpVisible) {
        GmsXposedScopeHelpSheet(
            applicationName = stringResource(R.string.apps_module_name),
            mode = GmsXposedScopeHelpMode.Module,
            onDismiss = { onEvent(AppsEvent.ModuleScopeHelpDismissed) },
        )
    }
    state.unsupportedApplication?.let { application ->
        UnsupportedAppSheet(
            application = application,
            onDismiss = { onEvent(AppsEvent.UnsupportedDismissed) },
        )
    }
    state.pairipHelpApplication?.let { application ->
        GmsPairipHelpSheet(
            applicationName = application.name,
            onDismiss = { onEvent(AppsEvent.PairipHelpDismissed) },
        )
    }
}
