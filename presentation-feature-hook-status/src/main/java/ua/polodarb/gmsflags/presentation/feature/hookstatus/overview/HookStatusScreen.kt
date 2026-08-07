package ua.polodarb.gmsflags.presentation.feature.hookstatus.overview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HookStatusScreen(
    onBack: () -> Unit,
    onApplicationSelected: (String) -> Unit,
) {
    val viewModel: HookStatusViewModel = koinViewModel()
    val state by viewModel.viewState.collectAsStateWithLifecycle()

    LifecycleResumeEffect(viewModel) {
        viewModel.setEvent(HookStatusEvent.Refresh)
        onPauseOrDispose { }
    }
    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is HookStatusEffect.OpenApplicationDetails ->
                    onApplicationSelected(effect.androidPackageName)
            }
        }
    }

    HookStatusContent(
        state = state,
        onBack = onBack,
        onEvent = viewModel::setEvent,
    )
}
