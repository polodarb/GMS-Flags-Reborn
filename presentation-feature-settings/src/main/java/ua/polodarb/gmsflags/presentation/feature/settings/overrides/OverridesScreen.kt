package ua.polodarb.gmsflags.presentation.feature.settings.overrides

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.LocalDate
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import ua.polodarb.gmsflags.presentation.core.message.UiMessageType
import ua.polodarb.gmsflags.presentation.core.ui.snackbar.LocalGmsSnackbarHostState
import ua.polodarb.gmsflags.presentation.core.ui.state.localizedDescription
import ua.polodarb.gmsflags.presentation.feature.settings.R
import ua.polodarb.gmsflags.presentation.feature.settings.backup.BackupContent
import ua.polodarb.gmsflags.presentation.feature.settings.backup.BackupFlow
import ua.polodarb.gmsflags.presentation.feature.settings.backup.BackupSheet
import ua.polodarb.gmsflags.presentation.feature.settings.backup.BackupViewModel

@Composable
internal fun OverridesScreen(
    onBack: () -> Unit,
    onImportSelected: (String) -> Unit,
    onImportBackup: () -> Unit,
    initialBackupSheetVisible: Boolean = false,
) {
    var backupSheetVisible by rememberSaveable { mutableStateOf(initialBackupSheetVisible) }
    val backupViewModel: BackupViewModel = koinViewModel(
        key = "overrides-backup-export",
        parameters = { parametersOf(null as String?) },
    )
    val backupState by backupViewModel.state.collectAsStateWithLifecycle()
    val saveBackup = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/gmsbackup")
    ) { uri ->
        if (uri == null) {
            backupSheetVisible = false
        }
        backupViewModel.save(uri?.toString())
    }
    LaunchedEffect(backupViewModel) {
        backupViewModel.saveRequest.collect {
            backupViewModel.pickerLaunched()
            saveBackup.launch("gms-flags-backup-${LocalDate.now()}.gmsbackup")
        }
    }
    BackHandler(enabled = backupState.busy) {}
    LaunchedEffect(backupState.summaryVisible) {
        if (backupState.summaryVisible) {
            backupSheetVisible = false
        }
    }
    if (backupState.summaryVisible) {
        BackHandler(onBack = backupViewModel::dismissSummary)
        BackupContent(
            state = backupState,
            flow = BackupFlow.Export,
            onBack = backupViewModel::dismissSummary,
            onExport = backupViewModel::export,
            onImport = {},
            onRestore = {},
        )
        return
    }

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
                OverridesEffect.OpenImport ->
                    picker.launch(arrayOf("application/xml", "text/xml", "*/*"))
                OverridesEffect.Updated -> Unit
                OverridesEffect.Deleted ->
                    snackbar.showSnackbar(deletedMessage, UiMessageType.Success)
                is OverridesEffect.Failed ->
                    snackbar.showSnackbar(
                        effect.error.localizedDescription(resources),
                        UiMessageType.Error,
                    )
            }
        }
    }
    OverridesContent(
        state = state,
        onBack = onBack,
        onEvent = viewModel::setEvent,
        onBackup = { backupSheetVisible = true },
    )
    if (backupSheetVisible) {
        BackupSheet(
            onDismiss = { backupSheetVisible = false },
            onExport = backupViewModel::export,
            state = backupState,
            onImport = onImportBackup,
        )
    }
}
