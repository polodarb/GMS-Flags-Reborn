package ua.polodarb.gmsflags.data.phenotype.sqlite

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CompositePhenotypeDatabaseReaderTest {
    @Test
    fun `combines available databases and removes duplicate bindings`() {
        val binding = PhenotypePackageBindingRecord("phenotype", "android")
        val reader = CompositePhenotypeDatabaseReader(
            readers = listOf(
                PhenotypeDatabaseReader { listOf(binding) },
                PhenotypeDatabaseReader { error("Database is unavailable") },
                PhenotypeDatabaseReader {
                    listOf(binding, PhenotypePackageBindingRecord("other", "android"))
                },
            ),
        )

        assertEquals(
            listOf(binding, PhenotypePackageBindingRecord("other", "android")),
            reader.readPhenotypePackages(),
        )
    }

    @Test
    fun `fails when no database can be read`() {
        val reader = CompositePhenotypeDatabaseReader(
            readers = listOf(
                PhenotypeDatabaseReader { error("first") },
                PhenotypeDatabaseReader { error("second") },
            ),
        )

        assertTrue(runCatching(reader::readPhenotypePackages).isFailure)
    }
}
