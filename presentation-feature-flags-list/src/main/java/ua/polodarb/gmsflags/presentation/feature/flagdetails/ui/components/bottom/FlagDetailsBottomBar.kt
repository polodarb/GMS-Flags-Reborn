package ua.polodarb.gmsflags.presentation.feature.flagdetails.ui.components.bottom

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import ua.polodarb.gmsflags.presentation.feature.flagdetails.mvi.FlagDetailsEvent
import ua.polodarb.gmsflags.presentation.feature.flagdetails.mvi.FlagDetailsState
import ua.polodarb.gmsflags.presentation.feature.flagdetails.ui.components.selection.FlagSelectionBottomBar

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun FlagDetailsBottomBar(
    state: FlagDetailsState,
    onEvent: (FlagDetailsEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spatialSpec = MaterialTheme.motionScheme.defaultSpatialSpec<IntOffset>()
    val effectsSpec = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()
    val fastEffectsSpec = MaterialTheme.motionScheme.fastEffectsSpec<Float>()

    AnimatedContent(
        targetState = state.selectionMode,
        modifier = modifier.fillMaxWidth(),
        transitionSpec = {
            if (targetState) {
                (slideInVertically(spatialSpec) { it } + fadeIn(effectsSpec))
                    .togetherWith(
                        slideOutVertically(spatialSpec) { -it } + fadeOut(fastEffectsSpec),
                    )
            } else {
                (slideInVertically(spatialSpec) { -it } + fadeIn(effectsSpec))
                    .togetherWith(
                        slideOutVertically(spatialSpec) { it } + fadeOut(fastEffectsSpec),
                    )
            }
        },
        contentKey = { it },
        label = "flag_details_bottom_bar",
    ) { selectionMode ->
        if (selectionMode) {
            FlagSelectionBottomBar(state = state, onEvent = onEvent)
        } else {
            FlagDetailsDefaultBottomBar(
                phenotypePackageName = state.phenotypePackageName,
                primaryPhenotypePackageName = state.primaryPhenotypePackageName,
                availablePhenotypePackageNames = state.availablePhenotypePackageNames,
                enabled = !state.operationInProgress,
                onEvent = onEvent,
            )
        }
    }
}
