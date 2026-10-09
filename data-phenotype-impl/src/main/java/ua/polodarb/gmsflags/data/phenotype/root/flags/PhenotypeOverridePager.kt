package ua.polodarb.gmsflags.data.phenotype.root.flags

import ua.polodarb.gmsflags.data.phenotype.root.parcel.PhenotypeFlagParcel
import ua.polodarb.gmsflags.domain.flags.FlagOverrideLimits

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
        var currentBytes = 0L

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
                currentBytes = 0L
            }
            current += override
            currentBytes += itemBytes
        }
        if (current.isNotEmpty()) pages += current
        return pages
    }

    private fun PhenotypeFlagParcel.estimatedParcelBytes(): Long =
        FlagOverrideLimits.estimatedParcelBytes(name, value, originalValue)

    private companion object {
        const val DEFAULT_MAX_ITEMS = 4096
        const val DEFAULT_MAX_BYTES = FlagOverrideLimits.MAX_PARCEL_BYTES
    }
}
