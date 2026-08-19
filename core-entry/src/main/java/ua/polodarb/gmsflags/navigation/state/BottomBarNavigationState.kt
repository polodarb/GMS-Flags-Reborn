package ua.polodarb.gmsflags.navigation.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.savedstate.serialization.SavedStateConfiguration
import ua.polodarb.gmsflags.presentation.core.navigation.BottomBarDestination

@Stable
internal class BottomBarNavigationState(
    val startDestination: BottomBarDestination,
    private val selectedDestinationId: MutableState<String>,
    private val backStacks: Map<BottomBarDestination, NavBackStack<NavKey>>,
) {
    var selectedDestination: BottomBarDestination
        get() = selectedDestinationId.value.toBottomBarDestination()
        private set(value) {
            selectedDestinationId.value = value.id
        }

    fun select(destination: BottomBarDestination) {
        selectedDestination = destination
    }

    fun navigateBack() {
        selectedDestination = startDestination
    }

    @Composable
    fun decoratedEntries(
        entryProvider: (NavKey) -> NavEntry<NavKey>,
    ): List<NavEntry<NavKey>> {
        val entriesByDestination = backStacks.mapValues { (_, backStack) ->
            rememberDecoratedNavEntries(
                backStack = backStack,
                entryDecorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator(),
                ),
                entryProvider = entryProvider,
            )
        }
        val visibleDestinations = if (selectedDestination == startDestination) {
            listOf(startDestination)
        } else {
            listOf(startDestination, selectedDestination)
        }
        return visibleDestinations.flatMap { entriesByDestination.getValue(it) }
    }
}

@Composable
internal fun rememberBottomBarNavigationState(
    startDestination: BottomBarDestination,
    destinations: List<BottomBarDestination>,
    configuration: SavedStateConfiguration,
): BottomBarNavigationState {
    val selectedDestinationId = rememberSaveable {
        mutableStateOf(startDestination.id)
    }
    val backStacks = destinations.associateWith { destination ->
        rememberNavBackStack(
            configuration = configuration,
            destination,
        )
    }
    return remember(startDestination, destinations, backStacks) {
        BottomBarNavigationState(
            startDestination = startDestination,
            selectedDestinationId = selectedDestinationId,
            backStacks = backStacks,
        )
    }
}

private val BottomBarDestination.id: String
    get() = when (this) {
        BottomBarDestination.Suggestions -> "suggestions"
        BottomBarDestination.Apps -> "apps"
        BottomBarDestination.Community -> "community"
        BottomBarDestination.GmsInsight -> "gms_insight"
        BottomBarDestination.Experimental -> "experimental"
    }

private fun String.toBottomBarDestination(): BottomBarDestination = when (this) {
    "apps" -> BottomBarDestination.Apps
    "community" -> BottomBarDestination.Community
    "gms_insight" -> BottomBarDestination.GmsInsight
    "experimental" -> BottomBarDestination.Experimental
    else -> BottomBarDestination.Suggestions
}

