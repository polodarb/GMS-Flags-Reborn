package ua.polodarb.gmsflags.presentation.feature.apps.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import ua.polodarb.gmsflags.presentation.core.navigation.BottomBarDestination
import ua.polodarb.gmsflags.presentation.feature.apps.ui.AppsScreen

fun EntryProviderScope<NavKey>.appsScreenEntry(
    onApplicationSelected: (
        androidPackageName: String,
        applicationName: String,
        flagPackageName: String,
        availableFlagPackageNames: List<String>,
    ) -> Unit,
    onHookStatusSelected: () -> Unit,
) {
    entry<BottomBarDestination.Apps> {
        AppsScreen { action ->
            when (action) {
                AppsScreenAction.OpenHookStatus -> onHookStatusSelected()
                is AppsScreenAction.OpenApplication -> onApplicationSelected(
                    action.androidPackageName,
                    action.applicationName,
                    action.flagPackageName,
                    action.availableFlagPackageNames,
                )
            }
        }
    }
}
