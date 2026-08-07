package ua.polodarb.gmsflags.presentation.feature.apps.ui.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsSpacing
import ua.polodarb.gmsflags.presentation.feature.apps.R
import ua.polodarb.gmsflags.presentation.feature.apps.ui.model.ApplicationUiModel

@Composable
internal fun AppPackagesDialog(
    application: ApplicationUiModel,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onPackageClick: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val filteredPackages = remember(application.flagPackages, searchQuery) {
        application.flagPackages.filter {
            it.packageName.contains(searchQuery.trim(), ignoreCase = true)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.apps_dialog_choose_package)) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                PackageSearchField(
                    query = searchQuery,
                    onQueryChange = onSearchQueryChange,
                )
                Spacer(Modifier.height(GmsSpacing.Small))
                DialogPackagesList(
                    packages = filteredPackages,
                    onPackageClick = onPackageClick,
                )
            }
        },
        confirmButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        },
    )
}
