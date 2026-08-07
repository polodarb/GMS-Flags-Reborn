package ua.polodarb.gmsflags.presentation.feature.settings.faq

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun FaqScreen(
    onBack: () -> Unit,
) {
    val viewModel: FaqViewModel = koinViewModel()
    val state by viewModel.viewState.collectAsStateWithLifecycle()

    FaqContent(
        state = state,
        onBack = onBack,
        onEvent = viewModel::setEvent,
    )
}
