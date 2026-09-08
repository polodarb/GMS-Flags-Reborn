package ua.polodarb.gmsflags.servermode

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import ua.polodarb.gmsflags.domain.servermode.ServerMode
import ua.polodarb.gmsflags.remoteconfig.RemoteFetch
import ua.polodarb.gmsflags.remoteconfig.RemoteFlagStore
import ua.polodarb.gmsflags.remoteconfig.StickyRemoteValue
import java.io.IOException

private const val KEY = "offline_mode_json"

class DefaultServerModeRepositoryTest {
    @Test
    fun `cached payload seeds the state before any refresh`() {
        val repository = repositoryWith(
            store = FakeStore(cached = """{"enabled":true}"""),
            fetchRawConfig = { fail("must not fetch") },
        )

        assertTrue(repository.mode.value.offline)
        assertTrue(repository.hasCachedValue)
    }

    @Test
    fun `empty cache starts online`() {
        val repository = repositoryWith(
            store = FakeStore(cached = null),
            fetchRawConfig = { fail("must not fetch") },
        )

        assertEquals(ServerMode.Online, repository.mode.value)
        assertFalse(repository.hasCachedValue)
    }

    @Test
    fun `refresh persists the fetched payload and updates the state`() = runTest {
        val store = FakeStore(cached = null)
        val repository = repositoryWith(
            store = store,
            fetchRawConfig = {
                RemoteFetch.Fetched("""{"enabled":true,"config":{"badge":"Offline"}}""")
            },
        )

        repository.refresh()

        assertTrue(repository.mode.value.offline)
        assertEquals("Offline", repository.mode.value.notice?.badge)
        assertEquals("""{"enabled":true,"config":{"badge":"Offline"}}""", store.written)
        assertTrue(repository.hasCachedValue)
    }

    @Test
    fun `a failed fetch keeps the cached value`() = runTest {
        val store = FakeStore(cached = """{"enabled":true}""")
        val repository = repositoryWith(
            store = store,
            fetchRawConfig = { RemoteFetch.Failed },
        )

        repository.refresh()

        assertTrue(repository.mode.value.offline)
        assertEquals(null, store.written)
    }

    @Test
    fun `a throwing fetch keeps the cached value`() = runTest {
        val store = FakeStore(cached = """{"enabled":true}""")
        val repository = repositoryWith(
            store = store,
            fetchRawConfig = { throw IOException("no network") },
        )

        repository.refresh()

        assertTrue(repository.mode.value.offline)
        assertEquals(null, store.written)
    }

    @Test
    fun `a fetched empty payload returns the app to online mode`() = runTest {
        val store = FakeStore(cached = """{"enabled":true}""")
        val repository = repositoryWith(
            store = store,
            fetchRawConfig = { RemoteFetch.Fetched("") },
        )

        repository.refresh()

        assertEquals(ServerMode.Online, repository.mode.value)
        assertEquals("", store.written)
        assertTrue(repository.hasCachedValue)
    }

    @Test
    fun `a fetched empty payload on a first launch marks the value as cached`() = runTest {
        val store = FakeStore(cached = null)
        val repository = repositoryWith(
            store = store,
            fetchRawConfig = { RemoteFetch.Fetched("") },
        )

        repository.refresh()

        assertEquals(ServerMode.Online, repository.mode.value)
        assertTrue(repository.hasCachedValue)
    }

    private fun repositoryWith(
        store: RemoteFlagStore,
        fetchRawConfig: suspend () -> RemoteFetch,
    ) = DefaultServerModeRepository(
        sticky = StickyRemoteValue(
            key = KEY,
            store = store,
            fetch = fetchRawConfig,
            parse = { raw -> ServerModeJson.parse(raw) { fail("must not be called") } },
            onFetchFailure = {},
        ),
    )

    private fun fail(message: String): Nothing = throw AssertionError(message)

    private class FakeStore(private val cached: String?) : RemoteFlagStore {
        var written: String? = null
        override fun read(key: String): String? = written ?: cached
        override fun write(key: String, raw: String) {
            written = raw
        }
    }
}
