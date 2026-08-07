package ua.polodarb.gmsflags.presentation.feature.hookstatus.details

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import ua.polodarb.gmsflags.domain.hookstatus.HookApplicationStatus
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing
import ua.polodarb.gmsflags.presentation.feature.hookstatus.R
import ua.polodarb.gmsflags.presentation.feature.hookstatus.ui.titleResource

@Composable
internal fun HookNoSessionNotice(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
    ) {
        Row(
            modifier = Modifier.padding(GmsSpacing.Large),
            horizontalArrangement = Arrangement.spacedBy(GmsSpacing.Medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(imageVector = Icons.Outlined.Info, contentDescription = null)
            Text(
                text = stringResource(R.string.hook_status_no_session),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
internal fun HookTechnicalDetails(
    status: HookApplicationStatus,
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "technical-details-chevron",
    )
    Surface(
        onClick = onToggle,
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(GmsSpacing.Large),
                horizontalArrangement = Arrangement.spacedBy(GmsSpacing.Medium),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(imageVector = Icons.Outlined.Info, contentDescription = null)
                Text(
                    text = stringResource(R.string.hook_status_technical_title),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                )
                Icon(
                    imageVector = Icons.Outlined.ExpandMore,
                    contentDescription = stringResource(
                        if (expanded) R.string.hook_status_technical_hide
                        else R.string.hook_status_technical_show
                    ),
                    modifier = Modifier.rotate(rotation),
                )
            }
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier.padding(
                        start = GmsSpacing.Large,
                        end = GmsSpacing.Large,
                        bottom = GmsSpacing.Large,
                    ),
                    verticalArrangement = Arrangement.spacedBy(GmsSpacing.Medium),
                ) {
                    TechnicalRow(
                        label = stringResource(R.string.hook_status_package),
                        value = status.androidPackageName,
                    )
                    status.compatibilityWarnings.forEach { warning ->
                        TechnicalRow(
                            label = stringResource(R.string.hook_status_compatibility),
                            value = stringResource(warning.titleResource()),
                        )
                    }
                    status.session?.let { session ->
                        TechnicalRow(
                            label = stringResource(R.string.hook_status_process),
                            value = session.processName,
                        )
                        TechnicalRow(
                            label = stringResource(R.string.hook_status_version),
                            value = session.versionCode.toString(),
                        )
                        session.strategies.forEach { strategy ->
                            Text(
                                text = stringResource(
                                    R.string.hook_status_strategy_format,
                                    strategy.name,
                                    strategy.state.name,
                                    strategy.appliedCount,
                                    strategy.consumedCount,
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TechnicalRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(GmsSpacing.ExtraSmall)) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium,
        )
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}
