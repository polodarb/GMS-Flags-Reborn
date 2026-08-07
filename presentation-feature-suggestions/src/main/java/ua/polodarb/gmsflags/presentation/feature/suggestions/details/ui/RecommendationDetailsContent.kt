package ua.polodarb.gmsflags.presentation.feature.suggestions.details.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.GppBad
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import kotlinx.coroutines.launch
import ua.polodarb.gmsflags.analytics.AnalyticsScreen
import ua.polodarb.gmsflags.analytics.TrackScreenView
import ua.polodarb.gmsflags.domain.server.content.HookTrustStatus
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsDimensions
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import ua.polodarb.gmsflags.domain.apps.XposedScopeStatus
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.LocalGmsAdaptiveLayout
import ua.polodarb.gmsflags.presentation.core.ui.animation.GmsInitialContentAnimation
import ua.polodarb.gmsflags.presentation.core.ui.xposed.GmsScopeWarningVisibility
import ua.polodarb.gmsflags.presentation.core.ui.xposed.GmsXposedScopeActionChip
import ua.polodarb.gmsflags.presentation.core.ui.xposed.GmsXposedScopeHelpSheet
import ua.polodarb.gmsflags.presentation.core.ui.layout.GmsBackHeader
import ua.polodarb.gmsflags.presentation.core.ui.layout.GmsContentContainer
import ua.polodarb.gmsflags.presentation.core.ui.layout.GmsHeaderActionButton
import ua.polodarb.gmsflags.presentation.core.ui.loading.GmsLoadingIndicator
import ua.polodarb.gmsflags.presentation.core.ui.state.GmsErrorContent
import ua.polodarb.gmsflags.presentation.feature.suggestions.R
import ua.polodarb.gmsflags.presentation.feature.suggestions.details.mvi.RecommendationDetailsContent
import ua.polodarb.gmsflags.presentation.feature.suggestions.details.mvi.RecommendationDetailsEvent
import ua.polodarb.gmsflags.presentation.feature.suggestions.details.mvi.RecommendationDetailsState
import ua.polodarb.gmsflags.presentation.feature.suggestions.details.ui.components.FullscreenRecommendationGallery
import ua.polodarb.gmsflags.presentation.feature.suggestions.details.ui.components.RecommendationApplyBar
import ua.polodarb.gmsflags.presentation.feature.suggestions.details.ui.components.RecommendationDetailsList
import ua.polodarb.gmsflags.presentation.feature.suggestions.details.ui.components.RecommendationReportSheet

@Composable
internal fun RecommendationDetailsContent(
    state: RecommendationDetailsState,
    onEvent: (RecommendationDetailsEvent) -> Unit,
) {
    TrackScreenView(AnalyticsScreen.RecommendationDetails)

    val ready = state.content as? RecommendationDetailsContent.Ready
    val screenshots = ready?.recommendation?.screenshots.orEmpty()
    var fullscreenPage by rememberSaveable { mutableStateOf<Int?>(null) }
    val activeFullscreen = fullscreenPage?.takeIf { it in screenshots.indices }
    var lastFullscreenPage by rememberSaveable { mutableIntStateOf(0) }
    if (activeFullscreen != null) lastFullscreenPage = activeFullscreen

    val view = LocalView.current
    val fullscreenOpen = activeFullscreen != null
    DisposableEffect(view, fullscreenOpen) {
        val controller = view.context.findActivity()?.window
            ?.let { WindowInsetsControllerCompat(it, view) }
        controller?.apply {
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            if (fullscreenOpen) {
                hide(WindowInsetsCompat.Type.statusBars())
            } else {
                show(WindowInsetsCompat.Type.statusBars())
            }
        }
        onDispose { controller?.show(WindowInsetsCompat.Type.statusBars()) }
    }

    val listState = rememberLazyListState()
    val screenshotsPagerState = rememberPagerState(pageCount = { screenshots.size })

    val fadeSpec = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()
    val slideSpec = MaterialTheme.motionScheme.defaultSpatialSpec<IntOffset>()

    Box(modifier = Modifier.fillMaxSize()) {
        RecommendationDetailsScaffold(
            state = state,
            ready = ready,
            listState = listState,
            screenshotsPagerState = screenshotsPagerState,
            onEvent = onEvent,
            onScreenshotClick = { fullscreenPage = it },
        )

        AnimatedVisibility(
            visible = activeFullscreen != null,
            enter = fadeIn(fadeSpec) +
                slideInVertically(slideSpec, initialOffsetY = { it / 6 }),
            exit = slideOutVertically(slideSpec, targetOffsetY = { it / 6 }) +
                fadeOut(fadeSpec),
            modifier = Modifier.fillMaxSize(),
        ) {
            FullscreenRecommendationGallery(
                urls = screenshots,
                initialPage = activeFullscreen ?: lastFullscreenPage,
                onDismiss = { fullscreenPage = null },
            )
        }
    }

    if (state.scopeHelpVisible) {
        GmsXposedScopeHelpSheet(
            applicationName = ready?.recommendation?.applicationName
                ?: ready?.recommendation?.title.orEmpty(),
            onDismiss = { onEvent(RecommendationDetailsEvent.ScopeHelpDismissed) },
        )
    }

    if (state.applyAnywayConfirmVisible) {
        ApplyHookAnywayWarningSheet(
            onDismiss = { onEvent(RecommendationDetailsEvent.ApplyAnywayDismissed) },
            onConfirm = { onEvent(RecommendationDetailsEvent.ApplyAnywayConfirmed) },
        )
    }

    state.report?.let { report ->
        RecommendationReportSheet(
            report = report,
            onMessageChange = { onEvent(RecommendationDetailsEvent.ReportMessageChanged(it)) },
            onContactChange = { onEvent(RecommendationDetailsEvent.ReportContactChanged(it)) },
            onToggleDetails = { onEvent(RecommendationDetailsEvent.ReportDetailsToggled) },
            onSend = { onEvent(RecommendationDetailsEvent.ReportSendClicked) },
            onDismiss = { onEvent(RecommendationDetailsEvent.ReportDismissed) },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ApplyHookAnywayWarningSheet(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val hideThen: (() -> Unit) -> Unit = { action ->
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) action()
        }
    }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        contentWindowInsets = { WindowInsets.safeDrawing.only(WindowInsetsSides.Top) },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(
                    start = GmsSpacing.ExtraLarge,
                    end = GmsSpacing.ExtraLarge,
                    bottom = GmsSpacing.Small,
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(GmsSpacing.Large),
        ) {
            Surface(
                modifier = Modifier.size(GmsDimensions.OpeningIconSize),
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.GppBad,
                        contentDescription = null,
                        modifier = Modifier.size(GmsDimensions.SnackbarIconSize * 1.6f),
                    )
                }
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(GmsSpacing.Small),
            ) {
                Text(
                    text = stringResource(R.string.suggestions_details_hook_confirm_title),
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = stringResource(R.string.suggestions_details_hook_confirm_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(GmsSpacing.Small),
            ) {
                TextButton(
                    onClick = { hideThen(onConfirm) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(GmsDimensions.PrimaryActionHeight),
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                    ),
                ) {
                    Text(stringResource(R.string.suggestions_details_hook_confirm_confirm))
                }
                Button(
                    onClick = { hideThen(onDismiss) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(GmsDimensions.PrimaryActionHeight),
                ) {
                    Text(stringResource(R.string.suggestions_details_hook_confirm_cancel))
                }
            }
        }
    }
}

@Composable
private fun RecommendationDetailsScaffold(
    state: RecommendationDetailsState,
    ready: RecommendationDetailsContent.Ready?,
    listState: LazyListState,
    screenshotsPagerState: PagerState,
    onEvent: (RecommendationDetailsEvent) -> Unit,
    onScreenshotClick: (Int) -> Unit,
) {
    val adaptiveLayout = LocalGmsAdaptiveLayout.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceBright,
        topBar = {
            GmsBackHeader(
                title = stringResource(R.string.suggestions_details_title),
                onBack = { onEvent(RecommendationDetailsEvent.BackClicked) },
                trailing = ready?.let {
                    {
                        GmsHeaderActionButton(
                            icon = Icons.Outlined.BugReport,
                            contentDescription = stringResource(
                                R.string.suggestions_report_action
                            ),
                            onClick = { onEvent(RecommendationDetailsEvent.HeaderReportClicked) },
                        )
                    }
                },
            )
        },
        bottomBar = {
            ready?.let {
                val scopeExcluded = state.xposedScopeStatus == XposedScopeStatus.Excluded
                val signatureBlocked = it.recommendation.variants
                    .getOrNull(state.selectedVariantIndex)
                    ?.hooks
                    ?.any { hook ->
                        hook.hook.required &&
                            hook.trustStatus != HookTrustStatus.VERIFIED
                    } == true
                Column {
                    GmsScopeWarningVisibility(visible = scopeExcluded) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceBright)
                                .padding(horizontal = adaptiveLayout.contentPadding)
                                .padding(top = GmsSpacing.Small),
                            contentAlignment = Alignment.Center,
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .widthIn(max = adaptiveLayout.listMaxWidth),
                            ) {
                                GmsXposedScopeActionChip(
                                    label = stringResource(
                                        R.string.suggestions_details_scope_action
                                    ),
                                    onClick = {
                                        onEvent(RecommendationDetailsEvent.ScopeHelpClicked)
                                    },
                                )
                            }
                        }
                    }
                    RecommendationApplyBar(
                        recommendation = it.recommendation,
                        selectedVariantIndex = state.selectedVariantIndex,
                        applying = state.applying,
                        applicationStatus = state.applicationStatus,
                        scopeExcluded = scopeExcluded,
                        scopeStatusChecking = state.scopeStatusChecking,
                        blockedBySignature = signatureBlocked,
                        onApply = { onEvent(RecommendationDetailsEvent.ApplyClicked) },
                        onDisable = { onEvent(RecommendationDetailsEvent.DisableClicked) },
                        onLaunchApplication = {
                            onEvent(RecommendationDetailsEvent.LaunchApplicationClicked)
                        },
                    )
                }
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
                Crossfade(
                    targetState = state.content,
                    animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
                    label = "recommendation_details_state",
                ) { content ->
                    when (content) {
                        RecommendationDetailsContent.Loading -> GmsLoadingIndicator(
                            Modifier.fillMaxSize()
                        )
                        is RecommendationDetailsContent.Error -> GmsErrorContent(
                            error = content.error,
                            onRetry = { onEvent(RecommendationDetailsEvent.Retry) },
                        )
                        is RecommendationDetailsContent.Ready -> GmsInitialContentAnimation(
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            RecommendationDetailsList(
                                recommendation = content.recommendation,
                                selectedVariantIndex = state.selectedVariantIndex,
                                infoExpanded = state.infoExpanded,
                                applicationStatus = state.applicationStatus,
                                appIsOfficial = state.appIsOfficial,
                                listState = listState,
                                screenshotsPagerState = screenshotsPagerState,
                                onEvent = onEvent,
                                onScreenshotClick = onScreenshotClick,
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun Context.findActivity(): Activity? {
    var context: Context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}
