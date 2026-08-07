package ua.polodarb.gmsflags.navigation

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import ua.polodarb.gmsflags.navigation.model.BottomBarNavigation
import ua.polodarb.gmsflags.navigation.state.rememberBottomBarNavigationState
import ua.polodarb.gmsflags.navigation.ui.BottomNavigationBar
import ua.polodarb.gmsflags.navigation.ui.SideNavigationRail
import ua.polodarb.gmsflags.presentation.core.navigation.AppNavConfig
import ua.polodarb.gmsflags.presentation.core.navigation.BottomBarDestination
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsNavigationType
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.LocalGmsAdaptiveLayout
import ua.polodarb.gmsflags.presentation.feature.apps.navigation.appsScreenEntry
import ua.polodarb.gmsflags.presentation.feature.apps.experimental.navigation.experimentalAppsScreenEntry
import ua.polodarb.gmsflags.presentation.feature.insight.navigation.insightScreenEntry
import ua.polodarb.gmsflags.presentation.feature.suggestions.navigation.suggestionsScreenEntry
import ua.polodarb.gmsflags.presentation.core.ui.layout.GmsTopLevelHeaderHost
import ua.polodarb.gmsflags.presentation.core.ui.layout.GmsTopLevelHeaderState
import ua.polodarb.gmsflags.presentation.core.ui.layout.LocalGmsTopLevelHeaderState
import ua.polodarb.gmsflags.presentation.core.ui.layout.LocalGmsTopLevelNotice
import ua.polodarb.gmsflags.presentation.feature.settings.ui.OverridesPausedNotice

@Composable
internal fun BottomBarNavDisplay(
    onRecommendationSelected: (Long) -> Unit,
    onApplicationSelected: (
        androidPackageName: String,
        applicationName: String,
        phenotypePackageName: String,
        availablePhenotypePackageNames: List<String>,
    ) -> Unit,
    onSettingsSelected: () -> Unit,
    onOverridesSelected: () -> Unit,
    onExperimentalApplicationSelected: (String, String) -> Unit,
) {
    val navigationState = rememberBottomBarNavigationState(
        startDestination = BottomBarDestination.Suggestions,
        destinations = BottomBarNavigation.items.map { it.destination },
        configuration = AppNavConfig.config,
    )
    val currentDestination = navigationState.selectedDestination
    val headerState = remember { GmsTopLevelHeaderState() }
    val entries = navigationState.decoratedEntries(
        entryProvider = entryProvider {
            suggestionsScreenEntry(
                onRecommendationSelected = onRecommendationSelected,
                onSettingsSelected = onSettingsSelected,
            )
            appsScreenEntry(
                onApplicationSelected = onApplicationSelected,
                onHookStatusSelected = onSettingsSelected,
            )
            insightScreenEntry(onSettingsSelected = onSettingsSelected)
            experimentalAppsScreenEntry(
                onApplicationSelected = onExperimentalApplicationSelected,
                onSettingsSelected = onSettingsSelected,
            )
        },
    )

    CompositionLocalProvider(
        LocalGmsTopLevelNotice provides {
            OverridesPausedNotice(onClick = onOverridesSelected)
        },
        LocalGmsTopLevelHeaderState provides headerState,
    ) {
    when (LocalGmsAdaptiveLayout.current.navigationType) {
        GmsNavigationType.BottomBar -> Scaffold(
            containerColor = MaterialTheme.colorScheme.surfaceBright,
            bottomBar = {
                BottomNavigationBar(
                    items = BottomBarNavigation.items,
                    selectedDestination = currentDestination,
                    onDestinationSelected = navigationState::select,
                )
            },
        ) { contentPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
                    .consumeWindowInsets(contentPadding),
            ) {
                GmsTopLevelHeaderHost(
                    state = headerState,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
                BottomBarNavContent(
                    entries = entries,
                    onBack = navigationState::navigateBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                )
            }
        }

        GmsNavigationType.Rail -> Row(modifier = Modifier.fillMaxSize()) {
            SideNavigationRail(
                items = BottomBarNavigation.items,
                selectedDestination = currentDestination,
                onDestinationSelected = navigationState::select,
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .windowInsetsPadding(
                        WindowInsets.safeDrawing.only(WindowInsetsSides.Vertical)
                    ),
            ) {
                GmsTopLevelHeaderHost(
                    state = headerState,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
                BottomBarNavContent(
                    entries = entries,
                    onBack = navigationState::navigateBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                )
            }
        }
    }
    }
}

@Composable
private fun BottomBarNavContent(
    entries: List<NavEntry<NavKey>>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    NavDisplay(
        entries = entries,
        onBack = onBack,
        modifier = modifier,
        transitionSpec = { noNavigationTransition() },
        popTransitionSpec = { noNavigationTransition() },
        predictivePopTransitionSpec = { noNavigationTransition() },
    )
}

private fun noNavigationTransition() =
    ContentTransform(EnterTransition.None, ExitTransition.None)
