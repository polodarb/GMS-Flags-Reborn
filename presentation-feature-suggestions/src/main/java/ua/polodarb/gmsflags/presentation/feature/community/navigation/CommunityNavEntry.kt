package ua.polodarb.gmsflags.presentation.feature.community.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import ua.polodarb.gmsflags.presentation.core.navigation.BottomBarDestination
import ua.polodarb.gmsflags.presentation.feature.community.ui.CommunityScreen

fun EntryProviderScope<NavKey>.communityScreenEntry(
    onSettingsSelected: () -> Unit = {},
) {
    entry<BottomBarDestination.Community> {
        CommunityScreen(
            onSettingsSelected = onSettingsSelected,
        )
    }
}
