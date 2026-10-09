package ua.polodarb.gmsflags.presentation.feature.settings.backup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.FileUpload
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsDimensions
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.LocalGmsAdaptiveLayout
import ua.polodarb.gmsflags.presentation.core.ui.layout.GmsBackHeader
import ua.polodarb.gmsflags.presentation.core.ui.layout.GmsContentContainer
import ua.polodarb.gmsflags.presentation.feature.settings.R

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun BackupContent(
    state: BackupState,
    flow: BackupFlow,
    onBack: () -> Unit,
    onExport: () -> Unit,
    onImport: () -> Unit,
    onRestore: () -> Unit,
) {
    val layout = LocalGmsAdaptiveLayout.current
    val exporting = flow == BackupFlow.Export
    val backup = state.backup
    val saved = exporting && state.savedFileName != null
    var detailsExpanded by rememberSaveable { mutableStateOf(false) }
    val canRestore = backup?.packages?.any { it.androidPackageName in state.availableApps } == true
    val showRestore = !exporting && backup != null &&
        (state.result == null || state.result.failedPackages.isNotEmpty())

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceBright,
        topBar = {
            GmsBackHeader(
                title = stringResource(
                    if (exporting) R.string.backup_export else R.string.backup_import
                ),
                onBack = {
                    if (!state.busy) {
                        onBack()
                    }
                },
            )
        },
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.surfaceContainerLow) {
                Box(
                    modifier = Modifier.fillMaxWidth().navigationBarsPadding(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        modifier = Modifier.widthIn(max = layout.listMaxWidth)
                            .fillMaxWidth()
                            .padding(
                                horizontal = layout.contentPadding,
                                vertical = GmsSpacing.Medium,
                            ),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Button(
                            onClick = if (saved) {
                                onBack
                            } else if (exporting) {
                                onExport
                            } else if (showRestore) {
                                onRestore
                            } else {
                                onImport
                            },
                            enabled = !state.busy && (!showRestore || canRestore),
                            shapes = ButtonDefaults.shapes(),
                            modifier = Modifier.fillMaxWidth()
                                .heightIn(min = GmsDimensions.PrimaryActionHeight),
                        ) {
                            Icon(
                                if (saved) {
                                    Icons.Rounded.Check
                                } else if (exporting) {
                                    Icons.Rounded.FileUpload
                                } else if (showRestore) {
                                    Icons.Rounded.Restore
                                } else {
                                    Icons.Rounded.FileDownload
                                },
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(Modifier.width(GmsSpacing.Small))
                            Text(
                                stringResource(
                                    when {
                                        saved -> R.string.backup_done
                                        exporting -> R.string.backup_export
                                        showRestore && state.result != null -> R.string.backup_retry
                                        showRestore -> R.string.backup_restore
                                        backup != null -> R.string.backup_choose_another
                                        else -> R.string.backup_choose_file
                                    }
                                )
                            )
                        }
                        if (showRestore && !exporting) {
                            TextButton(onClick = onImport, enabled = !state.busy) {
                                Text(stringResource(R.string.backup_choose_another))
                            }
                        }
                    }
                }
            }
        },
    ) { padding ->
        GmsContentContainer(Modifier.fillMaxSize().padding(padding)) {
            LazyColumn(
                modifier = Modifier.align(Alignment.TopCenter)
                    .widthIn(max = layout.listMaxWidth)
                    .fillMaxSize(),
                contentPadding = PaddingValues(
                    horizontal = layout.contentPadding,
                    vertical = GmsSpacing.ExtraLarge,
                ),
                verticalArrangement = Arrangement.spacedBy(GmsSpacing.Large),
            ) {
                item(key = "summary") {
                    BackupSummaryCard(
                        backup = backup,
                        flow = flow,
                        savedFileName = state.savedFileName,
                    )
                }
                if (state.busy) {
                    item(key = "progress") { BackupProgressCard() }
                }
                state.message
                    ?.takeIf { !saved }
                    ?.let { message -> item(key = "message") { BackupMessageCard(message) } }
                state.result?.let { result -> item(key = "result") { BackupResultCard(result) } }
                if (saved) {
                    item(key = "details") {
                        TextButton(
                            onClick = { detailsExpanded = !detailsExpanded },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                stringResource(
                                    if (detailsExpanded) {
                                        R.string.backup_hide_details
                                    } else {
                                        R.string.backup_show_details
                                    }
                                )
                            )
                        }
                    }
                }
                if (backup != null && (!exporting || detailsExpanded)) {
                    if (!exporting && state.result == null) {
                        item(key = "merge") { BackupMergeCard() }
                    }
                    item(key = "packages-heading") {
                        Text(
                            text = stringResource(R.string.backup_packages_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = GmsSpacing.Small).semantics {
                                heading()
                            },
                        )
                    }
                    items(backup.packages) { pkg ->
                        BackupPackageCard(
                            pkg = pkg,
                            skipped = !exporting &&
                                (pkg.androidPackageName !in state.availableApps ||
                                    state.result?.skippedPackages?.contains(pkg) == true),
                            failed = state.result?.failedPackages?.contains(pkg) == true,
                            restored = state.result != null,
                            exported = saved,
                        )
                    }
                }
            }
        }
    }
}
