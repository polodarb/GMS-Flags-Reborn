package ua.polodarb.gmsflags.presentation.feature.community.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ua.polodarb.gmsflags.domain.community.CommunityFlagItem
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing
import ua.polodarb.gmsflags.presentation.feature.community.R

@Composable
fun CommunitySubmitDialog(
    initialPackageName: String,
    initialFlags: List<CommunityFlagItem>,
    onDismiss: () -> Unit,
    onAddFlag: (String, String, String) -> Unit,
    onRemoveFlag: (Int) -> Unit,
    onSubmit: (String, String, String) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var packageName by remember { mutableStateOf(initialPackageName) }

    var newFlagName by remember { mutableStateOf("") }
    var newFlagValue by remember { mutableStateOf("") }
    var showAddFlagInput by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.community_submit_title)) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.community_submit_field_title)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(GmsSpacing.Small))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(R.string.community_submit_field_description)) },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(GmsSpacing.Small))
                OutlinedTextField(
                    value = packageName,
                    onValueChange = { packageName = it },
                    label = { Text(stringResource(R.string.community_submit_field_package_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(GmsSpacing.Medium))
                Text(
                    text = pluralStringResource(
                        R.plurals.community_submit_flags_count,
                        initialFlags.size,
                        initialFlags.size,
                    ),
                    style = MaterialTheme.typography.titleSmall,
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(GmsSpacing.ExtraSmall),
                ) {
                    itemsIndexed(initialFlags) { index, flag ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(GmsSpacing.Small),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = flag.flagName, style = MaterialTheme.typography.bodyMedium)
                                    Text(text = flag.value, style = MaterialTheme.typography.bodySmall)
                                }
                                IconButton(onClick = { onRemoveFlag(index) }) {
                                    Icon(
                                        imageVector = Icons.Rounded.Delete,
                                        contentDescription = stringResource(R.string.community_submit_remove_flag),
                                    )
                                }
                            }
                        }
                    }
                }

                if (showAddFlagInput) {
                    Spacer(modifier = Modifier.height(GmsSpacing.Small))
                    OutlinedTextField(
                        value = newFlagName,
                        onValueChange = { newFlagName = it },
                        label = { Text(stringResource(R.string.community_submit_flag_name)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(GmsSpacing.ExtraSmall))
                    OutlinedTextField(
                        value = newFlagValue,
                        onValueChange = { newFlagValue = it },
                        label = { Text(stringResource(R.string.community_submit_flag_value)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(GmsSpacing.ExtraSmall))
                    Row {
                        TextButton(onClick = { showAddFlagInput = false }) {
                            Text(stringResource(R.string.community_submit_cancel))
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Button(
                            onClick = {
                                if (newFlagName.isNotBlank()) {
                                    onAddFlag(newFlagName, "string", newFlagValue)
                                    newFlagName = ""
                                    newFlagValue = ""
                                    showAddFlagInput = false
                                }
                            },
                        ) {
                            Text(stringResource(R.string.community_submit_confirm_flag))
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(GmsSpacing.Small)) {
                OutlinedButton(
                    onClick = { showAddFlagInput = true },
                ) {
                    Icon(imageVector = Icons.Rounded.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(GmsSpacing.ExtraSmall))
                    Text(stringResource(R.string.community_submit_add_flag))
                }
                Button(
                    onClick = { onSubmit(title, description, packageName) },
                    enabled = title.isNotBlank() && packageName.isNotBlank() && initialFlags.isNotEmpty(),
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Rounded.Send, contentDescription = null)
                    Spacer(modifier = Modifier.width(GmsSpacing.ExtraSmall))
                    Text(stringResource(R.string.community_submit_send))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.community_submit_cancel))
            }
        },
    )
}
