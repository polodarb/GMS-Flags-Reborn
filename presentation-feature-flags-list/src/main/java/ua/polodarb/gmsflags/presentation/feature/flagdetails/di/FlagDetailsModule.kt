package ua.polodarb.gmsflags.presentation.feature.flagdetails.di

import org.koin.core.module.dsl.viewModel
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import ua.polodarb.gmsflags.presentation.feature.flagdetails.importflags.document.ContentResolverFlagImportDocumentSource
import ua.polodarb.gmsflags.presentation.feature.flagdetails.importflags.document.FlagImportDocumentSource
import ua.polodarb.gmsflags.presentation.feature.flagdetails.importflags.parser.GmsFlagsFileParser
import ua.polodarb.gmsflags.presentation.feature.flagdetails.importflags.ui.ImportFlagsViewModel
import ua.polodarb.gmsflags.presentation.feature.flagdetails.importflags.external.ui.ExternalImportViewModel
import ua.polodarb.gmsflags.presentation.feature.flagdetails.addmultiple.ui.AddMultipleFlagsViewModel
import ua.polodarb.gmsflags.presentation.feature.flagdetails.addmultiple.parser.FlagBatchParser
import ua.polodarb.gmsflags.presentation.feature.flagdetails.ui.FlagDetailsViewModel

val presentationFeatureFlagDetailsModule = module {
    factory { FlagBatchParser() }
    factory { GmsFlagsFileParser() }
    factory<FlagImportDocumentSource> {
        ContentResolverFlagImportDocumentSource(androidContext().contentResolver)
    }
    viewModel { parameters ->
        FlagDetailsViewModel(
            androidPackageName = parameters.get(0),
            applicationName = parameters.get(1),
            initialPhenotypePackageName = parameters.get(2),
            availablePhenotypePackageNames = parameters.get(3),
            getFlags = get(),
            applyOverrides = get(),
            deleteOverride = get(),
            deleteOverrides = get(),
            deletePackageOverrides = get(),
            observeFlagOverrideChanges = get(),
            getApplicationScopeStatus = get(),
            getPairipIncompatiblePackages = get(),
            getServerApplication = get(),
            getApplicationRecommendations = get(),
            errorResolver = get(),
            analytics = get(),
            performanceTracer = get(),
            observeServerMode = get(),
        )
    }
    viewModel { parameters ->
        AddMultipleFlagsViewModel(
            androidPackageName = parameters.get(0),
            phenotypePackageName = parameters.get(1),
            applyOverrides = get(),
            parser = get(),
            errorResolver = get(),
            analytics = get(),
        )
    }
    viewModel { parameters ->
        ImportFlagsViewModel(
            androidPackageName = parameters.get(0),
            currentPhenotypePackageName = parameters.get(1),
            supportedPhenotypePackageNames = parameters.get(2),
            initialDocumentUri = parameters.get(3),
            documentSource = get(),
            parser = get(),
            applyOverrides = get(),
            errorResolver = get(),
            analytics = get(),
        )
    }
    viewModel { parameters ->
        ExternalImportViewModel(
            documentUri = parameters.get(0),
            documentSource = get(),
            parser = get(),
            getSupportedApplications = get(),
        )
    }
}
