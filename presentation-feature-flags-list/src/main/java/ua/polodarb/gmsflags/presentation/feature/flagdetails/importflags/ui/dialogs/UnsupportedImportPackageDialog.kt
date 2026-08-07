package ua.polodarb.gmsflags.presentation.feature.flagdetails.importflags.ui.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ua.polodarb.gmsflags.presentation.feature.flagdetails.R

@Composable
internal fun UnsupportedImportPackageDialog(
    packageName: String,
    onChooseAnother: () -> Unit,
    onBack: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onBack,
        title = { Text(stringResource(R.string.import_flags_unsupported_package_title)) },
        text = {
            Text(
                stringResource(
                    R.string.import_flags_unsupported_package_message,
                    packageName,
                )
            )
        },
        confirmButton = {
            TextButton(onClick = onChooseAnother) {
                Text(stringResource(R.string.import_flags_choose_another))
            }
        },
        dismissButton = {
            TextButton(onClick = onBack) {
                Text(stringResource(R.string.action_cancel))
            }
        },
    )
}
