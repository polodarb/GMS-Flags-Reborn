package ua.polodarb.gmsflags.navigation.entry

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import ua.polodarb.gmsflags.navigation.BottomBarNavDisplay
import ua.polodarb.gmsflags.presentation.core.navigation.RootDestination

internal fun EntryProviderScope<NavKey>.bottomBarFlowEntry(
    onRecommendationSelected: (Long) -> Unit,
    onApplicationSelected: (
        androidPackageName: String,
        applicationName: String,
        phenotypePackageName: String,
        availablePhenotypePackageNames: List<String>,
    ) -> Unit,
    onSettingsSelected: () -> Unit,
    onOverridesSelected: () -> Unit,
    onExperimentalApplicationSelected: (String, String) -> Unit,
) {
    entry<RootDestination.BottomBarFlow> {
        BottomBarNavDisplay(
            onRecommendationSelected = onRecommendationSelected,
            onApplicationSelected = onApplicationSelected,
            onSettingsSelected = onSettingsSelected,
            onOverridesSelected = onOverridesSelected,
            onExperimentalApplicationSelected = onExperimentalApplicationSelected,
        )
    }
}
