package ua.polodarb.gmsflags.presentation.feature.flagdetails.addmultiple.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalResources
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import ua.polodarb.gmsflags.presentation.feature.flagdetails.addmultiple.mvi.AddMultipleFlagsEffect
import ua.polodarb.gmsflags.presentation.core.message.UiMessageType
import ua.polodarb.gmsflags.presentation.core.ui.snackbar.LocalGmsSnackbarHostState
import ua.polodarb.gmsflags.presentation.core.ui.state.localizedDescription

@Composable
fun AddMultipleFlagsScreen(
    androidPackageName: String,
    phenotypePackageName: String,
    onBack: () -> Unit,
) {
    val viewModel: AddMultipleFlagsViewModel = koinViewModel(
        key = "add-multiple:$androidPackageName:$phenotypePackageName",
        parameters = { parametersOf(androidPackageName, phenotypePackageName) }
    )
    val state by viewModel.viewState.collectAsStateWithLifecycle()
    val snackbarHostState = LocalGmsSnackbarHostState.current
    val resources = LocalResources.current

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                AddMultipleFlagsEffect.NavigateBack -> onBack()
                is AddMultipleFlagsEffect.ShowMessage -> snackbarHostState.showSnackbar(
                    message = resources.getString(effect.messageRes),
                    type = effect.type,
                )
                is AddMultipleFlagsEffect.ShowError -> snackbarHostState.showSnackbar(
                    message = effect.error.localizedDescription(resources),
                    type = UiMessageType.Error,
                )
            }
        }
    }

    AddMultipleFlagsContent(state = state, onEvent = viewModel::setEvent)
}
