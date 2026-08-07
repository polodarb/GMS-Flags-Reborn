package ua.polodarb.gmsflags.presentation.feature.settings.overview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun SettingsScreen(
    onBack: () -> Unit,
    onHookStatus: () -> Unit,
    onOverrides: () -> Unit,
    onFaq: () -> Unit,
) {
    val viewModel: SettingsViewModel = koinViewModel()
    val state by viewModel.viewState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                SettingsEffect.OpenHookStatus -> onHookStatus()
                SettingsEffect.OpenOverrides -> onOverrides()
                SettingsEffect.OpenFaq -> onFaq()
            }
        }
    }
    SettingsContent(state = state, onBack = onBack, onEvent = viewModel::setEvent)
}
