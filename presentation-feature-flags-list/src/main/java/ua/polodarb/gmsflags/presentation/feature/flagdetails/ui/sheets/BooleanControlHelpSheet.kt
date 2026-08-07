package ua.polodarb.gmsflags.presentation.feature.flagdetails.ui.sheets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Circle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindowProvider
import kotlinx.coroutines.launch
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsDimensions
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing
import ua.polodarb.gmsflags.presentation.feature.flagdetails.R

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun BooleanControlHelpSheet(onDismiss: () -> Unit) {
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
    ) {
        val view = LocalView.current
        SideEffect {
            (view.parent as? DialogWindowProvider)?.window?.isNavigationBarContrastEnforced = false
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(
                    start = GmsSpacing.ExtraLarge,
                    end = GmsSpacing.ExtraLarge,
                    bottom = GmsSpacing.Large,
                ),
            verticalArrangement = Arrangement.spacedBy(GmsSpacing.ExtraLarge),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(GmsSpacing.Medium),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val shape = MaterialShapes.Cookie9Sided.toShape()
                Box(
                    modifier = Modifier
                        .size(GmsDimensions.MinimumTouchTarget)
                        .clip(shape)
                        .background(MaterialTheme.colorScheme.tertiaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(GmsSpacing.ExtraSmall)) {
                    Text(
                        text = stringResource(R.string.flag_control_help_title),
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Text(
                        text = stringResource(R.string.flag_control_help_description),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
            Text(
                text = stringResource(R.string.flag_control_help_default_caveat),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
            Column(verticalArrangement = Arrangement.spacedBy(GmsSpacing.Small)) {
                FlagStatePreviewRow(
                    on = false,
                    overridden = false,
                    label = stringResource(R.string.flag_control_help_state_default_off),
                )
                FlagStatePreviewRow(
                    on = true,
                    overridden = false,
                    label = stringResource(R.string.flag_control_help_state_default_on),
                )
                FlagStatePreviewRow(
                    on = false,
                    overridden = true,
                    label = stringResource(R.string.flag_control_help_state_overridden_off),
                )
                FlagStatePreviewRow(
                    on = true,
                    overridden = true,
                    label = stringResource(R.string.flag_control_help_state_overridden_on),
                )
            }
            ResetNoteText()
            Button(
                onClick = hideSheet,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(GmsDimensions.PrimaryActionHeight),
            ) {
                Text(stringResource(R.string.action_close))
            }
        }
    }
}

@Composable
private fun ResetNoteText() {
    val inlineIconSize = MaterialTheme.typography.bodySmall.fontSize
    val resetIconId = "reset_icon"
    val deleteIconId = "delete_icon"
    val text = buildAnnotatedString {
        append(stringResource(R.string.flag_control_help_reset_note_prefix))
        appendInlineContent(resetIconId)
        append(" ")
        append(stringResource(R.string.flag_control_help_reset_note_middle))
        appendInlineContent(deleteIconId)
        append(" ")
        append(stringResource(R.string.flag_control_help_reset_note_suffix))
    }
    val placeholder = Placeholder(
        width = inlineIconSize,
        height = inlineIconSize,
        placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter,
    )
    val inlineContent = mapOf(
        resetIconId to InlineTextContent(placeholder = placeholder) {
            Icon(
                imageVector = Icons.Rounded.Refresh,
                contentDescription = stringResource(R.string.flag_reset_default_description),
                modifier = Modifier.fillMaxSize(),
            )
        },
        deleteIconId to InlineTextContent(placeholder = placeholder) {
            Icon(
                imageVector = Icons.Rounded.Delete,
                contentDescription = stringResource(R.string.flag_delete_description),
                modifier = Modifier.fillMaxSize(),
            )
        },
    )
    Text(
        text = text,
        inlineContent = inlineContent,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.bodySmall,
    )
}

@Composable
private fun FlagStatePreviewRow(
    on: Boolean,
    overridden: Boolean,
    label: String,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = if (overridden) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh
        },
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = GmsSpacing.Large,
                vertical = GmsSpacing.Medium,
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(GmsSpacing.Medium),
        ) {
            Switch(
                checked = on,
                onCheckedChange = null,
                thumbContent = if (overridden) {
                    {
                        Icon(
                            imageVector = Icons.Rounded.Circle,
                            contentDescription = null,
                            modifier = Modifier.size(10.dp),
                        )
                    }
                } else {
                    null
                },
            )
            Text(
                text = label,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
