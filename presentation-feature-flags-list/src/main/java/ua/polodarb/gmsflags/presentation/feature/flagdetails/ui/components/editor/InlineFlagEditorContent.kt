package ua.polodarb.gmsflags.presentation.feature.flagdetails.ui.components.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing
import ua.polodarb.gmsflags.presentation.feature.flagdetails.R
import ua.polodarb.gmsflags.presentation.feature.flagdetails.mvi.InlineFlagEditor

@Composable
internal fun InlineFlagEditorContent(
    editor: InlineFlagEditor,
    enabled: Boolean,
    onValueChanged: (String) -> Unit,
    onSave: () -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(GmsSpacing.Small),
    ) {
        FlagValueInput(
            type = editor.type,
            value = editor.value,
            onValueChanged = onValueChanged,
            onDone = onSave,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onReset, enabled = enabled) {
                Text(stringResource(R.string.flag_editor_default))
            }
            Spacer(Modifier.weight(1f))
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
            Button(onClick = onSave, enabled = enabled) {
                Text(stringResource(R.string.action_save))
            }
        }
    }
}
