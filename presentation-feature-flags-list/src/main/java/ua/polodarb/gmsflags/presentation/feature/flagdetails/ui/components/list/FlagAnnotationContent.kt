package ua.polodarb.gmsflags.presentation.feature.flagdetails.ui.components.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import ua.polodarb.gmsflags.domain.server.content.DangerLevel
import ua.polodarb.gmsflags.domain.server.content.ServerFlagCatalogEntry
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing
import ua.polodarb.gmsflags.presentation.feature.flagdetails.R

@Composable
internal fun FlagAnnotationContent(
    flagName: String,
    annotation: ServerFlagCatalogEntry?,
    nameStyle: TextStyle,
    nameColor: Color,
    modifier: Modifier = Modifier,
) {
    val title = annotation?.title?.takeIf(String::isNotBlank)
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(GmsSpacing.ExtraSmall),
    ) {
        title?.let {
            Text(
                text = it,
                style = nameStyle,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            text = flagName,
            style = if (title == null) nameStyle else MaterialTheme.typography.labelMedium,
            color = if (title == null) nameColor else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        annotation?.description?.takeIf(String::isNotBlank)?.let { description ->
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        annotation?.let { metadata ->
            val dangerLabel = metadata.dangerLevel.labelResOrNull()
            if (metadata.badges.isNotEmpty() || dangerLabel != null) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(GmsSpacing.Small),
                    verticalArrangement = Arrangement.spacedBy(GmsSpacing.ExtraSmall),
                ) {
                    dangerLabel?.let { labelRes ->
                        MetadataBadge(
                            label = stringResource(labelRes),
                            dangerLevel = metadata.dangerLevel,
                        )
                    }
                    metadata.badges.forEach { badge ->
                        MetadataBadge(label = badge.label)
                    }
                }
            }
        }
    }
}

@Composable
private fun MetadataBadge(
    label: String,
    dangerLevel: DangerLevel = DangerLevel.None,
) {
    val colors = when (dangerLevel) {
        DangerLevel.Caution -> MaterialTheme.colorScheme.tertiaryContainer to
            MaterialTheme.colorScheme.onTertiaryContainer
        DangerLevel.Dangerous -> MaterialTheme.colorScheme.errorContainer to
            MaterialTheme.colorScheme.onErrorContainer
        DangerLevel.None,
        DangerLevel.Unknown,
        -> MaterialTheme.colorScheme.surfaceContainerHighest to
            MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(
        shape = MaterialTheme.shapes.extraSmall,
        color = colors.first,
        contentColor = colors.second,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(
                horizontal = GmsSpacing.Small,
                vertical = GmsSpacing.ExtraSmall,
            ),
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

private fun DangerLevel.labelResOrNull(): Int? = when (this) {
    DangerLevel.Caution -> R.string.flag_annotation_caution
    DangerLevel.Dangerous -> R.string.flag_annotation_dangerous
    DangerLevel.None,
    DangerLevel.Unknown,
    -> null
}
