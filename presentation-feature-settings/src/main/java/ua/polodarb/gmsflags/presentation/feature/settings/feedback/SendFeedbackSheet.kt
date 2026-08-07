package ua.polodarb.gmsflags.presentation.feature.settings.feedback

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import ua.polodarb.gmsflags.domain.report.ReportCategory
import ua.polodarb.gmsflags.domain.report.ReportDiagnostics
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsDimensions
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing
import ua.polodarb.gmsflags.presentation.feature.settings.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendFeedbackSheet(onDismiss: () -> Unit) {
    val viewModel: SendFeedbackViewModel = koinViewModel()
    val state by viewModel.viewState.collectAsStateWithLifecycle()

    val formSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showSentSheet by remember { mutableStateOf(state.phase == SendFeedbackPhase.Sent) }

    val dismissAndReset: () -> Unit = {
        viewModel.setEvent(SendFeedbackEvent.Dismissed)
        onDismiss()
    }

    LaunchedEffect(state.phase) {
        if (state.phase == SendFeedbackPhase.Sent && !showSentSheet) {
            formSheetState.hide()
            showSentSheet = true
        }
    }

    if (showSentSheet) {
        FeedbackSentSheet(onDismiss = dismissAndReset)
    } else {
        ModalBottomSheet(
            onDismissRequest = dismissAndReset,
            sheetState = formSheetState,
            contentWindowInsets = { WindowInsets.safeDrawing.only(WindowInsetsSides.Top) },
        ) {
            FeedbackFormContent(
                state = state,
                onCategorySelected = { viewModel.setEvent(SendFeedbackEvent.CategorySelected(it)) },
                onMessageChange = { viewModel.setEvent(SendFeedbackEvent.MessageChanged(it)) },
                onContactChange = { viewModel.setEvent(SendFeedbackEvent.ContactChanged(it)) },
                onToggleDetails = { viewModel.setEvent(SendFeedbackEvent.DetailsToggled) },
                onSend = { viewModel.setEvent(SendFeedbackEvent.SendClicked) },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FeedbackSentSheet(onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val hideThen: (() -> Unit) -> Unit = { action ->
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) action()
        }
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
                .padding(start = GmsSpacing.ExtraLarge, end = GmsSpacing.ExtraLarge, bottom = GmsSpacing.Small),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(GmsSpacing.Large),
        ) {
            Surface(
                modifier = Modifier.size(GmsDimensions.OpeningIconSize),
                shape = RoundedCornerShape(percent = 50),
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(GmsDimensions.SnackbarIconSize * 1.8f),
                    )
                }
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(GmsSpacing.Small),
            ) {
                Text(
                    text = stringResource(R.string.settings_feedback_sent_title),
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = stringResource(R.string.settings_feedback_sent_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
            Button(
                onClick = { hideThen(onDismiss) },
                modifier = Modifier.fillMaxWidth().height(GmsDimensions.PrimaryActionHeight),
            ) {
                Text(stringResource(R.string.settings_feedback_sent_close))
            }
        }
    }
}

@Composable
private fun FeedbackFormContent(
    state: SendFeedbackState,
    onCategorySelected: (ReportCategory) -> Unit,
    onMessageChange: (String) -> Unit,
    onContactChange: (String) -> Unit,
    onToggleDetails: () -> Unit,
    onSend: () -> Unit,
) {
    val sending = state.phase == SendFeedbackPhase.Sending
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .navigationBarsPadding()
            .padding(start = GmsSpacing.ExtraLarge, end = GmsSpacing.ExtraLarge, bottom = GmsSpacing.Small),
        verticalArrangement = Arrangement.spacedBy(GmsSpacing.Large),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(GmsSpacing.Small)) {
            Text(
                text = stringResource(R.string.settings_feedback_title),
                style = MaterialTheme.typography.headlineSmall,
            )
            Text(
                text = stringResource(R.string.settings_feedback_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        val categories = ReportCategory.entries
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            categories.forEachIndexed { index, category ->
                SegmentedButton(
                    selected = state.category == category,
                    onClick = { onCategorySelected(category) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = categories.size),
                    label = { Text(stringResource(category.labelRes()), maxLines = 1) },
                )
            }
        }

        OutlinedTextField(
            value = state.message,
            onValueChange = onMessageChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = !sending,
            minLines = 4,
            maxLines = 8,
            shape = MaterialTheme.shapes.large,
            label = { Text(stringResource(R.string.settings_feedback_message_label)) },
            placeholder = { Text(stringResource(R.string.settings_feedback_message_placeholder)) },
        )

        OutlinedTextField(
            value = state.contact,
            onValueChange = onContactChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = !sending,
            singleLine = true,
            shape = MaterialTheme.shapes.large,
            label = { Text(stringResource(R.string.settings_feedback_contact_label)) },
            placeholder = { Text(stringResource(R.string.settings_feedback_contact_placeholder)) },
            supportingText = { Text(stringResource(R.string.settings_feedback_contact_hint)) },
        )

        FeedbackDisclosure(state = state, onToggle = onToggleDetails)

        if (state.phase == SendFeedbackPhase.Error) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(GmsSpacing.Small),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Outlined.ErrorOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(GmsDimensions.SnackbarIconSize),
                )
                Text(
                    text = stringResource(R.string.settings_feedback_error),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }

        Button(
            onClick = onSend,
            enabled = state.canSend(),
            modifier = Modifier.fillMaxWidth().height(GmsDimensions.PrimaryActionHeight),
        ) {
            if (sending) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(GmsSpacing.Small),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(GmsDimensions.SnackbarIconSize),
                        strokeWidth = 2.dp,
                        color = LocalContentColor.current,
                    )
                    Text(stringResource(R.string.settings_feedback_sending))
                }
            } else {
                Text(stringResource(R.string.settings_feedback_send))
            }
        }
    }
}

private fun ReportCategory.labelRes(): Int = when (this) {
    ReportCategory.Bug -> R.string.settings_feedback_category_bug
    ReportCategory.Suggestion -> R.string.settings_feedback_category_suggestion
    ReportCategory.Other -> R.string.settings_feedback_category_other
}

@Composable
private fun FeedbackDisclosure(state: SendFeedbackState, onToggle: () -> Unit) {
    Surface(
        onClick = onToggle,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(modifier = Modifier.padding(GmsSpacing.Large)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(GmsSpacing.Medium),
            ) {
                Text(
                    text = stringResource(R.string.settings_feedback_disclosure_title),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    imageVector = if (state.detailsExpanded) {
                        Icons.Outlined.ExpandLess
                    } else {
                        Icons.Outlined.ExpandMore
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            AnimatedVisibility(visible = state.detailsExpanded) {
                Column(
                    modifier = Modifier.padding(top = GmsSpacing.Medium),
                    verticalArrangement = Arrangement.spacedBy(GmsSpacing.Small),
                ) {
                    FeedbackDetailRows(diagnostics = state.diagnostics)
                }
            }
        }
    }
}

@Composable
private fun FeedbackDetailRows(diagnostics: ReportDiagnostics?) {
    val unknown = stringResource(R.string.settings_feedback_value_unknown)
    if (diagnostics == null) {
        FeedbackDetailRow(
            label = stringResource(R.string.settings_feedback_field_device),
            value = stringResource(R.string.settings_feedback_collecting),
        )
        return
    }
    val device = diagnostics.device
    FeedbackDetailRow(
        label = stringResource(R.string.settings_feedback_field_device),
        value = listOf(device.manufacturer, device.model).filter { it.isNotBlank() }.joinToString(" ").ifBlank { unknown },
    )
    FeedbackDetailRow(
        label = stringResource(R.string.settings_feedback_field_android),
        value = stringResource(R.string.settings_feedback_value_android, device.androidRelease.ifBlank { unknown }, device.sdkInt),
    )
    FeedbackDetailRow(
        label = stringResource(R.string.settings_feedback_field_app_version),
        value = stringResource(R.string.settings_feedback_value_app_version, diagnostics.appVersionName.ifBlank { unknown }, diagnostics.appVersionCode),
    )
    FeedbackDetailRow(
        label = stringResource(R.string.settings_feedback_field_signature),
        value = diagnostics.appSignatureSha256?.shortenSignature() ?: unknown,
    )
}

private fun String.shortenSignature(): String {
    val groups = split(":")
    if (groups.size <= 8) return this
    return groups.take(8).joinToString(":") + " …"
}

@Composable
private fun FeedbackDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(GmsSpacing.Large),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.4f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(0.6f),
        )
    }
}
