package ua.polodarb.gmsflags.presentation.feature.flagdetails.ui.overlays

import androidx.compose.runtime.Composable
import ua.polodarb.gmsflags.domain.flags.FlagType
import ua.polodarb.gmsflags.presentation.feature.flagdetails.mvi.FlagDetailsDialog
import ua.polodarb.gmsflags.presentation.feature.flagdetails.ui.dialogs.ConfirmDeleteOverridesDialog
import ua.polodarb.gmsflags.presentation.feature.flagdetails.ui.dialogs.ExportFlagsDialog
import ua.polodarb.gmsflags.presentation.feature.flagdetails.ui.dialogs.OperationDialog
import ua.polodarb.gmsflags.presentation.feature.flagdetails.ui.dialogs.ReportFlagsDialog
import ua.polodarb.gmsflags.presentation.feature.flagdetails.ui.sheets.FlagEditorSheet

import ua.polodarb.gmsflags.presentation.feature.community.ui.components.CommunitySubmitDialog

@Composable
internal fun FlagDetailsOverlays(
    dialog: FlagDetailsDialog?,
    packageName: String,
    operationInProgress: Boolean,
    bulkOperationInProgress: Boolean,
    onDismiss: () -> Unit,
    onEditorNameChanged: (String) -> Unit,
    onEditorTypeChanged: (FlagType) -> Unit,
    onEditorValueChanged: (String) -> Unit,
    onEditorSave: () -> Unit,
    onEditorReset: () -> Unit,
    onDeleteAll: () -> Unit,
    onExportNameChanged: (String) -> Unit,
    onExport: () -> Unit,
    onShareToCommunity: () -> Unit = {},
    onAddCommunityFlag: (String, String, String) -> Unit = { _, _, _ -> },
    onRemoveCommunityFlag: (Int) -> Unit = {},
    onSubmitCommunityPackage: (String, String, String) -> Unit = { _, _, _ -> },
    onReportDescriptionChanged: (String) -> Unit,
    onReport: () -> Unit,
) {
    when (dialog) {
        is FlagDetailsDialog.Editor -> FlagEditorSheet(
            editor = dialog,
            packageName = packageName,
            onDismiss = onDismiss,
            onNameChanged = onEditorNameChanged,
            onTypeChanged = onEditorTypeChanged,
            onValueChanged = onEditorValueChanged,
            onSave = onEditorSave,
            onReset = onEditorReset,
        )
        FlagDetailsDialog.DeleteAllOverrides -> ConfirmDeleteOverridesDialog(
            onDismiss = onDismiss,
            onConfirm = onDeleteAll,
        )
        is FlagDetailsDialog.Export -> ExportFlagsDialog(
            fileName = dialog.fileName,
            onFileNameChanged = onExportNameChanged,
            onDismiss = onDismiss,
            onConfirm = onExport,
            onShareToCommunity = onShareToCommunity,
        )
        is FlagDetailsDialog.ShareToCommunity -> CommunitySubmitDialog(
            initialPackageName = dialog.packageName,
            initialFlags = dialog.flags,
            onDismiss = onDismiss,
            onAddFlag = onAddCommunityFlag,
            onRemoveFlag = onRemoveCommunityFlag,
            onSubmit = onSubmitCommunityPackage,
        )
        is FlagDetailsDialog.Report -> ReportFlagsDialog(
            description = dialog.description,
            onDescriptionChanged = onReportDescriptionChanged,
            onDismiss = onDismiss,
            onConfirm = onReport,
        )
        null -> Unit
    }


    if (operationInProgress) OperationDialog(showBulkNotice = bulkOperationInProgress)
}
