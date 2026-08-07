package ua.polodarb.gmsflags.presentation.feature.flagdetails.importflags.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsDimensions
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing
import ua.polodarb.gmsflags.presentation.feature.flagdetails.R
import ua.polodarb.gmsflags.presentation.feature.flagdetails.importflags.model.ImportedFlag
import ua.polodarb.gmsflags.presentation.feature.flagdetails.ui.model.labelRes

@Composable
internal fun ImportedFlagCard(
    flag: ImportedFlag,
    selected: Boolean,
    enabled: Boolean,
    onSelectedChange: (Boolean) -> Unit,
    resolvedPackageName: String?,
    showPackageLine: Boolean,
    onPackageClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = MaterialTheme.shapes.large
    Card(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = GmsDimensions.FlagCardMinHeight)
            .clip(shape)
            .clickable(enabled = enabled) { onSelectedChange(!selected) },
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.surfaceContainerHighest
            } else {
                MaterialTheme.colorScheme.surfaceContainerHigh
            },
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = GmsSpacing.Large,
                    vertical = GmsSpacing.Medium,
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(GmsSpacing.Medium),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(GmsSpacing.ExtraSmall),
            ) {
                Text(
                    text = flag.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = stringResource(flag.type.labelRes),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = flag.value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (showPackageLine) {
                    Text(
                        text = resolvedPackageName?.let {
                            stringResource(R.string.flag_details_package, it)
                        } ?: stringResource(R.string.import_flags_flag_unresolved_package),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.extraSmall)
                            .clickable(onClick = onPackageClick)
                            .padding(vertical = GmsSpacing.Micro),
                    )
                }
            }
            Checkbox(
                checked = selected,
                onCheckedChange = if (enabled) onSelectedChange else null,
            )
        }
    }
}
