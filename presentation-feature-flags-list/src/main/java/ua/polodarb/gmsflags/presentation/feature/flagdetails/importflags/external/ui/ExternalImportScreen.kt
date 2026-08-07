package ua.polodarb.gmsflags.presentation.feature.flagdetails.importflags.external.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import ua.polodarb.gmsflags.presentation.feature.flagdetails.importflags.external.mvi.ExternalImportEffect
import ua.polodarb.gmsflags.presentation.feature.flagdetails.importflags.external.mvi.ExternalImportTarget

@Composable
fun ExternalImportScreen(
    documentUri: String,
    onBack: () -> Unit,
    onTargetResolved: (ExternalImportTarget) -> Unit,
    onUserInteractionRequired: () -> Unit,
) {
    val viewModel: ExternalImportViewModel = koinViewModel(
        key = "external-import:$documentUri",
        parameters = { parametersOf(documentUri) },
    )
    val state by viewModel.viewState.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                ExternalImportEffect.NavigateBack -> onBack()
                is ExternalImportEffect.OpenImport -> onTargetResolved(effect.target)
            }
        }
    }
    LaunchedEffect(
        state.loading,
        state.targets,
        state.unsupportedPackageName,
        state.errorMessageRes,
    ) {
        if (
            !state.loading &&
            (state.targets.size != 1 ||
                state.unsupportedPackageName != null ||
                state.errorMessageRes != null)
        ) {
            onUserInteractionRequired()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        ExternalImportContent(
            state = state,
            onEvent = viewModel::setEvent,
        )
    }
}
