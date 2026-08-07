package ua.polodarb.gmsflags.presentation.feature.suggestions.ui
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import androidx.compose.runtime.LaunchedEffect
import ua.polodarb.gmsflags.presentation.feature.suggestions.mvi.SuggestionsEffect
import androidx.compose.ui.platform.LocalResources
import ua.polodarb.gmsflags.presentation.core.message.UiMessageType
import ua.polodarb.gmsflags.presentation.core.ui.snackbar.GmsSnackbarDuration
import ua.polodarb.gmsflags.presentation.core.ui.snackbar.LocalGmsSnackbarHostState
import ua.polodarb.gmsflags.presentation.core.ui.state.localizedDescription
import ua.polodarb.gmsflags.presentation.feature.suggestions.R
import ua.polodarb.gmsflags.presentation.feature.suggestions.mvi.SuggestionsEvent

@Composable
fun SuggestionsScreen(
    onRecommendationSelected: (Long) -> Unit,
    onSettingsSelected: () -> Unit,
) {
    val viewModel: SuggestionsViewModel = koinViewModel()
    val state by viewModel.viewState.collectAsStateWithLifecycle()
    val snackbar = LocalGmsSnackbarHostState.current
    val resources = LocalResources.current
    val gridState = rememberLazyGridState()
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is SuggestionsEffect.OpenRecommendation -> onRecommendationSelected(effect.id)
                is SuggestionsEffect.ShowError -> snackbar.showSnackbar(
                    message = effect.error.localizedDescription(resources),
                    type = UiMessageType.Error,
                )
                is SuggestionsEffect.ShowTopDismissUndo -> snackbar.showSnackbar(
                    message = resources.getString(
                        R.string.suggestions_recommendation_moved_to_stack,
                        effect.title,
                    ),
                    duration = GmsSnackbarDuration.Undo,
                    actionLabel = resources.getString(R.string.suggestions_undo_action),
                    onAction = {
                        viewModel.setEvent(
                            SuggestionsEvent.RecommendationTopUndoClicked(effect.recommendationId)
                        )
                        if (gridState.firstVisibleItemIndex != 0 ||
                            gridState.firstVisibleItemScrollOffset != 0
                        ) {
                            coroutineScope.launch { gridState.animateScrollToItem(0) }
                        }
                    },
                )
            }
        }
    }
    SuggestionsContent(
        state = state,
        onEvent = viewModel::setEvent,
        onSettingsClick = onSettingsSelected,
        gridState = gridState,
    )
}
