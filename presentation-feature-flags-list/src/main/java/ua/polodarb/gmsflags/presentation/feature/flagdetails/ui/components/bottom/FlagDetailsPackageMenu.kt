package ua.polodarb.gmsflags.presentation.feature.flagdetails.ui.components.bottom

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.UnfoldMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ua.polodarb.gmsflags.presentation.core.ui.haptic.rememberHapticClick
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsDimensions
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing
import ua.polodarb.gmsflags.presentation.feature.flagdetails.R
import ua.polodarb.gmsflags.presentation.feature.flagdetails.mvi.FlagDetailsEvent

@Composable
internal fun FlagDetailsPackageMenu(
    currentPackageName: String,
    primaryPackageName: String,
    packageNames: List<String>,
    enabled: Boolean,
    onEvent: (FlagDetailsEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val desiredHeight =
        GmsDimensions.DetailsPackageMenuItemMinHeight * packageNames.size + GmsSpacing.Large
    val availableHeight = maxOf(
        GmsDimensions.MinimumTouchTarget * MINIMUM_VISIBLE_ITEMS + GmsSpacing.Large,
        screenHeight - GmsDimensions.HeaderHeight - GmsSpacing.ExtraHuge * 2,
    )

    BottomBarTransformMenu(
        enabled = enabled && packageNames.isNotEmpty(),
        expandedHeight = minOf(desiredHeight, availableHeight),
        alignment = BottomBarTransformMenuAlignment.Start,
        modifier = modifier.height(GmsDimensions.DetailsBottomBarHeight),
        triggerContent = {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = GmsSpacing.Large),
                horizontalArrangement = Arrangement.spacedBy(GmsSpacing.Small),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = currentPackageName,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Icon(
                    imageVector = Icons.Rounded.UnfoldMore,
                    contentDescription = stringResource(R.string.flag_details_choose_package),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
    ) { dismiss ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = GmsSpacing.Small),
        ) {
            items(
                items = packageNames,
                key = { it },
            ) { packageName ->
                val selected = packageName == currentPackageName
                val primary = packageName == primaryPackageName
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = GmsSpacing.Small)
                        .heightIn(min = GmsDimensions.DetailsPackageMenuItemMinHeight),
                    shape = RoundedCornerShape(GmsSpacing.Large),
                    color = if (selected) {
                        MaterialTheme.colorScheme.secondaryContainer
                    } else {
                        Color.Transparent
                    },
                ) {
                    Row(
                        modifier = Modifier
                            .selectable(
                                selected = selected,
                                role = Role.RadioButton,
                                onClick = rememberHapticClick {
                                    dismiss()
                                    if (!selected) {
                                        onEvent(FlagDetailsEvent.PackageSelected(packageName))
                                    }
                                },
                            )
                            .padding(
                                horizontal = GmsSpacing.Medium,
                                vertical = GmsSpacing.Medium,
                            ),
                        horizontalArrangement = Arrangement.spacedBy(GmsSpacing.Small),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = packageName,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.labelLarge,
                            color = if (selected) {
                                MaterialTheme.colorScheme.onSecondaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                        )
                        if (primary || selected) {
                            Icon(
                                imageVector = if (primary) {
                                    Icons.Rounded.Star
                                } else {
                                    Icons.Rounded.Check
                                },
                                contentDescription = stringResource(
                                    if (primary) {
                                        R.string.flag_details_primary_package
                                    } else {
                                        R.string.flag_details_current_package
                                    },
                                ),
                                tint = if (selected) {
                                    MaterialTheme.colorScheme.onSecondaryContainer
                                } else {
                                    MaterialTheme.colorScheme.primary
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

private const val MINIMUM_VISIBLE_ITEMS = 3
