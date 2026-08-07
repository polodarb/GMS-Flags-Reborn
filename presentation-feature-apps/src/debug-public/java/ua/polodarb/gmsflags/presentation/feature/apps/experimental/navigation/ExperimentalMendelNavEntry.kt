package ua.polodarb.gmsflags.presentation.feature.apps.experimental.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey

fun EntryProviderScope<NavKey>.experimentalAppsScreenEntry(
    onApplicationSelected: (String, String) -> Unit,
    onSettingsSelected: () -> Unit,
) = Unit
