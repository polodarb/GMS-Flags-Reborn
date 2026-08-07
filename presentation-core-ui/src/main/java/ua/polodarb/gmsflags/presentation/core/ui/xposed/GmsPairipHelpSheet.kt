package ua.polodarb.gmsflags.presentation.core.ui.xposed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch
import ua.polodarb.gmsflags.presentation.core.ui.R
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsDimensions
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GmsPairipHelpSheet(
    applicationName: String,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    val hideSheet: () -> Unit = {
        coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) onDismiss()
        }
        Unit
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        contentWindowInsets = { WindowInsets.safeDrawing.only(WindowInsetsSides.Top) },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(
                    start = GmsSpacing.ExtraLarge,
                    end = GmsSpacing.ExtraLarge,
                    bottom = GmsSpacing.Small,
                ),
        ) {
            Column(
                modifier = Modifier
                    .weight(weight = 1f, fill = false)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(GmsSpacing.ExtraLarge),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(GmsSpacing.Medium),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        modifier = Modifier.size(GmsDimensions.MinimumTouchTarget),
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Rounded.WarningAmber, contentDescription = null)
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(GmsSpacing.ExtraSmall)) {
                        Text(
                            text = stringResource(R.string.pairip_help_title),
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Text(
                            text = applicationName,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
                Text(
                    text = stringResource(R.string.pairip_help_explanation, applicationName),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyLarge,
                )
                Column(verticalArrangement = Arrangement.spacedBy(GmsSpacing.Medium)) {
                    Text(
                        text = stringResource(R.string.pairip_help_steps_title),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    PairipInstructionStep(1, stringResource(R.string.xposed_scope_step_open_lsposed))
                    PairipInstructionStep(2, stringResource(R.string.xposed_scope_step_open_modules))
                    PairipInstructionStep(3, stringResource(R.string.xposed_scope_step_open_gms_flags))
                    PairipInstructionStep(
                        4,
                        stringResource(R.string.pairip_help_step_disable_app, applicationName),
                    )
                }
            }
            Spacer(Modifier.height(GmsSpacing.ExtraLarge))
            Button(onClick = hideSheet, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.xposed_scope_close))
            }
        }
    }
}

@Composable
private fun PairipInstructionStep(number: Int, text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(GmsSpacing.Medium),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier.size(GmsDimensions.SelectionIndicatorSize),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(number.toString(), style = MaterialTheme.typography.labelMedium)
            }
        }
        Text(
            text = text,
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}
