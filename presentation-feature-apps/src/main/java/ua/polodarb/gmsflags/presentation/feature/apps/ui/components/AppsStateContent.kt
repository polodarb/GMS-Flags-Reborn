package ua.polodarb.gmsflags.presentation.feature.apps.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Box
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ua.polodarb.gmsflags.presentation.core.ui.loading.GmsLoadingIndicator
import ua.polodarb.gmsflags.presentation.core.ui.state.GmsEmptyContent
import ua.polodarb.gmsflags.presentation.core.ui.state.GmsErrorContent
import ua.polodarb.gmsflags.presentation.feature.apps.R
import ua.polodarb.gmsflags.presentation.feature.apps.mvi.AppsState
import ua.polodarb.gmsflags.presentation.feature.apps.ui.model.ApplicationUiModel
import ua.polodarb.gmsflags.presentation.feature.apps.mvi.visibleApplications

@Composable
internal fun AppsStateContent(
    state: AppsState,
    onRetry: () -> Unit,
    onApplicationClick: (ApplicationUiModel) -> Unit,
    onScopeHelpClick: (ApplicationUiModel) -> Unit,
    onPairipHelpClick: (ApplicationUiModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    val visibleApplications = state.visibleApplications()
    val hasApplications = state.applications.isNotEmpty()

    Box(modifier = modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = visibleApplications.isNotEmpty(),
            modifier = Modifier.fillMaxSize(),
            enter = if (state.query.isEmpty()) {
                fadeIn(animationSpec = tween(220, delayMillis = 90)) +
                    slideInVertically(
                        animationSpec = tween(220, delayMillis = 90),
                        initialOffsetY = { it / 20 },
                    )
            } else {
                EnterTransition.None
            },
            exit = fadeOut(animationSpec = tween(90)),
        ) {
            ApplicationsGrid(
                applications = visibleApplications,
                onApplicationClick = onApplicationClick,
                onScopeHelpClick = onScopeHelpClick,
                onPairipHelpClick = onPairipHelpClick,
            )
        }

        AnimatedVisibility(
            visible = state.loading && !hasApplications && state.error == null,
            modifier = Modifier.fillMaxSize(),
            enter = fadeIn(animationSpec = tween(220)),
        ) {
            GmsLoadingIndicator(Modifier.fillMaxSize())
        }

        AnimatedVisibility(
            visible = state.error != null && !hasApplications,
            modifier = Modifier.fillMaxSize(),
            enter = fadeIn(animationSpec = tween(220)),
        ) {
            GmsErrorContent(
                error = state.error ?: return@AnimatedVisibility,
                onRetry = onRetry,
                modifier = Modifier.fillMaxSize(),
            )
        }

        AnimatedVisibility(
            visible = !state.loading && state.error == null && !hasApplications,
            modifier = Modifier.fillMaxSize(),
            enter = fadeIn(animationSpec = tween(220)),
        ) {
            GmsEmptyContent(
                title = stringResource(R.string.apps_empty),
                modifier = Modifier.fillMaxSize(),
            )
        }

        AnimatedVisibility(
            visible = hasApplications && visibleApplications.isEmpty(),
            modifier = Modifier.fillMaxSize(),
            enter = fadeIn(animationSpec = tween(220, delayMillis = 75)),
        ) {
            GmsEmptyContent(
                title = stringResource(R.string.apps_search_empty),
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
