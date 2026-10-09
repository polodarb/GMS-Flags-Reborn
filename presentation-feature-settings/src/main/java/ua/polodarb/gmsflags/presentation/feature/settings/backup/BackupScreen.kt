package ua.polodarb.gmsflags.presentation.feature.settings.backup

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.LocalDate
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

internal enum class BackupFlow {
    Export,
    Import,
}

@Composable
internal fun BackupScreen(flow: BackupFlow, documentUri: String? = null, onBack: () -> Unit) {
    val viewModel: BackupViewModel = koinViewModel(parameters = { parametersOf(documentUri) })
    val state by viewModel.state.collectAsStateWithLifecycle()
    val save = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/gmsbackup")
    ) {
        viewModel.save(it?.toString())
    }
    val open = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) {
        if (it != null) {
            viewModel.load(it.toString())
        }
    }
    LaunchedEffect(viewModel) {
        viewModel.saveRequest.collect {
            save.launch("gms-flags-backup-${LocalDate.now()}.gmsbackup")
        }
    }
    BackHandler(enabled = state.busy) {}
    BackupContent(
        state = state,
        flow = flow,
        onBack = onBack,
        onExport = viewModel::export,
        onImport = { open.launch(arrayOf("application/xml", "text/xml", "*/*")) },
        onRestore = viewModel::restore,
    )
}
