package ua.polodarb.gmsflags.data.phenotype.root.flags

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import ua.polodarb.gmsflags.data.phenotype.root.parcel.PhenotypeFlagPageParcel
import ua.polodarb.gmsflags.data.phenotype.root.parcel.PhenotypeFlagParcel

class PhenotypeFlagPageCollectorTest {
    @Test
    fun `collects all pages in order`() {
        val source = (0 until 9).map(::flag)
        val collector = PhenotypeFlagPageCollector(pageSize = 4)

        val result = collector.readAll { offset, limit ->
            val end = minOf(offset + limit, source.size)
            PhenotypeFlagPageParcel(
                offset = offset,
                totalCount = source.size,
                nextOffset = if (end < source.size) end else PhenotypeFlagPageParcel.END_OF_LIST,
                flags = source.subList(offset, end),
            )
        }

        assertEquals(source, result)
    }

    @Test
    fun `rejects a page that does not advance`() {
        val collector = PhenotypeFlagPageCollector(pageSize = 4)

        assertThrows(IllegalStateException::class.java) {
            collector.readAll { offset, _ ->
                PhenotypeFlagPageParcel(
                    offset = offset,
                    totalCount = 2,
                    nextOffset = offset,
                    flags = listOf(flag(0)),
                )
            }
        }
    }

    @Test
    fun `rejects a total count that changes between pages`() {
        val collector = PhenotypeFlagPageCollector(pageSize = 1)

        assertThrows(IllegalStateException::class.java) {
            collector.readAll { offset, _ ->
                PhenotypeFlagPageParcel(
                    offset = offset,
                    totalCount = if (offset == 0) 2 else 3,
                    nextOffset = if (offset == 0) 1 else PhenotypeFlagPageParcel.END_OF_LIST,
                    flags = listOf(flag(offset)),
                )
            }
        }
    }

    private fun flag(index: Int) = PhenotypeFlagParcel(
        name = "flag_$index",
        type = 0,
        originalValue = "0",
        value = "0",
        overridden = false,
    )
}
