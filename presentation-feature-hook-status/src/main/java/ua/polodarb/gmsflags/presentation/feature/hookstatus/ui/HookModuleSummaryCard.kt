package ua.polodarb.gmsflags.presentation.feature.hookstatus.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Rule
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.ReportProblem
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing
import ua.polodarb.gmsflags.presentation.feature.hookstatus.R

@Composable
internal fun HookModuleSummaryCard(
    moduleObserved: Boolean,
    workingCount: Int,
    attentionCount: Int,
    modifier: Modifier = Modifier,
) {
    val containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    val contentColor = MaterialTheme.colorScheme.onSurface

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = containerColor,
        contentColor = contentColor,
    ) {
        Column(
            modifier = Modifier.padding(GmsSpacing.Large),
            verticalArrangement = Arrangement.spacedBy(GmsSpacing.Medium),
        ) {
            ModuleState(
                observed = moduleObserved,
                title = stringResource(
                    if (moduleObserved) R.string.hook_status_module_observed
                    else R.string.hook_status_module_not_observed
                ),
                support = stringResource(
                    if (moduleObserved) R.string.hook_status_module_observed_support
                    else R.string.hook_status_module_not_observed_support
                ),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(GmsSpacing.Small),
            ) {
                SummaryMetric(
                    icon = Icons.Outlined.Verified,
                    text = stringResource(R.string.hook_status_working_count, workingCount),
                    emphasized = workingCount > 0,
                    modifier = Modifier.weight(1f),
                )
                SummaryMetric(
                    icon = Icons.Outlined.ReportProblem,
                    text = stringResource(R.string.hook_status_attention_count, attentionCount),
                    isWarning = attentionCount > 0,
                    modifier = Modifier.weight(1f),
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(GmsSpacing.Medium)) {
                SummaryCapability(
                    icon = Icons.Outlined.AdminPanelSettings,
                    title = stringResource(R.string.hook_status_root_ready),
                    support = stringResource(R.string.hook_status_root_support),
                )
                SummaryCapability(
                    icon = Icons.AutoMirrored.Outlined.Rule,
                    title = stringResource(R.string.hook_status_rules_ready),
                    support = stringResource(R.string.hook_status_rules_support),
                )
            }
        }
    }
}

@Composable
private fun ModuleState(
    observed: Boolean,
    title: String,
    support: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(GmsSpacing.Medium)) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            color = if (observed) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.surfaceContainerHighest,
            contentColor = if (observed) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurfaceVariant,
        ) {
            Row(
                modifier = Modifier.padding(
                    horizontal = GmsSpacing.Large,
                    vertical = GmsSpacing.Medium,
                ),
                horizontalArrangement = Arrangement.spacedBy(GmsSpacing.Medium),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = if (observed) Icons.Outlined.Verified
                    else Icons.Outlined.ErrorOutline,
                    contentDescription = null,
                    modifier = Modifier.size(HookStatusDimensions.HeroIcon),
                )
                Text(text = title, style = MaterialTheme.typography.titleMedium)
            }
        }
        Text(
            text = support,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun SummaryMetric(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier,
    emphasized: Boolean = false,
    isWarning: Boolean = false,
) {
    val containerColor = when {
        isWarning -> MaterialTheme.colorScheme.error
        emphasized -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.surfaceContainerHighest
    }
    val contentColor = when {
        isWarning -> MaterialTheme.colorScheme.onError
        emphasized -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.onSurface
    }
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = containerColor,
        contentColor = contentColor,
    ) {
        Row(
            modifier = Modifier.padding(GmsSpacing.Medium),
            horizontalArrangement = Arrangement.spacedBy(GmsSpacing.Small),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(HookStatusDimensions.CompactIcon),
            )
            Text(text = text, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun SummaryCapability(
    icon: ImageVector,
    title: String,
    support: String,
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(GmsSpacing.Medium),
            horizontalArrangement = Arrangement.spacedBy(GmsSpacing.Medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceBright,
                contentColor = MaterialTheme.colorScheme.onSurface,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.padding(GmsSpacing.Small)
                        .size(HookStatusDimensions.PipelineIcon),
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(GmsSpacing.ExtraSmall),
            ) {
                Text(text = title, style = MaterialTheme.typography.titleSmall)
                Text(
                    text = support,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}
