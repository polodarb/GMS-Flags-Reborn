package ua.polodarb.gmsflags.presentation.feature.apps.ui.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing
import ua.polodarb.gmsflags.presentation.feature.apps.R
import ua.polodarb.gmsflags.presentation.feature.apps.ui.model.FlagPackageCategoryUi
import ua.polodarb.gmsflags.presentation.feature.apps.ui.model.FlagPackageUiModel

@Composable
internal fun DialogPackagesList(
    packages: List<FlagPackageUiModel>,
    onPackageClick: (String) -> Unit,
) {
    val groupedPackages = remember(packages) {
        packages.groupBy(FlagPackageUiModel::category)
    }
    val primary = groupedPackages[FlagPackageCategoryUi.Primary].orEmpty()
    val secondary = groupedPackages[FlagPackageCategoryUi.Secondary].orEmpty()

    LazyColumn(modifier = Modifier.clip(MaterialTheme.shapes.large)) {
        if (primary.isNotEmpty() && packages.size > 1) {
            packageGroup(R.string.primary, primary, onPackageClick)
            if (secondary.isNotEmpty()) {
                packageGroup(R.string.secondary, secondary, onPackageClick)
            }
        } else {
            packageItems(packages, onPackageClick)
        }
    }
}

private fun LazyListScope.packageGroup(
    labelRes: Int,
    packages: List<FlagPackageUiModel>,
    onPackageClick: (String) -> Unit,
) {
    item(key = "header:$labelRes") { PackageGroupLabel(stringResource(labelRes)) }
    packageItems(packages, onPackageClick)
}

private fun LazyListScope.packageItems(
    packages: List<FlagPackageUiModel>,
    onPackageClick: (String) -> Unit,
) {
    itemsIndexed(
        items = packages,
        key = { _, item -> item.packageName },
        contentType = { _, _ -> "package" },
    ) { index, item ->
        PackageListItem(
            packageName = item.packageName,
            listStart = index == 0,
            listEnd = index == packages.lastIndex,
            onClick = onPackageClick,
        )
    }
}

@Composable
private fun PackageGroupLabel(text: String) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(
            start = GmsSpacing.Small,
            top = GmsSpacing.Large,
            bottom = GmsSpacing.Small,
        ),
    )
}

@Composable
private fun PackageListItem(
    packageName: String,
    listStart: Boolean,
    listEnd: Boolean,
    onClick: (String) -> Unit,
) {
    Box(
        modifier = Modifier
            .padding(vertical = GmsSpacing.ExtraSmall)
            .clip(packageItemShape(listStart, listEnd))
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .clickable { onClick(packageName) },
    ) {
        Text(
            text = packageName,
            modifier = Modifier
                .fillMaxWidth()
                .padding(GmsSpacing.Medium),
        )
    }
}

private fun packageItemShape(listStart: Boolean, listEnd: Boolean) = when {
    listStart && listEnd -> RoundedCornerShape(GmsSpacing.Large)
    listStart -> RoundedCornerShape(
        topStart = GmsSpacing.Large,
        topEnd = GmsSpacing.Large,
        bottomStart = GmsSpacing.ExtraSmall,
        bottomEnd = GmsSpacing.ExtraSmall,
    )
    listEnd -> RoundedCornerShape(
        topStart = GmsSpacing.ExtraSmall,
        topEnd = GmsSpacing.ExtraSmall,
        bottomStart = GmsSpacing.Large,
        bottomEnd = GmsSpacing.Large,
    )
    else -> RoundedCornerShape(GmsSpacing.ExtraSmall)
}
