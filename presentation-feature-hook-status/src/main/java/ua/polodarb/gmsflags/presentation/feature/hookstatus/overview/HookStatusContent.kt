package ua.polodarb.gmsflags.presentation.feature.hookstatus.overview

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.LocalGmsAdaptiveLayout
import ua.polodarb.gmsflags.presentation.core.ui.layout.GmsBackHeader
import ua.polodarb.gmsflags.presentation.core.ui.layout.GmsContentContainer
import ua.polodarb.gmsflags.presentation.core.ui.loading.GmsLoadingIndicator
import ua.polodarb.gmsflags.presentation.core.ui.state.GmsEmptyContent
import ua.polodarb.gmsflags.presentation.core.ui.state.GmsErrorContent
import ua.polodarb.gmsflags.presentation.feature.hookstatus.R
import ua.polodarb.gmsflags.presentation.feature.hookstatus.ui.HookApplicationCard
import ua.polodarb.gmsflags.presentation.feature.hookstatus.ui.HookModuleSummaryCard
import ua.polodarb.gmsflags.analytics.AnalyticsScreen
import ua.polodarb.gmsflags.analytics.TrackScreenView
import ua.polodarb.gmsflags.domain.hookstatus.HookStatusOverview
import ua.polodarb.gmsflags.presentation.core.error.UiError

@Composable
internal fun HookStatusContent(
    state: HookStatusState,
    onBack: () -> Unit,
    onEvent: (HookStatusEvent) -> Unit,
) {
    TrackScreenView(AnalyticsScreen.HookStatus)

    val adaptiveLayout = LocalGmsAdaptiveLayout.current
    val contentState = when {
        state.loading -> ContentState.Loading
        state.error != null && state.overview == null -> ContentState.Error(state.error)
        else -> ContentState.Content(state.overview)
    }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceBright,
        topBar = {
            GmsBackHeader(
                title = stringResource(R.string.hook_status_title),
                onBack = onBack,
            )
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
                    label = "hook-status-state",
                ) { contentState ->
                    when (contentState) {
                        ContentState.Loading -> GmsLoadingIndicator(Modifier.fillMaxSize())
                        is ContentState.Error -> GmsErrorContent(
                            error = contentState.error,
                            onRetry = { onEvent(HookStatusEvent.Retry) },
                        )
                        is ContentState.Content -> {
                            val overview = contentState.overview
                            if (overview == null || overview.applications.isEmpty()) {
                                GmsEmptyContent(title = stringResource(R.string.hook_status_empty))
                            } else {
                                LazyVerticalGrid(
                                    columns = GridCells.Adaptive(
                                        adaptiveLayout.applicationCardMinWidth
                                    ),
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(
                                        adaptiveLayout.contentPadding,
                                    ),
                                    horizontalArrangement = Arrangement.spacedBy(GmsSpacing.Large),
                                    verticalArrangement = Arrangement.spacedBy(GmsSpacing.Large),
                                ) {
                                    item(span = { GridItemSpan(maxLineSpan) }) {
                                        HookModuleSummaryCard(
                                            moduleObserved = overview.moduleObserved,
                                            workingCount = overview.workingApplicationCount,
                                            attentionCount = overview.attentionApplicationCount,
                                        )
                                    }
                                    item(span = { GridItemSpan(maxLineSpan) }) {
                                        Text(
                                            text = stringResource(R.string.hook_status_applications_title),
                                            modifier = Modifier.padding(
                                                start = GmsSpacing.ExtraSmall,
                                                top = GmsSpacing.Small,
                                            ),
                                            style = MaterialTheme.typography.titleLarge,
                                        )
                                    }
                                    items(
                                        items = overview.applications,
                                        key = { it.androidPackageName },
                                    ) { application ->
                                        HookApplicationCard(
                                            status = application,
                                            onClick = {
                                                onEvent(
                                                    HookStatusEvent.ApplicationClicked(
                                                        application.androidPackageName
                                                    )
                                                )
                                            },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private sealed interface ContentState {
    data object Loading : ContentState
    data class Error(val error: UiError) : ContentState
    data class Content(val overview: HookStatusOverview?) : ContentState
}
