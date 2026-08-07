package ua.polodarb.gmsflags.presentation.feature.hookstatus.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import ua.polodarb.gmsflags.domain.hookstatus.HookApplicationStatus
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsDimensions
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing
import ua.polodarb.gmsflags.presentation.feature.hookstatus.R
import ua.polodarb.gmsflags.presentation.feature.hookstatus.ui.HookStatusDimensions

@Composable
internal fun HookPipeline(
    status: HookApplicationStatus,
    modifier: Modifier = Modifier,
) {
    val steps = listOf(
        HookPipelineStep(
            title = stringResource(R.string.hook_status_pipeline_saved),
            supportingText = stringResource(
                R.string.hook_status_pipeline_saved_support,
                status.currentOverrideCount,
            ),
            icon = Icons.Outlined.Save,
            complete = status.currentOverrideCount > 0,
        ),
        HookPipelineStep(
            title = stringResource(R.string.hook_status_pipeline_loaded),
            supportingText = stringResource(
                R.string.hook_status_pipeline_loaded_support,
                status.loadedOverrideCount,
            ),
            icon = Icons.Outlined.CloudDownload,
            complete = status.session != null,
        ),
        HookPipelineStep(
            title = stringResource(R.string.hook_status_pipeline_installed),
            supportingText = stringResource(
                R.string.hook_status_pipeline_installed_support,
                status.installedStrategyCount,
            ),
            icon = Icons.Outlined.Extension,
            complete = status.installedStrategyCount > 0,
        ),
        HookPipelineStep(
            title = stringResource(R.string.hook_status_pipeline_applied),
            supportingText = stringResource(
                R.string.hook_status_pipeline_applied_support,
                status.appliedCount,
            ),
            icon = Icons.Outlined.Tune,
            complete = status.appliedCount > 0,
        ),
        HookPipelineStep(
            title = stringResource(R.string.hook_status_pipeline_consumed),
            supportingText = stringResource(
                R.string.hook_status_pipeline_consumed_support,
                status.consumedCount,
            ),
            icon = Icons.Outlined.TouchApp,
            complete = status.consumedCount > 0,
        ),
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(GmsSpacing.Medium),
    ) {
        Text(
            text = stringResource(R.string.hook_status_pipeline_title),
            modifier = Modifier.padding(horizontal = GmsSpacing.ExtraSmall),
            style = MaterialTheme.typography.titleMedium,
        )
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ) {
            Column {
                steps.forEach { step ->
                    PipelineStepRow(step = step)
                }
            }
        }
    }
}

@Composable
private fun PipelineStepRow(step: HookPipelineStep) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(GmsSpacing.Large),
        horizontalArrangement = Arrangement.spacedBy(GmsSpacing.Medium),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier.size(GmsDimensions.MinimumTouchTarget),
            shape = MaterialTheme.shapes.medium,
            color = if (step.complete) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.surfaceContainerHighest,
            contentColor = if (step.complete) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurfaceVariant,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = step.icon,
                    contentDescription = null,
                    modifier = Modifier.size(HookStatusDimensions.PipelineIcon),
                )
            }
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(GmsSpacing.ExtraSmall),
        ) {
            Text(text = step.title, style = MaterialTheme.typography.titleMedium)
            Text(
                text = step.supportingText,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        if (step.complete) {
            Icon(
                imageVector = Icons.Outlined.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(HookStatusDimensions.CompactIcon),
            )
        }
    }
}

private data class HookPipelineStep(
    val title: String,
    val supportingText: String,
    val icon: ImageVector,
    val complete: Boolean,
)
