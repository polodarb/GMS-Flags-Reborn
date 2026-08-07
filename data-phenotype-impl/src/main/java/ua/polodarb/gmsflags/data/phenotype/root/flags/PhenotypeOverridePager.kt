package ua.polodarb.gmsflags.data.phenotype.root.flags

import ua.polodarb.gmsflags.data.phenotype.root.parcel.PhenotypeFlagParcel

internal class PhenotypeOverridePager(
    private val maxItemsPerPage: Int = DEFAULT_MAX_ITEMS,
    private val maxEstimatedBytesPerPage: Int = DEFAULT_MAX_BYTES,
) {
    init {
        require(maxItemsPerPage > 0)
        require(maxEstimatedBytesPerPage > 0)
    }

    fun pages(overrides: List<PhenotypeFlagParcel>): List<List<PhenotypeFlagParcel>> {
        if (overrides.isEmpty()) return emptyList()
        val pages = mutableListOf<List<PhenotypeFlagParcel>>()
        var current = mutableListOf<PhenotypeFlagParcel>()
        var currentBytes = 0

        overrides.forEach { override ->
            val itemBytes = override.estimatedParcelBytes()
            require(itemBytes <= maxEstimatedBytesPerPage) {
                "A single flag override is too large for Binder"
            }
            if (
                current.isNotEmpty() &&
                (current.size == maxItemsPerPage ||
                    currentBytes + itemBytes > maxEstimatedBytesPerPage)
            ) {
                pages += current
                current = mutableListOf()
                currentBytes = 0
            }
            current += override
            currentBytes += itemBytes
        }
        if (current.isNotEmpty()) pages += current
        return pages
    }

    private fun PhenotypeFlagParcel.estimatedParcelBytes(): Int = PARCEL_OVERHEAD_BYTES +
        (name.length + value.length + (originalValue?.length ?: 0)) * BYTES_PER_CHAR

    private companion object {
        const val DEFAULT_MAX_ITEMS = 4096
        const val DEFAULT_MAX_BYTES = 256 * 1024
        const val PARCEL_OVERHEAD_BYTES = 128
        const val BYTES_PER_CHAR = 2
    }
}
