package ua.polodarb.gmsflags.presentation.feature.suggestions.navigation
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import ua.polodarb.gmsflags.presentation.core.navigation.BottomBarDestination
import ua.polodarb.gmsflags.presentation.feature.suggestions.ui.SuggestionsScreen
import ua.polodarb.gmsflags.presentation.core.navigation.RootDestination
import ua.polodarb.gmsflags.presentation.feature.suggestions.details.ui.RecommendationDetailsScreen
fun EntryProviderScope<NavKey>.suggestionsScreenEntry(
    onRecommendationSelected: (Long) -> Unit,
    onSettingsSelected: () -> Unit,
) {
    entry<BottomBarDestination.Suggestions> {
        SuggestionsScreen(
            onRecommendationSelected = onRecommendationSelected,
            onSettingsSelected = onSettingsSelected,
        )
    }
}

fun EntryProviderScope<NavKey>.recommendationDetailsEntry(
    onBack: () -> Unit,
) {
    entry<RootDestination.RecommendationDetails> { destination ->
        RecommendationDetailsScreen(
            recommendationId = destination.recommendationId,
            onBack = onBack,
        )
    }
}
