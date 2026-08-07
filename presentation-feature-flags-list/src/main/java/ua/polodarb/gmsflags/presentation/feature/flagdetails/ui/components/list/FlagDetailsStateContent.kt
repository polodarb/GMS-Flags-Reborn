package ua.polodarb.gmsflags.presentation.feature.flagdetails.ui.components.list

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ua.polodarb.gmsflags.domain.flags.PhenotypeFlag
import ua.polodarb.gmsflags.presentation.core.ui.animation.GmsInitialContentAnimation
import ua.polodarb.gmsflags.presentation.core.ui.loading.GmsLoadingIndicator
import ua.polodarb.gmsflags.presentation.core.ui.state.GmsEmptyContent
import ua.polodarb.gmsflags.presentation.core.ui.state.GmsErrorContent
import ua.polodarb.gmsflags.presentation.core.error.UiError
import ua.polodarb.gmsflags.presentation.feature.flagdetails.R
import ua.polodarb.gmsflags.presentation.feature.flagdetails.mvi.FlagDetailsEvent
import ua.polodarb.gmsflags.presentation.feature.flagdetails.mvi.FlagDetailsState

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalAnimationApi::class)
@Composable
internal fun FlagDetailsStateContent(
    state: FlagDetailsState,
    visibleFlags: List<PhenotypeFlag>,
    onEvent: (FlagDetailsEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val contentState = when {
        state.loading -> FlagDetailsContentState.Loading
        state.error != null -> FlagDetailsContentState.Error
        visibleFlags.isEmpty() -> FlagDetailsContentState.Empty
        else -> FlagDetailsContentState.Content
    }
    val enterEffectsSpec = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()
    val enterSpatialSpec = MaterialTheme.motionScheme.defaultSpatialSpec<Float>()
    val exitEffectsSpec = MaterialTheme.motionScheme.fastEffectsSpec<Float>()

    AnimatedContent(
        targetState = contentState,
        modifier = modifier.fillMaxSize(),
        transitionSpec = {
            (fadeIn(animationSpec = enterEffectsSpec) + scaleIn(
                initialScale = CONTENT_ENTER_SCALE,
                animationSpec = enterSpatialSpec,
            )).togetherWith(
                fadeOut(animationSpec = exitEffectsSpec),
            )
        },
        contentKey = { it },
        label = "flag_details_content_state",
    ) { targetState ->
        when (targetState) {
            FlagDetailsContentState.Loading -> GmsLoadingIndicator()
            FlagDetailsContentState.Error -> GmsErrorContent(
                error = state.error ?: UiError.Generic,
                onRetry = { onEvent(FlagDetailsEvent.Retry) },
            )
            FlagDetailsContentState.Empty -> GmsEmptyContent(
                title = stringResource(
                    if (state.effectiveQuery.isNotEmpty()) R.string.flag_list_no_matches
                    else R.string.flag_list_empty,
                ),
            )
            FlagDetailsContentState.Content -> GmsInitialContentAnimation(
                modifier = Modifier.fillMaxSize(),
            ) {
                FlagsGrid(
                    flags = visibleFlags,
                    state = state,
                    onEvent = onEvent,
                )
            }
        }
    }
}

private enum class FlagDetailsContentState {
    Loading,
    Error,
    Empty,
    Content,
}

private const val CONTENT_ENTER_SCALE = 0.98f
