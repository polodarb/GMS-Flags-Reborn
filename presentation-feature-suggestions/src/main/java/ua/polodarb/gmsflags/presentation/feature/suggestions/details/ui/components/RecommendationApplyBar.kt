package ua.polodarb.gmsflags.presentation.feature.suggestions.details.ui.components

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.LoadingIndicatorDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntSize
import ua.polodarb.gmsflags.presentation.core.ui.haptic.rememberHapticClick
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsDimensions
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.LocalGmsAdaptiveLayout
import ua.polodarb.gmsflags.presentation.feature.suggestions.R
import ua.polodarb.gmsflags.presentation.feature.suggestions.details.ui.model.RecommendationApplyAvailability
import ua.polodarb.gmsflags.presentation.feature.suggestions.ui.model.RecommendationApplicationUiStatus
import ua.polodarb.gmsflags.presentation.feature.suggestions.details.ui.model.RecommendationDetailsUiModel
import ua.polodarb.gmsflags.presentation.feature.suggestions.details.ui.model.applyAvailability

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun RecommendationApplyBar(
    recommendation: RecommendationDetailsUiModel,
    selectedVariantIndex: Int,
    applying: Boolean,
    applicationStatus: RecommendationApplicationUiStatus,
    scopeExcluded: Boolean,
    scopeStatusChecking: Boolean = false,
    blockedBySignature: Boolean = false,
    onApply: () -> Unit,
    onDisable: () -> Unit,
    onLaunchApplication: () -> Unit,
) {
    val adaptiveLayout = LocalGmsAdaptiveLayout.current
    val availability = recommendation.applyAvailability(selectedVariantIndex)
    val appLaunchable = recommendation.target?.androidPackageName != null &&
        availability != RecommendationApplyAvailability.ApplicationNotInstalled &&
        availability != RecommendationApplyAvailability.ApplicationUnknown
    val isPristineApplyState = applicationStatus == RecommendationApplicationUiStatus.NotApplied &&
        availability == RecommendationApplyAvailability.Available
    val launchActionVisible = appLaunchable &&
        applicationStatus != RecommendationApplicationUiStatus.Checking &&
        !isPristineApplyState
    val effectsSpec = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()
    val fastEffectsSpec = MaterialTheme.motionScheme.fastEffectsSpec<Float>()
    val launchSpatialSpec = MaterialTheme.motionScheme.defaultSpatialSpec<IntSize>()
    val disabling = applicationStatus == RecommendationApplicationUiStatus.Applied
    val buttonColors = if (disabling) {
        ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
            disabledContainerColor = MaterialTheme.colorScheme.errorContainer,
            disabledContentColor = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f),
        )
    } else {
        ButtonDefaults.buttonColors()
    }
    val buttonContent = if (
        applying ||
        applicationStatus == RecommendationApplicationUiStatus.Checking ||
        scopeStatusChecking
    ) {
        ApplyButtonContent.Loading
    } else {
        ApplyButtonContent.Label(
            if (applicationStatus == RecommendationApplicationUiStatus.Applied) {
                R.string.suggestions_details_disable
            } else {
                availability.actionLabelRes(applicationStatus)
            }
        )
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceBright)
            .padding(horizontal = adaptiveLayout.contentPadding, vertical = GmsSpacing.Small)
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = adaptiveLayout.listMaxWidth),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Button(
                onClick = rememberHapticClick(
                    onClick = if (
                        applicationStatus == RecommendationApplicationUiStatus.Applied
                    ) onDisable else onApply,
                ),
                enabled = (availability == RecommendationApplyAvailability.Available ||
                    applicationStatus == RecommendationApplicationUiStatus.Applied) &&
                    applicationStatus != RecommendationApplicationUiStatus.Checking &&
                    !scopeStatusChecking &&
                    !applying &&
                    (!scopeExcluded ||
                        applicationStatus == RecommendationApplicationUiStatus.Applied) &&
                    (!blockedBySignature ||
                        applicationStatus == RecommendationApplicationUiStatus.Applied),
                modifier = Modifier
                    .weight(1f)
                    .height(GmsDimensions.PrimaryActionHeight),
                colors = buttonColors,
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    AnimatedContent(
                        targetState = buttonContent,
                        transitionSpec = {
                            fadeIn(effectsSpec)
                                .togetherWith(fadeOut(fastEffectsSpec))
                                .using(SizeTransform(clip = false))
                        },
                        contentAlignment = Alignment.Center,
                        contentKey = { it },
                        label = "recommendation_apply_button_content",
                    ) { content ->
                        when (content) {
                            ApplyButtonContent.Loading -> LoadingIndicator(
                                modifier = Modifier.size(GmsDimensions.SelectionIndicatorSize),
                                color = if (disabling) {
                                    MaterialTheme.colorScheme.onErrorContainer
                                } else {
                                    LoadingIndicatorDefaults.indicatorColor
                                },
                            )
                            is ApplyButtonContent.Label -> Text(stringResource(content.textRes))
                        }
                    }
                }
            }
            AnimatedVisibility(
                visible = launchActionVisible,
                enter = fadeIn(effectsSpec) + expandHorizontally(
                    animationSpec = launchSpatialSpec,
                    expandFrom = Alignment.End,
                ),
                exit = shrinkHorizontally(
                    animationSpec = launchSpatialSpec,
                    shrinkTowards = Alignment.End,
                ) + fadeOut(fastEffectsSpec),
            ) {
                FilledTonalIconButton(
                    onClick = rememberHapticClick(onClick = onLaunchApplication),
                    modifier = Modifier
                        .padding(start = GmsSpacing.Small)
                        .size(GmsDimensions.PrimaryActionHeight),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PlayArrow,
                        contentDescription = stringResource(
                            R.string.suggestions_details_launch_application
                        ),
                    )
                }
            }
        }
    }
}

private sealed interface ApplyButtonContent {
    data object Loading : ApplyButtonContent
    data class Label(@param:StringRes val textRes: Int) : ApplyButtonContent
}

private fun RecommendationApplyAvailability.actionLabelRes(
    applicationStatus: RecommendationApplicationUiStatus,
): Int = when (this) {
    RecommendationApplyAvailability.Available -> when (applicationStatus) {
        RecommendationApplicationUiStatus.Applied -> R.string.suggestions_details_disable
        RecommendationApplicationUiStatus.PartiallyApplied ->
            R.string.suggestions_details_restore
        else -> R.string.suggestions_details_apply
    }
    RecommendationApplyAvailability.ApplicationNotInstalled ->
        R.string.suggestions_details_not_installed
    RecommendationApplyAvailability.VersionUnsupported ->
        R.string.suggestions_details_version_unsupported
    RecommendationApplyAvailability.ContainsUnsupportedFlags ->
        R.string.suggestions_details_unsupported_types
    RecommendationApplyAvailability.NoFlags -> R.string.suggestions_details_no_flags
    RecommendationApplyAvailability.ApplicationUnknown,
    RecommendationApplyAvailability.FlagPackageUnavailable,
    -> R.string.suggestions_details_unavailable
}
