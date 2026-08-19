package ua.polodarb.gmsflags.presentation.feature.community.navigation

import androidx.navigation3.runtime.NavEntry
import ua.polodarb.gmsflags.presentation.core.navigation.BottomBarDestination
import ua.polodarb.gmsflags.presentation.feature.community.ui.CommunityScreen

fun communityScreenEntry(
    onSettingsSelected: () -> Unit,
): NavEntry<BottomBarDestination.Community> {
    return NavEntry(
        key = BottomBarDestination.Community,
    ) {
        CommunityScreen(onSettingsSelected = onSettingsSelected)
    }
}
