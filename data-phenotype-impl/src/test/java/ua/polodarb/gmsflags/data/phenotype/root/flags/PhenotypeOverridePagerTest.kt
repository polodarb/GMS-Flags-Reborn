package ua.polodarb.gmsflags.data.phenotype.root.flags

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import ua.polodarb.gmsflags.data.phenotype.root.parcel.PhenotypeFlagParcel

class PhenotypeOverridePagerTest {
    @Test
    fun `splits overrides by item count`() {
        val pager = PhenotypeOverridePager(
            maxItemsPerPage = 2,
            maxEstimatedBytesPerPage = 10_000,
        )

        val pages = pager.pages(List(5) { flag("flag_$it", "1") })

        assertEquals(listOf(2, 2, 1), pages.map(List<*>::size))
    }

    @Test
    fun `splits overrides by estimated payload size`() {
        val pager = PhenotypeOverridePager(
            maxItemsPerPage = 100,
            maxEstimatedBytesPerPage = 400,
        )

        val pages = pager.pages(
            listOf(
                flag("first", "x".repeat(60)),
                flag("second", "x".repeat(60)),
            )
        )

        assertEquals(listOf(1, 1), pages.map(List<*>::size))
    }

    @Test
    fun `rejects a single override larger than page budget`() {
        val pager = PhenotypeOverridePager(
            maxItemsPerPage = 100,
            maxEstimatedBytesPerPage = 256,
        )

        assertThrows(IllegalArgumentException::class.java) {
            pager.pages(listOf(flag("large", "x".repeat(100))))
        }
    }

    private fun flag(name: String, value: String) = PhenotypeFlagParcel(
        name = name,
        type = 0,
        originalValue = null,
        value = value,
        overridden = true,
    )
}
