package ua.polodarb.gmsflags.data.phenotype.sqlite

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RoutedPhenotypeFlagReaderTest {
    private val fallbackFlag = StoredPhenotypeFlag("flag", 0, "1")

    @Test
    fun `uses application database before the fallback database`() {
        val reader = RoutedPhenotypeFlagReader(
            defaultReaders = listOf(flagReader(fallbackFlag)),
            readersByApplication = mapOf(
                "com.android.vending" to listOf(
                    flagReader(StoredPhenotypeFlag("vending", 0, "1")),
                    flagReader(fallbackFlag),
                ),
            ),
        )

        assertEquals(
            listOf(StoredPhenotypeFlag("vending", 0, "1")),
            reader.readFlags("com.android.vending", "phenotype"),
        )
    }

    @Test
    fun `falls back when application database has no matching flags`() {
        val reader = RoutedPhenotypeFlagReader(
            defaultReaders = listOf(flagReader(fallbackFlag)),
            readersByApplication = mapOf(
                "com.android.vending" to listOf(
                    flagReader(),
                    flagReader(fallbackFlag),
                ),
            ),
        )

        assertEquals(
            listOf(fallbackFlag),
            reader.readFlags("com.android.vending", "phenotype"),
        )
    }

    @Test
    fun `fails when every configured database fails`() {
        val failingReader = PhenotypeFlagDatabaseReader { error("unavailable") }
        val reader = RoutedPhenotypeFlagReader(
            defaultReaders = listOf(failingReader),
            readersByApplication = emptyMap(),
        )

        assertTrue(runCatching { reader.readFlags("android", "phenotype") }.isFailure)
    }

    private fun flagReader(vararg flags: StoredPhenotypeFlag) =
        PhenotypeFlagDatabaseReader { flags.toList() }
}
