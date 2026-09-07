package ua.polodarb.gmsflags.navigation

import org.junit.Assert.assertEquals
import org.junit.Test
import ua.polodarb.gmsflags.navigation.model.BottomBarNavigation
import ua.polodarb.gmsflags.navigation.state.resolveSelectedDestination
import ua.polodarb.gmsflags.presentation.core.navigation.BottomBarDestination

class BottomBarNavigationTest {
    @Test
    fun `offline navigation keeps only the apps destination`() {
        val destinations = BottomBarNavigation.items(offline = true).map { it.destination }

        assertEquals(listOf(BottomBarDestination.Apps), destinations)
    }

    @Test
    fun `online navigation keeps suggestions apps and insight`() {
        val destinations = BottomBarNavigation.items(offline = false).map { it.destination }

        assertEquals(
            listOf(
                BottomBarDestination.Suggestions,
                BottomBarDestination.Apps,
                BottomBarDestination.GmsInsight,
            ),
            destinations.take(3),
        )
    }

    @Test
    fun `a saved destination that is gone falls back to the start destination`() {
        val resolved = resolveSelectedDestination(
            savedId = "suggestions",
            destinations = listOf(BottomBarDestination.Apps),
            startDestination = BottomBarDestination.Apps,
        )

        assertEquals(BottomBarDestination.Apps, resolved)
    }

    @Test
    fun `a saved destination that still exists is kept`() {
        val resolved = resolveSelectedDestination(
            savedId = "gms_insight",
            destinations = listOf(BottomBarDestination.Apps, BottomBarDestination.GmsInsight),
            startDestination = BottomBarDestination.Apps,
        )

        assertEquals(BottomBarDestination.GmsInsight, resolved)
    }
}
