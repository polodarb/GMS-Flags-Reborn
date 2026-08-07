package ua.polodarb.gmsflags.presentation.feature.flagdetails.importflags.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing
import ua.polodarb.gmsflags.presentation.core.ui.application.ApplicationIconProvider
import ua.polodarb.gmsflags.presentation.core.ui.application.GmsApplicationIcon
import ua.polodarb.gmsflags.presentation.feature.flagdetails.R

@Composable
internal fun ImportFlagsSummary(
    androidPackageName: String,
    packageName: String?,
    flagCount: Int,
    selectedCount: Int,
    skippedCount: Int,
    allSelected: Boolean,
    enabled: Boolean,
    onSelectAll: () -> Unit,
    onClearSelection: () -> Unit,
    onPackageClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val iconProvider = koinInject<ApplicationIconProvider>()
    val appLabel by produceState(androidPackageName, androidPackageName) {
        value = runCatching {
            val pm = context.packageManager
            pm.getApplicationLabel(pm.getApplicationInfo(androidPackageName, 0)).toString()
        }.getOrDefault(androidPackageName)
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(GmsSpacing.Large),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = packageName != null, onClick = onPackageClick),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceContainer,
        ) {
            Row(
                modifier = Modifier.padding(GmsSpacing.Large),
                horizontalArrangement = Arrangement.spacedBy(GmsSpacing.Large),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                GmsApplicationIcon(
                    packageName = androidPackageName,
                    applicationName = appLabel,
                    iconProvider = iconProvider,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(MaterialTheme.shapes.medium),
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(GmsSpacing.ExtraSmall),
                ) {
                    Text(
                        text = appLabel,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (packageName != null) {
                        Text(
                            text = packageName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(GmsSpacing.Small),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(GmsSpacing.ExtraSmall),
            ) {
                Text(
                    text = stringResource(
                        R.string.import_flags_selection_summary,
                        selectedCount,
                        flagCount,
                    ),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (skippedCount > 0) {
                    SkippedChip(count = skippedCount)
                }
            }
            TextButton(
                onClick = if (allSelected) onClearSelection else onSelectAll,
                enabled = enabled,
            ) {
                Text(
                    stringResource(
                        if (allSelected) R.string.import_flags_clear_selection
                        else R.string.import_flags_select_all
                    )
                )
            }
        }
    }
}

@Composable
private fun SkippedChip(count: Int) {
    Text(
        text = stringResource(R.string.import_flags_skipped, count),
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.errorContainer)
            .padding(horizontal = GmsSpacing.Small, vertical = GmsSpacing.Micro),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onErrorContainer,
    )
}
