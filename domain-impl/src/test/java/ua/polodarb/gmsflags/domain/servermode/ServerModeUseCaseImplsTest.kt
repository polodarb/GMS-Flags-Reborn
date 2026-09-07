package ua.polodarb.gmsflags.domain.servermode

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import ua.polodarb.gmsflags.data.repository.servermode.ServerModeRepository

class ServerModeUseCaseImplsTest {
    @Test
    fun `observe exposes the repository state`() {
        val repository = FakeServerModeRepository(
            ServerMode(offline = true, notice = OfflineNotice("t", "s", "b")),
        )

        val mode = ObserveServerModeUseCase(repository)().value

        assertEquals(ServerMode(offline = true, notice = OfflineNotice("t", "s", "b")), mode)
    }

    @Test
    fun `refresh delegates to the repository`() = runBlocking {
        val repository = FakeServerModeRepository(ServerMode.Online)

        RefreshServerModeUseCase(repository)()

        assertTrue(repository.refreshed)
    }

    private class FakeServerModeRepository(initial: ServerMode) : ServerModeRepository {
        var refreshed = false
        override val mode: StateFlow<ServerMode> = MutableStateFlow(initial)
        override val hasCachedValue = false
        override suspend fun refresh() {
            refreshed = true
        }
    }
}
