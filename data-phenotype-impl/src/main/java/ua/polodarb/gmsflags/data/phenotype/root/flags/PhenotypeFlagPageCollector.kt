package ua.polodarb.gmsflags.data.phenotype.root.flags

import ua.polodarb.gmsflags.data.phenotype.root.parcel.PhenotypeFlagPageParcel
import ua.polodarb.gmsflags.data.phenotype.root.parcel.PhenotypeFlagParcel

internal class PhenotypeFlagPageCollector(
    private val pageSize: Int = DEFAULT_PAGE_SIZE,
) {
    init {
        require(pageSize in 1..MAX_PAGE_SIZE) { "Invalid flag page size: $pageSize" }
    }

    fun readAll(
        readPage: (offset: Int, limit: Int) -> PhenotypeFlagPageParcel,
    ): List<PhenotypeFlagParcel> {
        val result = mutableListOf<PhenotypeFlagParcel>()
        var offset = 0
        var expectedTotal: Int? = null

        repeat(MAX_PAGE_COUNT) {
            val page = readPage(offset, pageSize)
            validate(page, offset, expectedTotal)
            if (expectedTotal == null) expectedTotal = page.totalCount
            result += page.flags

            if (page.nextOffset == PhenotypeFlagPageParcel.END_OF_LIST) {
                check(result.size == page.totalCount) {
                    "Incomplete flag result: received=${result.size}, total=${page.totalCount}"
                }
                return result
            }
            offset = page.nextOffset
        }

        error("Flag paging exceeded $MAX_PAGE_COUNT pages")
    }

    private fun validate(
        page: PhenotypeFlagPageParcel,
        expectedOffset: Int,
        expectedTotal: Int?,
    ) {
        check(page.offset == expectedOffset) {
            "Unexpected flag page offset: expected=$expectedOffset, actual=${page.offset}"
        }
        check(page.totalCount >= 0) { "Negative flag count: ${page.totalCount}" }
        check(expectedTotal == null || page.totalCount == expectedTotal) {
            "Flag count changed while paging: expected=$expectedTotal, actual=${page.totalCount}"
        }
        check(page.flags.size <= pageSize) {
            "Flag page exceeds requested limit: ${page.flags.size} > $pageSize"
        }
        check(page.nextOffset == PhenotypeFlagPageParcel.END_OF_LIST || page.flags.isNotEmpty()) {
            "Empty intermediate flag page at offset $expectedOffset"
        }
        check(
            page.nextOffset == PhenotypeFlagPageParcel.END_OF_LIST ||
                page.nextOffset == expectedOffset + page.flags.size
        ) {
            "Invalid next flag offset: ${page.nextOffset}"
        }
    }

    private companion object {
        const val DEFAULT_PAGE_SIZE = 128
        const val MAX_PAGE_SIZE = 512
        const val MAX_PAGE_COUNT = 1_024
    }
}
