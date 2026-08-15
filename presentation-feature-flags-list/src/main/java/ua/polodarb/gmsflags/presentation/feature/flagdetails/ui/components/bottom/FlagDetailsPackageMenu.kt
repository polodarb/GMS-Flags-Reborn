package ua.polodarb.gmsflags.presentation.feature.flagdetails.ui.components.bottom

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.UnfoldMore
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
    val filterable = packageNames.size >= MINIMUM_FILTERABLE_ITEMS
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val filterHeight = if (filterable) {
        GmsDimensions.DetailsPackageFilterHeight + GmsSpacing.Small
    } else {
        GmsSpacing.None
    }
    val desiredHeight = GmsDimensions.DetailsPackageMenuItemMinHeight * packageNames.size +
        GmsSpacing.Large + filterHeight
    val availableHeight = maxOf(
        GmsDimensions.MinimumTouchTarget * MINIMUM_VISIBLE_ITEMS + GmsSpacing.Large + filterHeight,
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
        var query by rememberSaveable { mutableStateOf("") }
        val visiblePackages = remember(packageNames, query) {
            val term = query.trim()
            if (term.isEmpty()) {
                packageNames
            } else {
                packageNames.filter { it.contains(term, ignoreCase = true) }
            }
        }

        Column(modifier = Modifier.fillMaxSize()) {
            if (filterable) {
                PackageFilterField(
                    query = query,
                    onQueryChange = { query = it },
                )
            }
            val listModifier = Modifier
                .weight(1f)
                .padding(horizontal = GmsSpacing.Small)
                .padding(bottom = GmsSpacing.Small)
                .clip(RoundedCornerShape(GmsSpacing.Large))
            if (visiblePackages.isEmpty()) {
                EmptyPackagesLabel(modifier = listModifier)
            } else {
                PackageList(
                    packageNames = visiblePackages,
                    currentPackageName = currentPackageName,
                    primaryPackageName = primaryPackageName,
                    onSelect = { packageName ->
                        dismiss()
                        onEvent(FlagDetailsEvent.PackageSelected(packageName))
                    },
                    modifier = listModifier,
                )
            }
        }
    }
}

@Composable
private fun PackageFilterField(
    query: String,
    onQueryChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = GmsSpacing.Small, vertical = GmsSpacing.Small)
            .heightIn(min = GmsDimensions.DetailsPackageFilterHeight),
        singleLine = true,
        shape = RoundedCornerShape(GmsSpacing.Large),
        textStyle = MaterialTheme.typography.labelLarge,
        placeholder = {
            Text(
                text = stringResource(R.string.flag_details_search_packages),
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        trailingIcon = {
            AnimatedVisibility(
                visible = query.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                IconButton(onClick = rememberHapticClick { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = stringResource(R.string.flag_details_clear_search),
                    )
                }
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
        ),
    )
}

@Composable
private fun EmptyPackagesLabel(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(GmsSpacing.Large),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.flag_details_packages_empty),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PackageList(
    packageNames: List<String>,
    currentPackageName: String,
    primaryPackageName: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = GmsSpacing.Small),
    ) {
        items(
            items = packageNames,
            key = { it },
            contentType = { "package" },
        ) { packageName ->
            PackageRow(
                packageName = packageName,
                selected = packageName == currentPackageName,
                primary = packageName == primaryPackageName,
                onSelect = onSelect,
                modifier = Modifier.animateItem(),
            )
        }
    }
}

@Composable
private fun PackageRow(
    packageName: String,
    selected: Boolean,
    primary: Boolean,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
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
                        if (!selected) onSelect(packageName)
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
                    imageVector = if (primary) Icons.Rounded.Star else Icons.Rounded.Check,
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

private const val MINIMUM_VISIBLE_ITEMS = 3
private const val MINIMUM_FILTERABLE_ITEMS = 6
