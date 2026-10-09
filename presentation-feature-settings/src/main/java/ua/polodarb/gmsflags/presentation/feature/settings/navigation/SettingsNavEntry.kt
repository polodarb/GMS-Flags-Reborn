package ua.polodarb.gmsflags.presentation.feature.settings.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import ua.polodarb.gmsflags.presentation.core.navigation.RootDestination
import ua.polodarb.gmsflags.presentation.feature.settings.backup.BackupFlow
import ua.polodarb.gmsflags.presentation.feature.settings.backup.BackupScreen
import ua.polodarb.gmsflags.presentation.feature.settings.faq.FaqScreen
import ua.polodarb.gmsflags.presentation.feature.settings.overrides.OverridesScreen
import ua.polodarb.gmsflags.presentation.feature.settings.overview.SettingsScreen

fun EntryProviderScope<NavKey>.settingsEntries(
    onBack: () -> Unit,
    onHookStatus: () -> Unit,
    onOverrides: () -> Unit,
    onFaq: () -> Unit,
    onImportSelected: (String) -> Unit,
    onImportBackup: () -> Unit,
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
        OverridesScreen(
            onBack = onBack,
            onImportSelected = onImportSelected,
            onImportBackup = onImportBackup,
        )
    }
    entry<RootDestination.FlagsBackup> { destination ->
        if (destination.documentUri != null) {
            BackupScreen(
                flow = BackupFlow.Import,
                documentUri = destination.documentUri,
                onBack = onBack,
            )
        } else {
            OverridesScreen(
                onBack = onBack,
                onImportSelected = onImportSelected,
                onImportBackup = onImportBackup,
                initialBackupSheetVisible = true,
            )
        }
    }
    entry<RootDestination.ExportFlagsBackup> {
        BackupScreen(flow = BackupFlow.Export, onBack = onBack)
    }
    entry<RootDestination.ImportFlagsBackup> { destination ->
        BackupScreen(
            flow = BackupFlow.Import,
            documentUri = destination.documentUri,
            onBack = onBack,
        )
    }
    entry<RootDestination.Faq> { FaqScreen(onBack = onBack) }
}
