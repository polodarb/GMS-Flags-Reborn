package ua.polodarb.gmsflags.presentation.feature.suggestions.di
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import ua.polodarb.gmsflags.presentation.feature.suggestions.ui.SuggestionsViewModel
import ua.polodarb.gmsflags.presentation.feature.suggestions.details.ui.RecommendationDetailsViewModel
val presentationFeatureSuggestionsModule = module {
    viewModel {
        SuggestionsViewModel(
            getRecommendationFeed = get(),
            getHomeContent = get(),
            observeFlagOverrideChanges = get(),
            getApplicationScopeStatus = get(),
            locallyDemotedRecommendationsStore = get(),
            performanceTracer = get(),
            errorResolver = get(),
        )
    }
    viewModel { parameters ->
        RecommendationDetailsViewModel(
            recommendationId = parameters.get(),
            getRecommendation = get(),
            getPhenotypeFlags = get(),
            applyFlagOverrides = get(),
            applyMicroHooks = get(),
            deleteFlagOverrides = get(),
            deleteMicroHooks = get(),
            appliedSetupStore = get(),
            getApplicationScopeStatus = get(),
            verifyHookTrust = get(),
            decodeHookRecipe = get(),
            hookEngineSupport = get(),
            isOfficialAppBuild = get(),
            collectReportDiagnostics = get(),
            submitProblemReport = get(),
            errorResolver = get(),
            analytics = get(),
        )
    }
}
