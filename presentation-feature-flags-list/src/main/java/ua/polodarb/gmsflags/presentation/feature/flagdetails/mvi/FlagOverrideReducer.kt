package ua.polodarb.gmsflags.presentation.feature.flagdetails.mvi

import ua.polodarb.gmsflags.domain.flags.FlagOverride
import ua.polodarb.gmsflags.domain.flags.FlagType
import ua.polodarb.gmsflags.domain.flags.PhenotypeFlag

internal fun FlagDetailsState.withOverride(override: FlagOverride): FlagDetailsState {
    var updatedExistingFlag = false
    val updatedFlags = buildList {
        flags.forEach { flag ->
            when {
                flag.name == override.name && flag.type == override.type -> {
                    add(flag.copy(value = override.value, overridden = true))
                    updatedExistingFlag = true
                }
                flag.name == override.name && flag.originalValue == null -> Unit
                else -> add(flag)
            }
        }
    }
    if (updatedExistingFlag) return copy(flags = updatedFlags)

    return copy(
        flags = updatedFlags
            .plus(
                PhenotypeFlag(
                    name = override.name,
                    type = override.type,
                    originalValue = null,
                    value = override.value,
                    overridden = true,
                )
            )
            .sortedWith(compareBy(PhenotypeFlag::type).thenBy(PhenotypeFlag::name)),
    )
}

internal fun FlagDetailsState.withOverrides(overrides: List<FlagOverride>): FlagDetailsState {
    if (overrides.isEmpty()) return this

    val working = flags.toMutableList<PhenotypeFlag?>()
    val indexByNameAndType = HashMap<Pair<String, FlagType>, Int>(working.size * 2)
    val indicesByName = HashMap<String, MutableList<Int>>(working.size * 2)
    flags.forEachIndexed { index, flag ->
        indexByNameAndType[flag.name to flag.type] = index
        indicesByName.getOrPut(flag.name) { mutableListOf() }.add(index)
    }

    val appended = mutableListOf<PhenotypeFlag>()
    for (override in overrides) {
        val existingIndex = indexByNameAndType[override.name to override.type]
        val existingFlag = existingIndex?.let { working[it] }
        if (existingFlag != null) {
            working[existingIndex] = existingFlag.copy(value = override.value, overridden = true)
            continue
        }

        indicesByName[override.name]?.forEach { index ->
            val flag = working[index]
            if (flag != null && flag.originalValue == null) {
                working[index] = null
            }
        }
        appended += PhenotypeFlag(
            name = override.name,
            type = override.type,
            originalValue = null,
            value = override.value,
            overridden = true,
        )
    }

    val updatedFlags = working.filterNotNull()
    if (appended.isEmpty()) return copy(flags = updatedFlags)

    return copy(
        flags = (updatedFlags + appended)
            .sortedWith(compareBy(PhenotypeFlag::type).thenBy(PhenotypeFlag::name)),
    )
}

internal fun FlagDetailsState.withoutOverride(name: String): FlagDetailsState = copy(
    flags = flags.mapNotNull { flag ->
        if (flag.name != name) {
            flag
        } else {
            flag.originalValue?.let { original ->
                flag.copy(value = original, overridden = false)
            }
        }
    },
)

internal fun FlagDetailsState.withoutOverrides(names: Collection<String>): FlagDetailsState {
    if (names.isEmpty()) return this
    val nameSet = names.toHashSet()
    return copy(
        flags = flags.mapNotNull { flag ->
            if (flag.name !in nameSet) {
                flag
            } else {
                flag.originalValue?.let { original ->
                    flag.copy(value = original, overridden = false)
                }
            }
        },
    )
}

internal fun FlagDetailsState.withoutAllOverrides(): FlagDetailsState = copy(
    dialog = null,
    inlineEditor = null,
    selectedFlags = emptySet(),
    flags = flags.mapNotNull { flag ->
        flag.originalValue?.let { original ->
            flag.copy(value = original, overridden = false)
        }
    },
)

internal fun FlagDetailsState.withBooleanOverride(
    name: String,
    enabled: Boolean,
): Pair<FlagDetailsState, FlagOverride> {
    val override = FlagOverride(
        name = name,
        type = FlagType.Boolean,
        value = if (enabled) "1" else "0",
    )
    return withOverride(override) to override
}
