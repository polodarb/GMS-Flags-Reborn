package ua.polodarb.gmsflags.presentation.feature.insight.navigation
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import ua.polodarb.gmsflags.presentation.core.navigation.BottomBarDestination
import ua.polodarb.gmsflags.presentation.feature.insight.ui.InsightScreen
fun EntryProviderScope<NavKey>.insightScreenEntry(
    onSettingsSelected: () -> Unit,
) {
    entry<BottomBarDestination.GmsInsight> {
        InsightScreen(onSettingsSelected = onSettingsSelected)
    }
}
