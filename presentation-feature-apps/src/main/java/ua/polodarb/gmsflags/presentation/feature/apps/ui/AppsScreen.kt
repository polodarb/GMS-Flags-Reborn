package ua.polodarb.gmsflags.presentation.feature.apps.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.Lifecycle
import org.koin.compose.viewmodel.koinViewModel
import ua.polodarb.gmsflags.presentation.feature.apps.mvi.AppsEffect
import ua.polodarb.gmsflags.presentation.feature.apps.navigation.AppsScreenAction
import ua.polodarb.gmsflags.presentation.feature.apps.mvi.AppsEvent

@Composable
fun AppsScreen(onAction: (AppsScreenAction) -> Unit) {
    val viewModel: AppsViewModel = koinViewModel()
    val state by viewModel.viewState.collectAsStateWithLifecycle()
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.setEvent(AppsEvent.Resumed)
    }
    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is AppsEffect.OpenApplication -> onAction(
                    AppsScreenAction.OpenApplication(
                        androidPackageName = effect.androidPackageName,
                        applicationName = effect.applicationName,
                        flagPackageName = effect.flagPackageName,
                        availableFlagPackageNames = effect.availableFlagPackageNames,
                    )
                )
                AppsEffect.OpenHookStatus -> onAction(AppsScreenAction.OpenHookStatus)
            }
        }
    }
    AppsContent(state = state, onEvent = viewModel::setEvent)
}
