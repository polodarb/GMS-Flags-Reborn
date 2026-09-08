package ua.polodarb.gmsflags.domain.navigation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import ua.polodarb.gmsflags.data.repository.navigation.NavigationFlagsRepository

class NavigationFlagsUseCaseImplsTest {
    @Test
    fun `observe exposes the repository state`() {
        val repository = FakeNavigationFlagsRepository(hidden = true)

        val hidden = ObserveGmsInsightHiddenUseCase(repository)().value

        assertEquals(true, hidden)
    }

    @Test
    fun `refresh delegates to the repository`() = runBlocking {
        val repository = FakeNavigationFlagsRepository(hidden = false)

        RefreshNavigationFlagsUseCase(repository)()

        assertTrue(repository.refreshed)
    }

    private class FakeNavigationFlagsRepository(hidden: Boolean) : NavigationFlagsRepository {
        var refreshed = false
        override val gmsInsightHidden: StateFlow<Boolean> = MutableStateFlow(hidden)
        override suspend fun refresh() {
            refreshed = true
        }
    }
}
