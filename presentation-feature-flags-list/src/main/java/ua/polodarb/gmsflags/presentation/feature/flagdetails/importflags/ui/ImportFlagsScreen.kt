package ua.polodarb.gmsflags.presentation.feature.flagdetails.importflags.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalResources
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import ua.polodarb.gmsflags.presentation.feature.flagdetails.importflags.mvi.ImportFlagsEffect
import ua.polodarb.gmsflags.presentation.feature.flagdetails.importflags.mvi.ImportFlagsEvent
import ua.polodarb.gmsflags.presentation.core.ui.snackbar.LocalGmsSnackbarHostState
import ua.polodarb.gmsflags.presentation.core.ui.state.localizedDescription
import ua.polodarb.gmsflags.presentation.core.message.UiMessageType

@Composable
fun ImportFlagsScreen(
    androidPackageName: String,
    currentPhenotypePackageName: String,
    supportedPhenotypePackageNames: List<String>,
    documentUri: String,
    onBack: () -> Unit,
    onImported: (phenotypePackageName: String) -> Unit,
    onContentReady: () -> Unit = {},
) {
    val viewModel: ImportFlagsViewModel = koinViewModel(
        key = "import-flags:$androidPackageName:$documentUri",
        parameters = {
            parametersOf(
                androidPackageName,
                currentPhenotypePackageName,
                supportedPhenotypePackageNames,
                documentUri,
            )
        },
    )
    val state by viewModel.viewState.collectAsStateWithLifecycle()
    val snackbarHostState = LocalGmsSnackbarHostState.current
    val resources = LocalResources.current
    val filePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        uri?.let { viewModel.setEvent(ImportFlagsEvent.DocumentSelected(it.toString())) }
    }

    LaunchedEffect(viewModel, resources) {
        viewModel.effect.collect { effect ->
            when (effect) {
                ImportFlagsEffect.NavigateBack -> onBack()
                ImportFlagsEffect.OpenDocumentPicker -> filePicker.launch(IMPORT_MIME_TYPES)
                is ImportFlagsEffect.ImportCompleted -> onImported(effect.phenotypePackageName)
                is ImportFlagsEffect.ShowMessage -> snackbarHostState.showSnackbar(
                    message = resources.getString(effect.messageRes),
                    type = effect.type,
                )
                is ImportFlagsEffect.ShowError -> snackbarHostState.showSnackbar(
                    message = effect.error.localizedDescription(resources),
                    type = UiMessageType.Error,
                )
            }
        }
    }
    LaunchedEffect(state.loading) {
        if (!state.loading) onContentReady()
    }

    ImportFlagsContent(state = state, onEvent = viewModel::setEvent)
}

private val IMPORT_MIME_TYPES = arrayOf(
    "application/gmsflags",
    "application/xml",
    "text/xml",
    "application/octet-stream",
    "text/plain",
)
