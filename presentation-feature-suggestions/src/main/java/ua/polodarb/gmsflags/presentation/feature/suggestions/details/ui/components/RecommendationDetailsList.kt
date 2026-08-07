package ua.polodarb.gmsflags.presentation.feature.suggestions.details.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.LocalGmsAdaptiveLayout
import ua.polodarb.gmsflags.domain.server.content.HookTrustStatus
import ua.polodarb.gmsflags.presentation.core.ui.shape.gmsGroupPositionOf
import ua.polodarb.gmsflags.presentation.core.ui.shape.gmsGroupedCardShape
import ua.polodarb.gmsflags.presentation.feature.suggestions.details.mvi.RecommendationDetailsEvent
import ua.polodarb.gmsflags.presentation.feature.suggestions.details.ui.model.RecommendationDetailsUiModel
import ua.polodarb.gmsflags.presentation.feature.suggestions.ui.components.common.expandCollapsePlacementSpec
import ua.polodarb.gmsflags.presentation.feature.suggestions.ui.model.RecommendationApplicationUiStatus

@Composable
internal fun RecommendationDetailsList(
    recommendation: RecommendationDetailsUiModel,
    selectedVariantIndex: Int,
    infoExpanded: Boolean,
    applicationStatus: RecommendationApplicationUiStatus,
    appIsOfficial: Boolean,
    onEvent: (RecommendationDetailsEvent) -> Unit,
    onScreenshotClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    screenshotsPagerState: PagerState =
        rememberPagerState(pageCount = recommendation.screenshots::size),
) {
    val adaptiveLayout = LocalGmsAdaptiveLayout.current
    val sidePadding = Modifier.padding(horizontal = adaptiveLayout.contentPadding)

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = adaptiveLayout.contentPadding),
        verticalArrangement = Arrangement.spacedBy(GmsSpacing.Large),
    ) {
        item(key = "hero", contentType = "hero") {
            RecommendationHeroCard(
                recommendation = recommendation,
                applicationStatus = applicationStatus,
                onExternalLinkClick = {
                    onEvent(RecommendationDetailsEvent.ExternalLinkClicked)
                },
                modifier = sidePadding,
            )
        }
        recommendation.warning?.let { warning ->
            item(key = "warning", contentType = "message") {
                RecommendationDetailsWarning(warning, modifier = sidePadding.animateItem(placementSpec = expandCollapsePlacementSpec))
            }
        }
        recommendation.infoBlock?.let { infoBlock ->
            item(key = "info", contentType = "message") {
                RecommendationInfoCard(
                    infoBlock = infoBlock,
                    expanded = infoExpanded,
                    onClick = { onEvent(RecommendationDetailsEvent.InfoBlockClicked) },
                    modifier = sidePadding.animateItem(placementSpec = expandCollapsePlacementSpec),
                )
            }
        }
        if (recommendation.screenshots.isNotEmpty()) {
            item(key = "screenshots", contentType = "screenshots") {
                RecommendationScreenshots(
                    urls = recommendation.screenshots,
                    onScreenshotClick = onScreenshotClick,
                    horizontalPadding = adaptiveLayout.contentPadding,
                    pagerState = screenshotsPagerState,
                    modifier = Modifier.animateItem(placementSpec = expandCollapsePlacementSpec),
                )
            }
        }
        item(key = "config_group", contentType = "config_group") {
            val fadeInSpec = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()
            val fadeOutSpec = MaterialTheme.motionScheme.fastEffectsSpec<Float>()
            AnimatedContent(
                targetState = selectedVariantIndex,
                transitionSpec = { fadeIn(fadeInSpec) togetherWith fadeOut(fadeOutSpec) },
                modifier = sidePadding.animateItem(placementSpec = expandCollapsePlacementSpec),
                label = "config_variant_switch",
            ) { index ->
                val variant = recommendation.variants.getOrNull(index)
                val flags = variant?.flags.orEmpty()
                val hooks = variant?.hooks.orEmpty()
                val groupCount = 1 + flags.size + hooks.size
                val blockingHook = hooks.firstOrNull {
                    it.hook.required && it.trustStatus != HookTrustStatus.VERIFIED
                }
                Column(verticalArrangement = Arrangement.spacedBy(GmsSpacing.ExtraSmall)) {
                    RecommendationVariantSection(
                        recommendation = recommendation,
                        selectedVariantIndex = index,
                        onVariantSelected = {
                            onEvent(RecommendationDetailsEvent.VariantSelected(it))
                        },
                        shape = gmsGroupedCardShape(gmsGroupPositionOf(0, groupCount)),
                    )
                    flags.forEachIndexed { i, flag ->
                        RecommendedFlagCard(
                            flag = flag,
                            defaultPackageName = recommendation.target?.phenotypePackageName,
                            shape = gmsGroupedCardShape(gmsGroupPositionOf(1 + i, groupCount)),
                            onLongClick = {
                                onEvent(RecommendationDetailsEvent.FlagNameLongClicked(flag.name))
                            },
                        )
                    }
                    hooks.forEachIndexed { i, hook ->
                        val isBlocking = hook === blockingHook
                        RecommendedHookCard(
                            hook = hook,
                            shape = gmsGroupedCardShape(
                                gmsGroupPositionOf(1 + flags.size + i, groupCount),
                            ),
                            appIntegrityBlocked = !appIsOfficial,
                            onReportProblem = if (isBlocking) {
                                { onEvent(RecommendationDetailsEvent.ReportProblemClicked) }
                            } else null,
                            onApplyAnyway = if (isBlocking) {
                                { onEvent(RecommendationDetailsEvent.ApplyAnywayClicked) }
                            } else null,
                        )
                    }
                }
            }
        }
    }
}
