package ua.polodarb.gmsflags.presentation.feature.hookstatus.details

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ua.polodarb.gmsflags.analytics.AnalyticsScreen
import ua.polodarb.gmsflags.analytics.TrackScreenView
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.LocalGmsAdaptiveLayout
import ua.polodarb.gmsflags.presentation.core.ui.layout.GmsBackHeader
import ua.polodarb.gmsflags.presentation.core.ui.layout.GmsContentContainer
import ua.polodarb.gmsflags.presentation.core.ui.loading.GmsLoadingIndicator
import ua.polodarb.gmsflags.presentation.core.ui.state.GmsErrorContent
import ua.polodarb.gmsflags.presentation.feature.hookstatus.R
import ua.polodarb.gmsflags.domain.hookstatus.HookApplicationStatus
import ua.polodarb.gmsflags.presentation.core.error.UiError

@Composable
internal fun HookStatusDetailsContent(
    state: HookStatusDetailsState,
    onBack: () -> Unit,
    onEvent: (HookStatusDetailsEvent) -> Unit,
) {
    TrackScreenView(AnalyticsScreen.HookStatusDetails)

    val adaptiveLayout = LocalGmsAdaptiveLayout.current
    val title = state.status?.applicationName?.let { applicationName ->
        stringResource(R.string.hook_status_details_title, applicationName)
    } ?: stringResource(R.string.hook_status_title)
    val contentState = when {
        state.loading -> DetailsContentState.Loading
        state.status == null -> DetailsContentState.Error(state.error ?: UiError.Generic)
        else -> DetailsContentState.Content(state.status)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceBright,
        topBar = { GmsBackHeader(title = title, onBack = onBack) },
        bottomBar = {
            state.status?.let { status ->
                HookStatusActionsBar(
                    restarting = state.restarting,
                    hasOverrides = status.currentOverrideCount > 0,
                    onEvent = onEvent,
                )
            }
        },
    ) { contentPadding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(contentPadding),
            contentAlignment = Alignment.Center,
        ) {
            GmsContentContainer(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = adaptiveLayout.contentMaxWidth),
            ) {
                AnimatedContent(
                    targetState = contentState,
                    contentKey = { it::class },
                    transitionSpec = { fadeIn() togetherWith fadeOut() using SizeTransform(clip = false) },
                    label = "hook-status-details-state",
                ) { contentState ->
                    when (contentState) {
                        DetailsContentState.Loading -> GmsLoadingIndicator(Modifier.fillMaxSize())
                        is DetailsContentState.Error -> GmsErrorContent(
                            error = contentState.error,
                            onRetry = { onEvent(HookStatusDetailsEvent.Retry) },
                        )
                        is DetailsContentState.Content -> HookStatusDetailsList(
                            status = contentState.status,
                            technicalDetailsExpanded = state.technicalDetailsExpanded,
                            onEvent = onEvent,
                            contentPadding = PaddingValues(adaptiveLayout.contentPadding),
                        )
                    }
                }
            }
        }
    }

    if (state.deleteOverridesConfirmVisible) {
        ConfirmDeleteApplicationOverridesDialog(
            onDismiss = { onEvent(HookStatusDetailsEvent.DeleteOverridesDismissed) },
            onConfirm = { onEvent(HookStatusDetailsEvent.DeleteOverridesConfirmed) },
        )
    }
}

private sealed interface DetailsContentState {
    data object Loading : DetailsContentState
    data class Error(val error: UiError) : DetailsContentState
    data class Content(val status: HookApplicationStatus) : DetailsContentState
}
