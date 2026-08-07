package ua.polodarb.gmsflags.presentation.feature.flagdetails.addmultiple.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import ua.polodarb.gmsflags.domain.flags.FlagOverride
import ua.polodarb.gmsflags.domain.flags.FlagType
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.LocalGmsAdaptiveLayout
import ua.polodarb.gmsflags.presentation.feature.flagdetails.R
import ua.polodarb.gmsflags.presentation.feature.flagdetails.ui.model.labelRes

private const val MaxVisibleFlags = 6

@Composable
internal fun AddMultipleFlagsPreview(
    flags: List<FlagOverride>,
    invalidTokenCount: Int,
    modifier: Modifier = Modifier,
) {
    val adaptiveLayout = LocalGmsAdaptiveLayout.current

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = adaptiveLayout.contentPadding,
                end = adaptiveLayout.contentPadding,
                bottom = GmsSpacing.Large,
            ),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(
            modifier = Modifier.padding(GmsSpacing.Large),
            verticalArrangement = Arrangement.spacedBy(GmsSpacing.Medium),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.add_multiple_review_title),
                    style = MaterialTheme.typography.titleMedium,
                )
                if (flags.isNotEmpty()) {
                    Surface(
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.secondaryContainer,
                    ) {
                        Text(
                            text = flags.size.toString(),
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(
                                horizontal = GmsSpacing.Medium,
                                vertical = GmsSpacing.ExtraSmall,
                            ),
                        )
                    }
                }
            }

            when {
                invalidTokenCount > 0 -> Text(
                    text = pluralStringResource(
                        R.plurals.add_multiple_invalid_count,
                        invalidTokenCount,
                        invalidTokenCount,
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
                flags.isEmpty() -> Text(
                    text = stringResource(R.string.add_multiple_review_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            flags.take(MaxVisibleFlags).forEach { flag -> FlagPreviewRow(flag) }

            val hiddenCount = flags.size - MaxVisibleFlags
            if (hiddenCount > 0) {
                Text(
                    text = stringResource(R.string.add_multiple_more_flags, hiddenCount),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun FlagPreviewRow(flag: FlagOverride) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = GmsSpacing.Medium, vertical = GmsSpacing.Small),
            horizontalArrangement = Arrangement.spacedBy(GmsSpacing.Medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(flag.type.labelRes),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = flag.name,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = when (flag.type) {
                    FlagType.Boolean -> stringResource(
                        if (flag.value == "1") R.string.add_multiple_enabled
                        else R.string.add_multiple_disabled
                    )
                    else -> flag.value
                },
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
