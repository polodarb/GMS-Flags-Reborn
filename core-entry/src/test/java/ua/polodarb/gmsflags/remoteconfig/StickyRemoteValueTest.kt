package ua.polodarb.gmsflags.remoteconfig

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class StickyRemoteValueTest {
    @Test
    fun `cached value seeds the state before any refresh`() {
        val sticky = StickyRemoteValue(
            key = "flag",
            store = FakeStore(cached = "true"),
            fetch = { fail("must not fetch") },
            parse = { it == "true" },
        )

        assertTrue(sticky.value.value)
        assertTrue(sticky.hasCachedValue)
    }

    @Test
    fun `no cached value seeds the state via the parser default`() {
        val sticky = StickyRemoteValue(
            key = "flag",
            store = FakeStore(cached = null),
            fetch = { fail("must not fetch") },
            parse = { it == "true" },
        )

        assertFalse(sticky.value.value)
        assertFalse(sticky.hasCachedValue)
    }

    @Test
    fun `a successfully fetched empty value replaces the cache`() = runTest {
        val store = FakeStore(cached = "true")
        val sticky = StickyRemoteValue(
            key = "flag",
            store = store,
            fetch = { RemoteFetch.Fetched("") },
            parse = { it == "true" },
        )

        sticky.refresh()

        assertFalse(sticky.value.value)
        assertEquals("", store.written)
        assertTrue(sticky.hasCachedValue)
    }

    @Test
    fun `a failed fetch keeps the cache`() = runTest {
        val store = FakeStore(cached = "true")
        val sticky = StickyRemoteValue(
            key = "flag",
            store = store,
            fetch = { RemoteFetch.Failed },
            parse = { it == "true" },
        )

        sticky.refresh()

        assertTrue(sticky.value.value)
        assertEquals(null, store.written)
    }

    @Test
    fun `a throwing fetch keeps the cache`() = runTest {
        val store = FakeStore(cached = "true")
        val sticky = StickyRemoteValue(
            key = "flag",
            store = store,
            fetch = { throw IOException("no network") },
            parse = { it == "true" },
        )

        sticky.refresh()

        assertTrue(sticky.value.value)
        assertEquals(null, store.written)
    }

    @Test
    fun `hasCachedValue reflects the store after a refresh`() = runTest {
        val store = FakeStore(cached = null)
        val sticky = StickyRemoteValue(
            key = "flag",
            store = store,
            fetch = { RemoteFetch.Fetched("true") },
            parse = { it == "true" },
        )

        sticky.refresh()

        assertTrue(sticky.hasCachedValue)
        assertTrue(sticky.value.value)
    }

    private fun fail(message: String): Nothing = throw AssertionError(message)

    private class FakeStore(private val cached: String?) : RemoteFlagStore {
        var written: String? = null
        override fun read(key: String): String? = written ?: cached
        override fun write(key: String, raw: String) {
            written = raw
        }
    }
}
