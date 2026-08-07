package ua.polodarb.gmsflags.navigation

import androidx.compose.animation.ContentTransform
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import ua.polodarb.gmsflags.navigation.entry.bottomBarFlowEntry
import ua.polodarb.gmsflags.presentation.core.navigation.AppNavConfig
import ua.polodarb.gmsflags.presentation.core.navigation.RootDestination
import ua.polodarb.gmsflags.presentation.core.navigation.fullScreenEnterTransition
import ua.polodarb.gmsflags.presentation.core.navigation.fullScreenExitTransition
import ua.polodarb.gmsflags.presentation.core.navigation.fullScreenPopEnterTransition
import ua.polodarb.gmsflags.presentation.core.navigation.fullScreenPopExitTransition
import ua.polodarb.gmsflags.presentation.feature.flagdetails.navigation.flagDetailsEntries
import ua.polodarb.gmsflags.presentation.feature.hookstatus.navigation.hookStatusEntries
import ua.polodarb.gmsflags.presentation.feature.suggestions.navigation.recommendationDetailsEntry
import ua.polodarb.gmsflags.presentation.feature.settings.navigation.settingsEntries

@Composable
fun RootNavDisplay(
    externalImportDocumentUri: String? = null,
    onExternalImportConsumed: () -> Unit = {},
) {
    val rootBackStack = rememberNavBackStack(
        configuration = AppNavConfig.config,
        RootDestination.BottomBarFlow,
    )
    var openingExternalImportUri by remember {
        mutableStateOf(externalImportDocumentUri)
    }

    LaunchedEffect(externalImportDocumentUri) {
        val documentUri = externalImportDocumentUri ?: return@LaunchedEffect
        openingExternalImportUri = documentUri
        if (rootBackStack.lastOrNull() != RootDestination.ExternalImportFlags(documentUri)) {
            rootBackStack.add(RootDestination.ExternalImportFlags(documentUri))
        }
        onExternalImportConsumed()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NavDisplay(
            backStack = rootBackStack,
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceBright),
            predictivePopTransitionSpec = {
                ContentTransform(
                    fullScreenPopEnterTransition(),
                    fullScreenPopExitTransition(),
                )
            },
            transitionSpec = {
                ContentTransform(
                    fullScreenEnterTransition(),
                    fullScreenExitTransition(),
                )
            },
            popTransitionSpec = {
                ContentTransform(
                    fullScreenPopEnterTransition(),
                    fullScreenPopExitTransition(),
                )
            },
            entryProvider = entryProvider {
                bottomBarFlowEntry(
                    onRecommendationSelected = { recommendationId ->
                        rootBackStack.add(
                            RootDestination.RecommendationDetails(recommendationId)
                        )
                    },
                    onSettingsSelected = {
                        rootBackStack.add(RootDestination.Settings)
                    },
                    onOverridesSelected = {
                        rootBackStack.add(RootDestination.OverridesStorage)
                    },
                    onExperimentalApplicationSelected = { packageName, applicationName ->
                        val experimentalIdentity = "experimental#$packageName"
                        rootBackStack.add(
                            RootDestination.FlagDetails(
                                androidPackageName = packageName,
                                applicationName = applicationName,
                                phenotypePackageName = experimentalIdentity,
                                availablePhenotypePackageNames = listOf(experimentalIdentity),
                            )
                        )
                    },
                    onApplicationSelected = {
                        androidPackageName,
                        applicationName,
                        phenotypePackageName,
                        availablePhenotypePackageNames ->
                        rootBackStack.add(
                            RootDestination.FlagDetails(
                                androidPackageName = androidPackageName,
                                applicationName = applicationName,
                                phenotypePackageName = phenotypePackageName,
                                availablePhenotypePackageNames =
                                    availablePhenotypePackageNames,
                            )
                        )
                    },
                )
                settingsEntries(
                    onBack = { rootBackStack.removeLastOrNull() },
                    onHookStatus = { rootBackStack.add(RootDestination.HookStatus) },
                    onOverrides = { rootBackStack.add(RootDestination.OverridesStorage) },
                    onFaq = { rootBackStack.add(RootDestination.Faq) },
                    onImportSelected = { documentUri ->
                        rootBackStack.add(RootDestination.ExternalImportFlags(documentUri))
                    },
                )
                hookStatusEntries(
                    onBack = { rootBackStack.removeLastOrNull() },
                    onApplicationSelected = { androidPackageName ->
                        rootBackStack.add(
                            RootDestination.HookStatusDetails(androidPackageName)
                        )
                    },
                )
                recommendationDetailsEntry(
                    onBack = { rootBackStack.removeLastOrNull() },
                )
                flagDetailsEntries(
                    onBack = { rootBackStack.removeLastOrNull() },
                    onAddMultiple = { androidPackageName, phenotypePackageName ->
                        rootBackStack.add(
                            RootDestination.AddMultipleFlags(
                                androidPackageName = androidPackageName,
                                phenotypePackageName = phenotypePackageName,
                            )
                        )
                    },
                    onImportFlags = {
                        androidPackageName,
                        applicationName,
                        currentPhenotypePackageName,
                        supportedPhenotypePackageNames,
                        documentUri ->
                        rootBackStack.add(
                            RootDestination.ImportFlags(
                                androidPackageName = androidPackageName,
                                applicationName = applicationName,
                                currentPhenotypePackageName = currentPhenotypePackageName,
                                supportedPhenotypePackageNames =
                                    supportedPhenotypePackageNames,
                                documentUri = documentUri,
                            )
                        )
                    },
                    onImportCompleted = { importedPackageName ->
                        val importDestination = rootBackStack.lastOrNull()
                            as? RootDestination.ImportFlags
                        val details = rootBackStack
                            .dropLast(1)
                            .lastOrNull() as? RootDestination.FlagDetails
                        rootBackStack.removeLastOrNull()
                        if (details != null && rootBackStack.lastOrNull() == details) {
                            rootBackStack.removeLastOrNull()
                            rootBackStack.add(
                                details.copy(
                                    phenotypePackageName = importedPackageName,
                                    availablePhenotypePackageNames = buildList {
                                        addAll(details.availablePhenotypePackageNames)
                                        add(importedPackageName)
                                    }.distinct(),
                                )
                            )
                        } else if (importDestination != null) {
                            rootBackStack.add(
                                RootDestination.FlagDetails(
                                    androidPackageName = importDestination.androidPackageName,
                                    applicationName = importDestination.applicationName,
                                    phenotypePackageName = importedPackageName,
                                    availablePhenotypePackageNames =
                                        importDestination.supportedPhenotypePackageNames,
                                )
                            )
                        }
                    },
                    onExternalImportResolved = { target, documentUri ->
                        rootBackStack.removeLastOrNull()
                        rootBackStack.add(
                            RootDestination.ImportFlags(
                                androidPackageName = target.androidPackageName,
                                applicationName = target.applicationName,
                                currentPhenotypePackageName = target.phenotypePackageName,
                                supportedPhenotypePackageNames =
                                    target.supportedPhenotypePackageNames,
                                documentUri = documentUri,
                            )
                        )
                    },
                    onExternalImportOpeningFinished = { documentUri ->
                        if (openingExternalImportUri == documentUri) {
                            openingExternalImportUri = null
                        }
                    },
                    onRecommendationSelected = { recommendationId ->
                        rootBackStack.add(
                            RootDestination.RecommendationDetails(recommendationId)
                        )
                    },
                )
            },
        )

        ExternalImportOpeningOverlay(
            visible = openingExternalImportUri != null,
            modifier = Modifier.fillMaxSize(),
        )
    }
}
