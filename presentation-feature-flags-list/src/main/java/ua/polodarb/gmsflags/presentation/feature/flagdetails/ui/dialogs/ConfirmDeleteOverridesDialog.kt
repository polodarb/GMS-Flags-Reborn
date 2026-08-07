package ua.polodarb.gmsflags.presentation.feature.flagdetails.ui.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.res.stringResource
import ua.polodarb.gmsflags.presentation.feature.flagdetails.R
import androidx.compose.runtime.Composable

@Composable
internal fun ConfirmDeleteOverridesDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.delete_overrides_title)) },
        text = { Text(stringResource(R.string.delete_overrides_message)) },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        },
        confirmButton = {
            Button(onClick = onConfirm) { Text(stringResource(R.string.action_delete)) }
        },
    )
}
