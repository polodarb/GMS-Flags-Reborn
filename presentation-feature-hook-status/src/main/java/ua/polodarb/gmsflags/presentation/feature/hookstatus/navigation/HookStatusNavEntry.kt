package ua.polodarb.gmsflags.presentation.feature.hookstatus.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import ua.polodarb.gmsflags.presentation.core.navigation.RootDestination
import ua.polodarb.gmsflags.presentation.feature.hookstatus.details.HookStatusDetailsScreen
import ua.polodarb.gmsflags.presentation.feature.hookstatus.overview.HookStatusScreen

fun EntryProviderScope<NavKey>.hookStatusEntries(
    onBack: () -> Unit,
    onApplicationSelected: (String) -> Unit,
) {
    entry<RootDestination.HookStatus> {
        HookStatusScreen(
            onBack = onBack,
            onApplicationSelected = onApplicationSelected,
        )
    }
    entry<RootDestination.HookStatusDetails> { destination ->
        HookStatusDetailsScreen(
            androidPackageName = destination.androidPackageName,
            onBack = onBack,
        )
    }
}
