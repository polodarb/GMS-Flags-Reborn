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

@Composable
internal fun ReportFlagsDialog(
    description: String,
    onDescriptionChanged: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.report_flags_title)) },
        text = {
            OutlinedTextField(
                value = description,
                onValueChange = onDescriptionChanged,
                label = { Text(stringResource(R.string.report_flags_description)) },
                minLines = 3,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        },
        confirmButton = {
            Button(onClick = onConfirm) { Text(stringResource(R.string.action_continue)) }
        },
    )
}
