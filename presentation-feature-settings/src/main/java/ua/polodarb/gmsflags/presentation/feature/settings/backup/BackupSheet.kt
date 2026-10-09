package ua.polodarb.gmsflags.presentation.feature.settings.backup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.FileUpload
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import kotlinx.coroutines.launch
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing
import ua.polodarb.gmsflags.presentation.feature.settings.R
import ua.polodarb.gmsflags.presentation.feature.settings.ui.SettingsRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BackupSheet(
    onDismiss: () -> Unit,
    onExport: () -> Unit,
    onImport: () -> Unit,
    state: BackupState,
) {
    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded),
    )
    val scope = rememberCoroutineScope()
    var openingScreen by remember { mutableStateOf(false) }

    fun openScreen(action: () -> Unit) {
        if (openingScreen) {
            return
        }
        openingScreen = true
        scope.launch {
            sheetState.hide()
            if (!sheetState.isVisible) {
                onDismiss()
                action()
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = {
            if (!state.busy) {
                onDismiss()
            }
        },
        sheetState = sheetState,
        contentWindowInsets = { WindowInsets.safeDrawing.only(WindowInsetsSides.Top) },
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .padding(horizontal = GmsSpacing.ExtraLarge, vertical = GmsSpacing.Small),
            verticalArrangement = Arrangement.spacedBy(GmsSpacing.Large),
        ) {
            Text(
                text = stringResource(R.string.backup_title),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.semantics { heading() },
            )
            Text(
                text = stringResource(R.string.backup_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
            ) {
                Column {
                    SettingsRow(
                        title = stringResource(R.string.backup_export),
                        description = stringResource(R.string.backup_export_description),
                        icon = Icons.Rounded.FileUpload,
                        onClick = if (openingScreen || state.busy) null else { { onExport() } },
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = GmsSpacing.Large),
                        color = MaterialTheme.colorScheme.outlineVariant,
                    )
                    SettingsRow(
                        title = stringResource(R.string.backup_import),
                        description = stringResource(R.string.backup_import_description),
                        icon = Icons.Rounded.FileDownload,
                        onClick = if (openingScreen || state.busy) null else { { openScreen(onImport) } },
                    )
                }
            }
            if (state.busy) {
                BackupProgressCard()
            }
            state.message?.takeIf { it != R.string.backup_saved }?.let { BackupMessageCard(it) }
            TextButton(
                onClick = onDismiss,
                enabled = !openingScreen && !state.busy,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.settings_action_cancel))
            }
        }
    }
}
