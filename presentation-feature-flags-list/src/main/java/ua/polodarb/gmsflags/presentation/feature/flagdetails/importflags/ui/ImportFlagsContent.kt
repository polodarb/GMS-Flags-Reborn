package ua.polodarb.gmsflags.presentation.feature.flagdetails.importflags.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.LocalGmsAdaptiveLayout
import ua.polodarb.gmsflags.presentation.core.ui.layout.GmsBackHeader
import ua.polodarb.gmsflags.presentation.core.ui.layout.GmsContentContainer
import ua.polodarb.gmsflags.presentation.feature.flagdetails.R
import ua.polodarb.gmsflags.presentation.feature.flagdetails.importflags.mvi.ImportFlagsEvent
import ua.polodarb.gmsflags.presentation.feature.flagdetails.importflags.mvi.ImportFlagsState
import ua.polodarb.gmsflags.presentation.feature.flagdetails.importflags.ui.components.ImportFlagsBottomBar
import ua.polodarb.gmsflags.presentation.feature.flagdetails.importflags.ui.components.ImportFlagsStateContent
import ua.polodarb.gmsflags.presentation.feature.flagdetails.importflags.ui.dialogs.UnsupportedImportPackageDialog
import ua.polodarb.gmsflags.presentation.feature.flagdetails.importflags.ui.sheets.ImportFlagPackageSheet

@Composable
internal fun ImportFlagsContent(
    state: ImportFlagsState,
    onEvent: (ImportFlagsEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val adaptiveLayout = LocalGmsAdaptiveLayout.current

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceBright,
        topBar = {
            GmsBackHeader(
                title = stringResource(R.string.import_flags_title),
                onBack = { onEvent(ImportFlagsEvent.BackClicked) },
            )
        },
        bottomBar = {
            if (state.batch != null) {
                ImportFlagsBottomBar(
                    selectedCount = state.selectedCount,
                    applying = state.applying,
                    onApply = { onEvent(ImportFlagsEvent.ApplyClicked) },
                )
            }
        },
    ) { contentPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
        ) {
            GmsContentContainer(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = adaptiveLayout.contentMaxWidth)
                    .align(Alignment.Center),
            ) {
                ImportFlagsStateContent(
                    state = state,
                    onEvent = onEvent,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }

    state.unsupportedPackage?.let { unsupported ->
        UnsupportedImportPackageDialog(
            packageName = unsupported.packageName,
            onChooseAnother = {
                onEvent(ImportFlagsEvent.ChooseAnotherFileClicked)
            },
            onBack = { onEvent(ImportFlagsEvent.BackClicked) },
        )
    }

    state.packageOverrideTarget?.let {
        ImportFlagPackageSheet(
            currentPackageName = state.batch?.phenotypePackageName,
            supportedPackageNames = state.supportedPhenotypePackageNames,
            onPackageSelected = { packageName ->
                onEvent(ImportFlagsEvent.PackageOverrideSelected(packageName))
            },
            onDismiss = { onEvent(ImportFlagsEvent.PackageOverrideDismissed) },
        )
    }
}
