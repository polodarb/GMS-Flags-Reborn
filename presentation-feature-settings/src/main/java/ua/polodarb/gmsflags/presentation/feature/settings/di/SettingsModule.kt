package ua.polodarb.gmsflags.presentation.feature.settings.di

import android.content.Context
import java.io.File
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import ua.polodarb.gmsflags.presentation.feature.settings.backup.BackupDocumentStore
import ua.polodarb.gmsflags.presentation.feature.settings.backup.BackupDocuments
import ua.polodarb.gmsflags.presentation.feature.settings.backup.BackupExportStore
import ua.polodarb.gmsflags.presentation.feature.settings.backup.BackupViewModel
import ua.polodarb.gmsflags.presentation.feature.settings.backup.FileBackupExportStore
import ua.polodarb.gmsflags.presentation.feature.settings.faq.FaqViewModel
import ua.polodarb.gmsflags.presentation.feature.settings.feedback.SendFeedbackViewModel
import ua.polodarb.gmsflags.presentation.feature.settings.overrides.OverridesViewModel
import ua.polodarb.gmsflags.presentation.feature.settings.overview.SettingsViewModel
import ua.polodarb.gmsflags.presentation.feature.settings.ui.OverrideNoticeViewModel

val presentationFeatureSettingsModule = module {
    factory<BackupDocumentStore> { BackupDocuments(get<Context>().contentResolver) }
    factory<BackupExportStore> {
        FileBackupExportStore(File(get<Context>().filesDir, "backup-exports"))
    }
    viewModel { parameters ->
        BackupViewModel(
            service = get(),
            codec = get(),
            documents = get(),
            exports = get(),
            savedStateHandle = get(),
            initialUri = parameters.getOrNull<String>(),
        )
    }
    viewModel { SettingsViewModel(get(), get(), get(), get(), get(), get(), get()) }
    viewModel { OverridesViewModel(get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel { OverrideNoticeViewModel(get()) }
    viewModel { FaqViewModel(get(), get()) }
    viewModel { SendFeedbackViewModel(get(), get()) }
}
