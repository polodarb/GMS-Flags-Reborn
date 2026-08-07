package ua.polodarb.gmsflags.presentation.feature.hookstatus.details

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ua.polodarb.gmsflags.presentation.feature.hookstatus.R

@Composable
internal fun ConfirmDeleteApplicationOverridesDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.hook_status_delete_overrides_confirm_title)) },
        text = { Text(stringResource(R.string.hook_status_delete_overrides_confirm_message)) },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        },
        confirmButton = {
            Button(onClick = onConfirm) { Text(stringResource(R.string.action_delete)) }
        },
    )
}
