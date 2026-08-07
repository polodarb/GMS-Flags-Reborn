package ua.polodarb.gmsflags.presentation.feature.settings.overrides

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalResources
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import ua.polodarb.gmsflags.presentation.core.message.UiMessageType
import ua.polodarb.gmsflags.presentation.core.ui.snackbar.LocalGmsSnackbarHostState
import ua.polodarb.gmsflags.presentation.feature.settings.R
import ua.polodarb.gmsflags.presentation.core.ui.state.localizedDescription

@Composable
internal fun OverridesScreen(
    onBack: () -> Unit,
    onImportSelected: (String) -> Unit,
) {
    val viewModel: OverridesViewModel = koinViewModel()
    val state by viewModel.viewState.collectAsStateWithLifecycle()
    val snackbar = LocalGmsSnackbarHostState.current
    val resources = LocalResources.current
    val deletedMessage = stringResource(R.string.settings_removed_message)
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { onImportSelected(it.toString()) }
    }

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                OverridesEffect.OpenImport -> picker.launch(arrayOf("application/xml", "text/xml", "*/*"))
                OverridesEffect.Updated -> Unit
                OverridesEffect.Deleted -> snackbar.showSnackbar(deletedMessage, UiMessageType.Success)
                is OverridesEffect.Failed -> snackbar.showSnackbar(
                    effect.error.localizedDescription(resources),
                    UiMessageType.Error,
                )
            }
        }
    }
    OverridesContent(state = state, onBack = onBack, onEvent = viewModel::setEvent)
}
