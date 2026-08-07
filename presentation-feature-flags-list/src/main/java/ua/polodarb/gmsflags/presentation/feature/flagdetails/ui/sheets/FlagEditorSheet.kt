package ua.polodarb.gmsflags.presentation.feature.flagdetails.ui.sheets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions
import ua.polodarb.gmsflags.domain.flags.FlagType
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsDimensions
import ua.polodarb.gmsflags.presentation.feature.flagdetails.editor.FlagEditorValidator
import kotlinx.coroutines.launch
import ua.polodarb.gmsflags.presentation.feature.flagdetails.R
import ua.polodarb.gmsflags.presentation.feature.flagdetails.mvi.FlagDetailsDialog
import ua.polodarb.gmsflags.presentation.feature.flagdetails.ui.components.editor.FlagValueInput
import ua.polodarb.gmsflags.presentation.feature.flagdetails.ui.model.labelRes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FlagEditorSheet(
    editor: FlagDetailsDialog.Editor,
    packageName: String,
    onDismiss: () -> Unit,
    onNameChanged: (String) -> Unit,
    onTypeChanged: (FlagType) -> Unit,
    onValueChanged: (String) -> Unit,
    onSave: () -> Unit,
    onReset: () -> Unit,
) {
    val isNew = editor.originalName == null
    val canSave = FlagEditorValidator.validate(editor).isSuccess
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    val save: () -> Unit = {
        if (canSave) {
            coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
                if (!sheetState.isVisible) onSave()
            }
        }
        Unit
    }
    val reset: () -> Unit = {
        coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) onReset()
        }
        Unit
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        contentWindowInsets = {
            WindowInsets.safeDrawing.only(WindowInsetsSides.Top)
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(
                    start = GmsSpacing.ExtraLarge,
                    end = GmsSpacing.ExtraLarge,
                    bottom = GmsSpacing.Small,
                ),
            verticalArrangement = Arrangement.spacedBy(GmsSpacing.Large),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(GmsSpacing.Medium),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    modifier = Modifier.size(GmsDimensions.MinimumTouchTarget),
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isNew) Icons.Outlined.AddCircleOutline
                            else Icons.Outlined.Edit,
                            contentDescription = null,
                        )
                    }
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(GmsSpacing.ExtraSmall),
                ) {
                    Text(
                        text = stringResource(
                            if (isNew) R.string.flag_editor_add_title
                            else R.string.flag_editor_change_title,
                        ),
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    Text(
                        text = packageName,
                        maxLines = 1,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(weight = 1f, fill = false)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(GmsSpacing.Large),
            ) {
                OutlinedTextField(
                    value = editor.name,
                    onValueChange = onNameChanged,
                    enabled = isNew,
                    label = { Text(stringResource(R.string.flag_editor_name)) },
                    singleLine = true,
                    shape = MaterialTheme.shapes.extraLarge,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth(),
                )

                if (isNew) {
                    FlagTypeSelector(
                        selectedType = editor.type,
                        onTypeSelected = onTypeChanged,
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(GmsSpacing.Small)) {
                        Text(
                            text = stringResource(R.string.flag_editor_type),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = stringResource(editor.type.labelRes),
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                }

                FlagValueEditor(
                    type = editor.type,
                    value = editor.value,
                    onValueChanged = onValueChanged,
                    onDone = save,
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(GmsSpacing.Small)) {
                Button(
                    onClick = save,
                    enabled = canSave,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(GmsDimensions.PrimaryActionHeight),
                ) {
                    Text(
                        stringResource(
                            if (isNew) R.string.flag_editor_add_action
                            else R.string.action_save,
                        )
                    )
                }
                if (!isNew) {
                    TextButton(
                        onClick = reset,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(R.string.flag_editor_default))
                    }
                }
            }
        }
    }
}

@Composable
private fun FlagTypeSelector(
    selectedType: FlagType,
    onTypeSelected: (FlagType) -> Unit,
) {
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(
            modifier = Modifier.padding(GmsSpacing.Large),
            verticalArrangement = Arrangement.spacedBy(GmsSpacing.Medium),
        ) {
            Text(
                text = stringResource(R.string.flag_editor_type),
                style = MaterialTheme.typography.titleSmall,
            )
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                FlagType.entries.forEachIndexed { index, type ->
                    SegmentedButton(
                        selected = selectedType == type,
                        onClick = { onTypeSelected(type) },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = FlagType.entries.size,
                        ),
                        label = { Text(stringResource(type.labelRes), maxLines = 1) },
                    )
                }
            }
        }
    }
}

@Composable
private fun FlagValueEditor(
    type: FlagType,
    value: String,
    onValueChanged: (String) -> Unit,
    onDone: () -> Unit,
) {
    if (type == FlagType.Boolean) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainer,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(GmsSpacing.Large),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(GmsSpacing.ExtraSmall),
                ) {
                    Text(
                        text = stringResource(R.string.flag_editor_enabled),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = stringResource(R.string.flag_editor_boolean_supporting),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = value == "1" || value.equals("true", ignoreCase = true),
                    onCheckedChange = { onValueChanged(if (it) "1" else "0") },
                )
            }
        }
    } else {
        FlagValueInput(
            type = type,
            value = value,
            onValueChanged = onValueChanged,
            onDone = onDone,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
