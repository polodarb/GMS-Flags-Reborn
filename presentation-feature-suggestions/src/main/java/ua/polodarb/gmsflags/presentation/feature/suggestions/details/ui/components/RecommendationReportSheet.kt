package ua.polodarb.gmsflags.presentation.feature.suggestions.details.ui.components

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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import kotlinx.coroutines.launch
import ua.polodarb.gmsflags.domain.report.ProblemReportContext
import ua.polodarb.gmsflags.domain.report.ReportDiagnostics
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsDimensions
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing
import ua.polodarb.gmsflags.presentation.feature.suggestions.R
import ua.polodarb.gmsflags.presentation.feature.suggestions.details.ui.model.RecommendationReportPhase
import ua.polodarb.gmsflags.presentation.feature.suggestions.details.ui.model.RecommendationReportUiState
import ua.polodarb.gmsflags.presentation.feature.suggestions.details.ui.model.canSend

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RecommendationReportSheet(
    report: RecommendationReportUiState,
    onMessageChange: (String) -> Unit,
    onContactChange: (String) -> Unit,
    onToggleDetails: () -> Unit,
    onSend: () -> Unit,
    onDismiss: () -> Unit,
) {
    val formSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showSentSheet by remember {
        mutableStateOf(report.phase == RecommendationReportPhase.Sent)
    }

    LaunchedEffect(report.phase) {
        if (report.phase == RecommendationReportPhase.Sent && !showSentSheet) {
            formSheetState.hide()
            showSentSheet = true
        }
    }

    if (showSentSheet) {
        ReportSentSheet(
            onDismiss = onDismiss,
        )
    } else {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = formSheetState,
            contentWindowInsets = { WindowInsets.safeDrawing.only(WindowInsetsSides.Top) },
        ) {
            ReportFormContent(
                report = report,
                onMessageChange = onMessageChange,
                onContactChange = onContactChange,
                onToggleDetails = onToggleDetails,
                onSend = onSend,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReportSentSheet(
    onDismiss: () -> Unit,
) {
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
        ReportSentContent(onDone = { hideThen(onDismiss) })
    }
}

@Composable
private fun ReportFormContent(
    report: RecommendationReportUiState,
    onMessageChange: (String) -> Unit,
    onContactChange: (String) -> Unit,
    onToggleDetails: () -> Unit,
    onSend: () -> Unit,
) {
    val sending = report.phase == RecommendationReportPhase.Sending
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .navigationBarsPadding()
            .padding(
                start = GmsSpacing.ExtraLarge,
                end = GmsSpacing.ExtraLarge,
                bottom = GmsSpacing.Small,
            ),
        verticalArrangement = Arrangement.spacedBy(GmsSpacing.Large),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(GmsSpacing.Small)) {
            Text(
                text = stringResource(R.string.suggestions_report_title),
                style = MaterialTheme.typography.headlineSmall,
            )
            Text(
                text = stringResource(R.string.suggestions_report_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        OutlinedTextField(
            value = report.message,
            onValueChange = onMessageChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = !sending,
            minLines = 4,
            maxLines = 8,
            shape = MaterialTheme.shapes.large,
            label = { Text(stringResource(R.string.suggestions_report_message_label)) },
            placeholder = { Text(stringResource(R.string.suggestions_report_message_placeholder)) },
        )

        OutlinedTextField(
            value = report.contact,
            onValueChange = onContactChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = !sending,
            singleLine = true,
            shape = MaterialTheme.shapes.large,
            label = { Text(stringResource(R.string.suggestions_report_contact_label)) },
            placeholder = { Text(stringResource(R.string.suggestions_report_contact_placeholder)) },
            supportingText = { Text(stringResource(R.string.suggestions_report_contact_hint)) },
        )

        ReportDisclosure(
            report = report,
            onToggle = onToggleDetails,
        )

        if (report.phase == RecommendationReportPhase.Error) {
            ReportErrorRow()
        }

        Button(
            onClick = onSend,
            enabled = report.canSend(),
            modifier = Modifier
                .fillMaxWidth()
                .height(GmsDimensions.PrimaryActionHeight),
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
                    Text(stringResource(R.string.suggestions_report_sending))
                }
            } else {
                Text(stringResource(R.string.suggestions_report_send))
            }
        }
    }
}

@Composable
private fun ReportDisclosure(
    report: RecommendationReportUiState,
    onToggle: () -> Unit,
) {
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
                    text = stringResource(R.string.suggestions_report_disclosure_title),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    imageVector = if (report.detailsExpanded) {
                        Icons.Outlined.ExpandLess
                    } else {
                        Icons.Outlined.ExpandMore
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            AnimatedVisibility(visible = report.detailsExpanded) {
                Column(
                    modifier = Modifier.padding(top = GmsSpacing.Medium),
                    verticalArrangement = Arrangement.spacedBy(GmsSpacing.Small),
                ) {
                    ReportDetailRows(context = report.context, diagnostics = report.diagnostics)
                    Text(
                        text = stringResource(R.string.suggestions_report_logs_note),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = GmsSpacing.ExtraSmall),
                    )
                }
            }
        }
    }
}

@Composable
private fun ReportDetailRows(
    context: ProblemReportContext,
    diagnostics: ReportDiagnostics?,
) {
    val unknown = stringResource(R.string.suggestions_report_value_unknown)
    val none = stringResource(R.string.suggestions_report_value_none)

    if (diagnostics != null) {
        val device = diagnostics.device
        ReportDetailRow(
            label = stringResource(R.string.suggestions_report_field_device),
            value = listOf(device.manufacturer, device.model)
                .filter { it.isNotBlank() }
                .joinToString(" ")
                .ifBlank { unknown },
        )
        ReportDetailRow(
            label = stringResource(R.string.suggestions_report_field_android),
            value = stringResource(
                R.string.suggestions_report_value_android,
                device.androidRelease.ifBlank { unknown },
                device.sdkInt,
            ),
        )
        ReportDetailRow(
            label = stringResource(R.string.suggestions_report_field_app_version),
            value = stringResource(
                R.string.suggestions_report_value_app_version,
                diagnostics.appVersionName.ifBlank { unknown },
                diagnostics.appVersionCode,
            ),
        )
        ReportDetailRow(
            label = stringResource(R.string.suggestions_report_field_signature),
            value = diagnostics.appSignatureSha256?.shortenSignature() ?: unknown,
        )
    } else {
        ReportDetailRow(
            label = stringResource(R.string.suggestions_report_field_device),
            value = stringResource(R.string.suggestions_report_collecting),
        )
    }

    ReportDetailRow(
        label = stringResource(R.string.suggestions_report_field_target),
        value = context.targetPackage ?: none,
    )
    context.targetVersionName?.let { version ->
        ReportDetailRow(
            label = stringResource(R.string.suggestions_report_field_target_version),
            value = version,
        )
    }
    context.recommendationId?.let { recommendationId ->
        ReportDetailRow(
            label = stringResource(R.string.suggestions_report_field_recommendation),
            value = recommendationId.toString(),
        )
    }
    context.variantLabel?.let { label ->
        ReportDetailRow(
            label = stringResource(R.string.suggestions_report_field_variant),
            value = label,
        )
    }
    context.hookRecipeId?.let { recipeId ->
        ReportDetailRow(
            label = stringResource(R.string.suggestions_report_field_patch),
            value = recipeId.toString(),
        )
    }
    context.hookTrustStatus?.let { status ->
        ReportDetailRow(
            label = stringResource(R.string.suggestions_report_field_patch_status),
            value = status,
        )
    }
}

@Composable
private fun ReportDetailRow(label: String, value: String) {
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

@Composable
private fun ReportErrorRow() {
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
            text = stringResource(R.string.suggestions_report_error),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
        )
    }
}

@Composable
private fun ReportSentContent(onDone: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(
                start = GmsSpacing.ExtraLarge,
                end = GmsSpacing.ExtraLarge,
                bottom = GmsSpacing.Small,
            ),
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
                text = stringResource(R.string.suggestions_report_sent_title),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.suggestions_report_sent_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
        Button(
            onClick = onDone,
            modifier = Modifier
                .fillMaxWidth()
                .height(GmsDimensions.PrimaryActionHeight),
        ) {
            Text(stringResource(R.string.suggestions_report_sent_close))
        }
    }
}

/**
 * Shortens the full colon-separated cert hash to the first 8 octets so the disclosure stays readable;
 * the FULL hash is still what gets sent. E.g. `AB:CD:...` (32 groups) -> `AB:CD:EF:...:12 …`.
 */
private fun String.shortenSignature(): String {
    val groups = split(":")
    if (groups.size <= 8) return this
    return groups.take(8).joinToString(":") + " …"
}
