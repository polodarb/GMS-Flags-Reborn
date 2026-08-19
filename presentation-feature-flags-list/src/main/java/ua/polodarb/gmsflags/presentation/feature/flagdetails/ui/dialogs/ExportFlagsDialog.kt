package ua.polodarb.gmsflags.presentation.feature.flagdetails.ui.dialogs

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ua.polodarb.gmsflags.presentation.feature.flagdetails.R

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.OutlinedButton
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing

@Composable
internal fun ExportFlagsDialog(
    fileName: String,
    onFileNameChanged: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    onShareToCommunity: (() -> Unit)? = null,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.export_flags_title)) },
        text = {
            OutlinedTextField(
                value = fileName,
                onValueChange = onFileNameChanged,
                label = { Text(stringResource(R.string.export_file_name)) },
                suffix = { Text(stringResource(R.string.export_file_suffix)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(GmsSpacing.Small)) {
                if (onShareToCommunity != null) {
                    OutlinedButton(onClick = onShareToCommunity) {
                        Text(stringResource(R.string.selection_share_community))
                    }
                }

                Button(onClick = onConfirm) { Text(stringResource(R.string.action_share)) }
            }
        },
    )
}

