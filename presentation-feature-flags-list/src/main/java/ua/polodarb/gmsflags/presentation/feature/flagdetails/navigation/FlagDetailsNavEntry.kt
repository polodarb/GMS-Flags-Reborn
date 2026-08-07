package ua.polodarb.gmsflags.presentation.feature.flagdetails.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import ua.polodarb.gmsflags.presentation.core.navigation.RootDestination
import ua.polodarb.gmsflags.presentation.feature.flagdetails.addmultiple.ui.AddMultipleFlagsScreen
import ua.polodarb.gmsflags.presentation.feature.flagdetails.importflags.ui.ImportFlagsScreen
import ua.polodarb.gmsflags.presentation.feature.flagdetails.importflags.external.mvi.ExternalImportTarget
import ua.polodarb.gmsflags.presentation.feature.flagdetails.importflags.external.ui.ExternalImportScreen
import ua.polodarb.gmsflags.presentation.feature.flagdetails.ui.FlagDetailsScreen

fun EntryProviderScope<NavKey>.flagDetailsEntries(
    onBack: () -> Unit,
    onAddMultiple: (androidPackageName: String, phenotypePackageName: String) -> Unit,
    onImportFlags: (
        androidPackageName: String,
        applicationName: String,
        currentPhenotypePackageName: String,
        supportedPhenotypePackageNames: List<String>,
        documentUri: String,
    ) -> Unit,
    onImportCompleted: (phenotypePackageName: String) -> Unit,
    onExternalImportResolved: (
        target: ExternalImportTarget,
        documentUri: String,
    ) -> Unit,
    onExternalImportOpeningFinished: (documentUri: String) -> Unit,
    onRecommendationSelected: (id: Long) -> Unit,
) {
    entry<RootDestination.FlagDetails> { destination ->
        FlagDetailsScreen(
            androidPackageName = destination.androidPackageName,
            applicationName = destination.applicationName,
            phenotypePackageName = destination.phenotypePackageName,
            availablePhenotypePackageNames = destination.availablePhenotypePackageNames,
        ) { action ->
            when (action) {
                FlagDetailsScreenAction.Back -> onBack()
                is FlagDetailsScreenAction.AddMultiple -> onAddMultiple(
                    action.androidPackageName,
                    action.phenotypePackageName,
                )
                is FlagDetailsScreenAction.ImportFlags -> onImportFlags(
                    action.androidPackageName,
                    action.applicationName,
                    action.currentPhenotypePackageName,
                    action.supportedPhenotypePackageNames,
                    action.documentUri,
                )
                is FlagDetailsScreenAction.OpenRecommendation -> {
                    onRecommendationSelected(action.id)
                }
            }
        }
    }
    entry<RootDestination.AddMultipleFlags> { destination ->
        AddMultipleFlagsScreen(
            androidPackageName = destination.androidPackageName,
            phenotypePackageName = destination.phenotypePackageName,
            onBack = onBack,
        )
    }
    entry<RootDestination.ImportFlags> { destination ->
        ImportFlagsScreen(
            androidPackageName = destination.androidPackageName,
            currentPhenotypePackageName = destination.currentPhenotypePackageName,
            supportedPhenotypePackageNames = destination.supportedPhenotypePackageNames,
            documentUri = destination.documentUri,
            onBack = onBack,
            onImported = onImportCompleted,
            onContentReady = {
                onExternalImportOpeningFinished(destination.documentUri)
            },
        )
    }
    entry<RootDestination.ExternalImportFlags> { destination ->
        ExternalImportScreen(
            documentUri = destination.documentUri,
            onBack = {
                onExternalImportOpeningFinished(destination.documentUri)
                onBack()
            },
            onTargetResolved = { target ->
                onExternalImportResolved(target, destination.documentUri)
            },
            onUserInteractionRequired = {
                onExternalImportOpeningFinished(destination.documentUri)
            },
        )
    }
}
