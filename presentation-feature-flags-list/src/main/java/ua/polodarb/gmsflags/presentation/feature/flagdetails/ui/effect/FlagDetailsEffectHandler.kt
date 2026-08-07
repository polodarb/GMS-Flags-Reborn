package ua.polodarb.gmsflags.presentation.feature.flagdetails.ui.effect

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.core.net.toUri
import androidx.core.content.FileProvider
import java.io.File
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import ua.polodarb.gmsflags.presentation.feature.flagdetails.mvi.FlagDetailsEffect
import ua.polodarb.gmsflags.presentation.feature.flagdetails.navigation.FlagDetailsScreenAction
import ua.polodarb.gmsflags.presentation.feature.flagdetails.R
import ua.polodarb.gmsflags.presentation.core.message.UiMessageType
import ua.polodarb.gmsflags.presentation.core.ui.snackbar.LocalGmsSnackbarHostState
import ua.polodarb.gmsflags.presentation.core.ui.snackbar.GmsSnackbarDuration
import ua.polodarb.gmsflags.presentation.core.ui.state.localizedDescription

@Composable
internal fun FlagDetailsEffectHandler(
    effects: Flow<FlagDetailsEffect>,
    onAction: (FlagDetailsScreenAction) -> Unit,
    onOpenImportFilePicker: () -> Unit,
) {
    val context = LocalContext.current
    val resources = LocalResources.current
    val clipboard = LocalClipboard.current
    val snackbarHostState = LocalGmsSnackbarHostState.current

    LaunchedEffect(effects, context, resources, clipboard, snackbarHostState) {
        effects.collect { effect ->
            try {
                when (effect) {
                    FlagDetailsEffect.NavigateBack -> onAction(FlagDetailsScreenAction.Back)
                    is FlagDetailsEffect.CopyPackageName -> {
                        clipboard.setClipEntry(
                            ClipEntry(
                                ClipData.newPlainText(
                                    resources.getString(R.string.clipboard_package_name),
                                    effect.packageName,
                                )
                            )
                        )
                        snackbarHostState.showSnackbar(
                            message = resources.getString(R.string.message_package_copied),
                            type = UiMessageType.Success,
                        )
                    }
                    is FlagDetailsEffect.CopyFlagNames -> {
                        clipboard.setClipEntry(
                            ClipEntry(
                                ClipData.newPlainText(
                                    resources.getString(R.string.clipboard_flag_names),
                                    effect.names.joinToString("\n"),
                                )
                            )
                        )
                        snackbarHostState.showSnackbar(
                            message = resources.getQuantityString(
                                R.plurals.message_flag_names_copied,
                                effect.names.size,
                                effect.names.size,
                            ),
                            type = UiMessageType.Success,
                        )
                    }
                    is FlagDetailsEffect.OpenAddMultiple -> onAction(
                        FlagDetailsScreenAction.AddMultiple(
                            effect.androidPackageName,
                            effect.phenotypePackageName,
                        )
                    )
                    is FlagDetailsEffect.OpenRecommendation -> onAction(
                        FlagDetailsScreenAction.OpenRecommendation(effect.id)
                    )
                    FlagDetailsEffect.OpenImportFilePicker -> onOpenImportFilePicker()
                    is FlagDetailsEffect.LaunchApplication -> {
                        if (!context.launchApplication(effect.androidPackageName)) {
                            snackbarHostState.showSnackbar(
                                message = resources.getString(R.string.message_unable_launch_app),
                                type = UiMessageType.Error,
                            )
                        }
                    }
                    is FlagDetailsEffect.OpenAppSettings -> context.openAppSettings(
                        effect.androidPackageName
                    )
                    is FlagDetailsEffect.ShareFlags -> context.shareFlags(effect)
                    is FlagDetailsEffect.ReportFlags -> context.reportFlags(effect)
                    is FlagDetailsEffect.ShowMessage -> snackbarHostState.showSnackbar(
                        message = resources.getString(effect.messageRes),
                        type = effect.type,
                        duration = if (effect.longDuration) {
                            GmsSnackbarDuration.Long
                        } else {
                            GmsSnackbarDuration.Short
                        },
                    )
                    is FlagDetailsEffect.ShowError -> snackbarHostState.showSnackbar(
                        message = effect.error.localizedDescription(resources),
                        type = UiMessageType.Error,
                    )
                }
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                snackbarHostState.showSnackbar(
                    message = resources.getString(R.string.message_unable_complete_action),
                    type = UiMessageType.Error,
                )
            }
        }
    }
}

private fun Context.launchApplication(packageName: String): Boolean {
    val launchIntent = packageManager.getLaunchIntentForPackage(packageName) ?: return false
    startActivity(launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    return true
}

private fun Context.openAppSettings(packageName: String) {
    startActivity(
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }
    )
}

private suspend fun Context.shareFlags(effect: FlagDetailsEffect.ShareFlags) {
    val file = withContext(Dispatchers.IO) {
        val exportDirectory = File(cacheDir, "exports").apply { mkdirs() }
        File(
            exportDirectory,
            "${effect.fileName.toSafeExportName(getString(R.string.default_export_file_name))}" +
                getString(R.string.export_file_suffix),
        ).apply {
            writeText(effect.content)
        }
    }
    val uri = FileProvider.getUriForFile(
        this,
        "$packageName.fileprovider",
        file,
    )
    startActivity(
        Intent.createChooser(
            Intent(Intent.ACTION_SEND).apply {
                type = "application/gmsflags"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            },
            getString(R.string.share_flags_chooser),
        )
    )
}

private fun Context.reportFlags(effect: FlagDetailsEffect.ReportFlags) {
    startActivity(
        Intent.createChooser(
            Intent(Intent.ACTION_SENDTO).apply {
                data = "mailto:".toUri()
                putExtra(Intent.EXTRA_EMAIL, arrayOf("gmsflags@gmail.com"))
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.report_email_subject))
                putExtra(Intent.EXTRA_TEXT, effect.emailBody(this@reportFlags))
            },
            getString(R.string.report_flags_chooser),
        )
    )
}

internal fun String.toSafeExportName(defaultName: String): String =
    removeSuffix(".gmsflags")
        .replace(Regex("[^A-Za-z0-9._-]"), "_")
        .ifBlank { defaultName }

private fun FlagDetailsEffect.ReportFlags.emailBody(context: Context): String = context.getString(
    R.string.report_email_body,
    packageName,
    description,
    flags,
)
