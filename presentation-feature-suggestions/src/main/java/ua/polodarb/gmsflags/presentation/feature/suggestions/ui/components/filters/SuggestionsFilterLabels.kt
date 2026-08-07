package ua.polodarb.gmsflags.presentation.feature.suggestions.ui.components.filters

import androidx.annotation.StringRes
import ua.polodarb.gmsflags.presentation.feature.suggestions.R
import ua.polodarb.gmsflags.presentation.feature.suggestions.mvi.SuggestionsApplicationStatusFilter
import ua.polodarb.gmsflags.presentation.feature.suggestions.mvi.SuggestionsSort
import ua.polodarb.gmsflags.presentation.feature.suggestions.ui.model.RecommendationSupportUiModel

internal val SuggestionsApplicationStatusFilter.labelRes: Int
    @StringRes get() = when (this) {
        SuggestionsApplicationStatusFilter.All -> R.string.suggestions_filter_status_all
        SuggestionsApplicationStatusFilter.NotEnabled -> R.string.suggestions_filter_not_enabled
        SuggestionsApplicationStatusFilter.Enabled -> R.string.suggestions_filter_enabled
        SuggestionsApplicationStatusFilter.DifferentSetup ->
            R.string.suggestions_filter_different_setup
        SuggestionsApplicationStatusFilter.Unknown -> R.string.suggestions_filter_unknown
    }

internal val RecommendationSupportUiModel.labelRes: Int
    @StringRes get() = when (this) {
        RecommendationSupportUiModel.Verified -> R.string.suggestions_support_verified
        RecommendationSupportUiModel.Partial -> R.string.suggestions_support_partial
        RecommendationSupportUiModel.Experimental -> R.string.suggestions_support_experimental
        RecommendationSupportUiModel.Unknown -> R.string.suggestions_support_unknown
    }

internal val SuggestionsSort.labelRes: Int
    @StringRes get() = when (this) {
        SuggestionsSort.Recommended -> R.string.suggestions_filter_sort_recommended
        SuggestionsSort.Application -> R.string.suggestions_filter_sort_application
    }
