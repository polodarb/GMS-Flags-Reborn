package ua.polodarb.gmsflags.presentation.feature.settings.backup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import ua.polodarb.gmsflags.domain.backup.BackupPackage
import ua.polodarb.gmsflags.domain.backup.BackupRestoreResult
import ua.polodarb.gmsflags.domain.backup.FlagsBackup
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsDimensions
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing
import ua.polodarb.gmsflags.presentation.feature.settings.R

@Composable
internal fun BackupSummaryCard(
    backup: FlagsBackup?,
    flow: BackupFlow,
    savedFileName: String? = null,
) {
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(GmsSpacing.ExtraLarge),
            verticalArrangement = Arrangement.spacedBy(GmsSpacing.Large),
        ) {
            BackupIcon(
                icon = if (flow == BackupFlow.Export) {
                    Icons.Rounded.CloudDone
                } else {
                    Icons.Rounded.Restore
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            )
            Text(
                text = stringResource(
                    when {
                        savedFileName != null -> R.string.backup_saved_title
                        backup != null -> R.string.backup_preview_title
                        flow == BackupFlow.Export -> R.string.backup_hero_title
                        else -> R.string.backup_import_title
                    }
                ),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.semantics { heading() },
            )
            Text(
                text = stringResource(
                    when {
                        savedFileName != null -> R.string.backup_saved_description
                        backup != null -> R.string.backup_preview_description
                        flow == BackupFlow.Export -> R.string.backup_export_scope
                        else -> R.string.backup_import_description
                    }
                ),
                style = MaterialTheme.typography.bodyLarge,
            )
            if (savedFileName != null) {
                Text(savedFileName, style = MaterialTheme.typography.bodyMedium)
            }
            if (backup != null) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(GmsSpacing.Small),
                    verticalArrangement = Arrangement.spacedBy(GmsSpacing.Small),
                ) {
                    BackupCount(
                        stringResource(
                            R.string.backup_app_count,
                            backup.packages.map { it.androidPackageName }.distinct().size,
                        )
                    )
                    BackupCount(stringResource(R.string.backup_package_count, backup.packages.size))
                    BackupCount(stringResource(R.string.backup_flag_count, backup.flagCount))
                }
            }
        }
    }
}

@Composable
private fun BackupCount(text: String) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = GmsSpacing.Medium, vertical = GmsSpacing.Small),
        )
    }
}

@Composable
internal fun BackupProgressCard() {
    val progressLabel = stringResource(R.string.backup_working)
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(GmsSpacing.Large).semantics {
                liveRegion = LiveRegionMode.Polite
            },
            verticalArrangement = Arrangement.spacedBy(GmsSpacing.Medium),
        ) {
            Text(
                stringResource(R.string.backup_working),
                style = MaterialTheme.typography.labelLarge,
            )
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth().semantics { contentDescription = progressLabel }
            )
        }
    }
}

@Composable
internal fun BackupMessageCard(message: Int) {
    val error = message != R.string.backup_saved && message != R.string.backup_empty
    BackupStatusCard(
        icon = if (error) {
            Icons.Rounded.ErrorOutline
        } else if (message == R.string.backup_saved) {
            Icons.Rounded.CheckCircle
        } else {
            Icons.Rounded.Info
        },
        error = error,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(stringResource(message), style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
internal fun BackupMergeCard() {
    BackupStatusCard(icon = Icons.Rounded.Info) {
        Text(
            stringResource(R.string.backup_merge_title),
            style = MaterialTheme.typography.titleSmall,
        )
        Text(
            stringResource(R.string.backup_merge_description),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
internal fun BackupResultCard(result: BackupRestoreResult) {
    val incomplete = result.failedPackages.isNotEmpty() ||
        result.skippedPackages.isNotEmpty() ||
        result.restartFailedApps.isNotEmpty() ||
        result.refreshFailed
    BackupStatusCard(
        icon = if (incomplete) Icons.Rounded.WarningAmber else Icons.Rounded.CheckCircle,
        error = result.failedPackages.isNotEmpty(),
    ) {
        Text(
            stringResource(
                if (incomplete) R.string.backup_result_attention
                else R.string.backup_result_complete
            ),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            stringResource(
                R.string.backup_result,
                result.restoredFlags,
                result.skippedPackages.size,
                result.failedPackages.size,
            ),
            style = MaterialTheme.typography.bodyMedium,
        )
        if (result.failedPackages.isNotEmpty()) {
            Text(
                stringResource(R.string.backup_partial_failure),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        if (result.refreshFailed) {
            Text(
                stringResource(R.string.backup_refresh_failed),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        if (result.restartFailedApps.isNotEmpty()) {
            Text(
                stringResource(
                    R.string.backup_restart_failed,
                    result.restartFailedApps.joinToString(),
                ),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun BackupStatusCard(
    icon: ImageVector,
    error: Boolean = false,
    verticalAlignment: Alignment.Vertical = Alignment.Top,
    content: @Composable () -> Unit,
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = if (error) {
            MaterialTheme.colorScheme.errorContainer
        } else {
            MaterialTheme.colorScheme.secondaryContainer
        },
        contentColor = if (error) {
            MaterialTheme.colorScheme.onErrorContainer
        } else {
            MaterialTheme.colorScheme.onSecondaryContainer
        },
        modifier = Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite },
    ) {
        Row(
            modifier = Modifier.padding(GmsSpacing.Large),
            horizontalArrangement = Arrangement.spacedBy(GmsSpacing.Medium),
            verticalAlignment = verticalAlignment,
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(GmsSpacing.Small),
            ) {
                content()
            }
        }
    }
}

@Composable
internal fun BackupPackageCard(
    pkg: BackupPackage,
    skipped: Boolean,
    failed: Boolean,
    restored: Boolean,
    exported: Boolean = false,
) {
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(GmsSpacing.Large),
            verticalArrangement = Arrangement.spacedBy(GmsSpacing.Medium),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(GmsSpacing.Medium),
            ) {
                BackupIcon(
                    Icons.Rounded.Inventory2,
                    MaterialTheme.colorScheme.surfaceContainerHighest,
                    MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(GmsSpacing.ExtraSmall),
                ) {
                    Text(pkg.androidPackageName, style = MaterialTheme.typography.titleSmall)
                    Text(
                        pkg.phenotypePackageName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(GmsSpacing.Small),
                verticalArrangement = Arrangement.spacedBy(GmsSpacing.Small),
            ) {
                BackupCount(stringResource(R.string.backup_flag_count, pkg.flags.size))
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = if (failed) {
                        MaterialTheme.colorScheme.errorContainer
                    } else {
                        MaterialTheme.colorScheme.secondaryContainer
                    },
                    contentColor = if (failed) {
                        MaterialTheme.colorScheme.onErrorContainer
                    } else {
                        MaterialTheme.colorScheme.onSecondaryContainer
                    },
                ) {
                    Text(
                        stringResource(
                            if (exported) R.string.backup_status_saved
                            else if (failed) R.string.backup_status_failed
                            else if (skipped) R.string.backup_status_skipped
                            else if (restored) R.string.backup_status_restored
                            else R.string.backup_status_ready
                        ),
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(
                            horizontal = GmsSpacing.Medium,
                            vertical = GmsSpacing.Small,
                        ),
                    )
                }
            }
            if (skipped) {
                Text(
                    stringResource(R.string.backup_unavailable),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (failed) {
                Text(
                    stringResource(R.string.backup_package_failed),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun BackupIcon(icon: ImageVector, containerColor: Color, contentColor: Color) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = containerColor,
        contentColor = contentColor,
        modifier = Modifier.size(GmsDimensions.MinimumTouchTarget),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
        }
    }
}
