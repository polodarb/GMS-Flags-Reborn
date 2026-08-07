package ua.polodarb.gmsflags.data.phenotype.root.flags

import ua.polodarb.gmsflags.data.phenotype.root.parcel.PhenotypeFlagPageParcel
import ua.polodarb.gmsflags.data.phenotype.root.parcel.PhenotypeFlagParcel
import ua.polodarb.gmsflags.data.phenotype.runtime.RuntimeFlagOverride
import ua.polodarb.gmsflags.data.phenotype.runtime.RuntimeFlagOverrideStore
import ua.polodarb.gmsflags.data.phenotype.sqlite.PhenotypeFlagReader
import ua.polodarb.xposed.info.phenotypePackageCandidates

internal class PhenotypeFlagStore(
    private val flagReader: PhenotypeFlagReader,
    private val overrideStore: RuntimeFlagOverrideStore,
) {
    private var cachedSnapshot: FlagSnapshot? = null

    @Synchronized
    fun readFlagsPage(
        androidPackageName: String,
        phenotypePackageName: String,
        offset: Int,
        limit: Int,
    ): PhenotypeFlagPageParcel {
        require(offset >= 0) { "Flag page offset cannot be negative" }
        require(limit in 1..MAX_PAGE_SIZE) { "Invalid flag page limit: $limit" }

        val key = FlagSnapshotKey(androidPackageName, phenotypePackageName)
        val snapshot = cachedSnapshot
            ?.takeIf { it.key == key && offset > 0 }
            ?: FlagSnapshot(key, readFlagSnapshot(androidPackageName, phenotypePackageName))
                .also { cachedSnapshot = it }
        require(offset <= snapshot.flags.size) {
            "Flag page offset $offset exceeds total ${snapshot.flags.size}"
        }

        val endIndex = minOf(offset + limit, snapshot.flags.size)
        val nextOffset = if (endIndex < snapshot.flags.size) {
            endIndex
        } else {
            PhenotypeFlagPageParcel.END_OF_LIST
        }
        val page = PhenotypeFlagPageParcel(
            offset = offset,
            totalCount = snapshot.flags.size,
            nextOffset = nextOffset,
            flags = snapshot.flags.subList(offset, endIndex).toList(),
        )
        if (nextOffset == PhenotypeFlagPageParcel.END_OF_LIST) cachedSnapshot = null
        return page
    }

    private fun readFlagSnapshot(
        androidPackageName: String,
        phenotypePackageName: String,
    ): List<PhenotypeFlagParcel> {
        val baseFlags = flagReader.readFlags(androidPackageName, phenotypePackageName)
        val overrides = linkedMapOf<Pair<Int, String>, RuntimeFlagOverride>()
        phenotypePackageCandidates(phenotypePackageName, androidPackageName).forEach { candidate ->
            overrideStore.read(androidPackageName, candidate.packageName).forEach { override ->
                overrides.putIfAbsent(override.type to override.name, override)
            }
        }

        val result = baseFlags.map { flag ->
            val override = overrides[flag.type to flag.name]
            PhenotypeFlagParcel(
                name = flag.name,
                type = flag.type,
                originalValue = flag.value,
                value = override?.value ?: flag.value,
                overridden = override != null,
            )
        }.toMutableList()

        val baseKeys = baseFlags.mapTo(mutableSetOf()) { it.type to it.name }
        result += overrides.values
            .filter { (it.type to it.name) !in baseKeys }
            .map { override ->
                PhenotypeFlagParcel(
                    name = override.name,
                    type = override.type,
                    originalValue = null,
                    value = override.value,
                    overridden = true,
                )
            }
        return result.sortedWith(compareBy(PhenotypeFlagParcel::type).thenBy(PhenotypeFlagParcel::name))
    }

    @Synchronized
    fun writeOverrides(
        androidPackageName: String,
        phenotypePackageName: String,
        overrides: List<PhenotypeFlagParcel>,
    ) {
        cachedSnapshot = null
        overrideStore.write(
            androidPackageName,
            phenotypePackageName,
            overrides.map { override ->
                RuntimeFlagOverride(
                    packageName = phenotypePackageName,
                    name = override.name,
                    type = override.type,
                    value = override.value,
                )
            },
        )
    }

    @Synchronized
    fun deleteOverride(
        androidPackageName: String,
        phenotypePackageName: String,
        flagName: String,
    ) {
        cachedSnapshot = null
        phenotypePackageCandidates(phenotypePackageName, androidPackageName).forEach { candidate ->
            overrideStore.delete(androidPackageName, candidate.packageName, flagName)
        }
    }

    @Synchronized
    fun deleteOverrides(
        androidPackageName: String,
        phenotypePackageName: String,
        flagNames: List<String>,
    ) {
        cachedSnapshot = null
        phenotypePackageCandidates(phenotypePackageName, androidPackageName).forEach { candidate ->
            overrideStore.delete(androidPackageName, candidate.packageName, flagNames)
        }
    }

    @Synchronized
    fun deletePackageOverrides(
        androidPackageName: String,
        phenotypePackageName: String,
    ) {
        cachedSnapshot = null
        phenotypePackageCandidates(phenotypePackageName, androidPackageName).forEach { candidate ->
            overrideStore.deletePackage(androidPackageName, candidate.packageName)
        }
    }

    private data class FlagSnapshotKey(
        val androidPackageName: String,
        val phenotypePackageName: String,
    )

    private data class FlagSnapshot(
        val key: FlagSnapshotKey,
        val flags: List<PhenotypeFlagParcel>,
    )

    private companion object {
        const val MAX_PAGE_SIZE = 512
    }
}
