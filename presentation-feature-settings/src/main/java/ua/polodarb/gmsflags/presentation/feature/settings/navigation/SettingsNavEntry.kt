package ua.polodarb.gmsflags.presentation.feature.settings.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import ua.polodarb.gmsflags.presentation.core.navigation.RootDestination
import ua.polodarb.gmsflags.presentation.feature.settings.faq.FaqScreen
import ua.polodarb.gmsflags.presentation.feature.settings.overrides.OverridesScreen
import ua.polodarb.gmsflags.presentation.feature.settings.overview.SettingsScreen

fun EntryProviderScope<NavKey>.settingsEntries(
    onBack: () -> Unit,
    onHookStatus: () -> Unit,
    onOverrides: () -> Unit,
    onFaq: () -> Unit,
    onImportSelected: (String) -> Unit,
) {
    entry<RootDestination.Settings> {
        SettingsScreen(
            onBack = onBack,
            onHookStatus = onHookStatus,
            onOverrides = onOverrides,
            onFaq = onFaq,
        )
    }
    entry<RootDestination.OverridesStorage> {
        OverridesScreen(onBack = onBack, onImportSelected = onImportSelected)
    }
    entry<RootDestination.Faq> {
        FaqScreen(onBack = onBack)
    }
}
