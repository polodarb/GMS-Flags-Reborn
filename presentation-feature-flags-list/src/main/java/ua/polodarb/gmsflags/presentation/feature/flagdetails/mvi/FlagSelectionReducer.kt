package ua.polodarb.gmsflags.presentation.feature.flagdetails.mvi

import ua.polodarb.gmsflags.domain.flags.FlagOverride
import ua.polodarb.gmsflags.domain.flags.FlagType
import ua.polodarb.gmsflags.domain.flags.PhenotypeFlag

internal fun FlagDetailsState.toggleSelection(name: String): FlagDetailsState {
    val selected = SelectedFlag(selectedType, name)
    return copy(
        selectedFlags = selectedFlags.toMutableSet().apply {
            if (!add(selected)) remove(selected)
        }
    )
}

internal fun FlagDetailsState.selectAllVisible(): FlagDetailsState = copy(
    selectedFlags = selectedFlags + visibleFlags().map { SelectedFlag(it.type, it.name) }
)

internal fun FlagDetailsState.withSelectedBooleanValue(
    enabled: Boolean,
): FlagSelectionMutation {
    val value = if (enabled) "1" else "0"
    val overrides = flags
        .filter { SelectedFlag(it.type, it.name) in selectedFlags && it.type == FlagType.Boolean }
        .map { FlagOverride(it.name, it.type, value) }
    val names = overrides.mapTo(mutableSetOf(), FlagOverride::name)

    return FlagSelectionMutation(
        state = copy(
            selectedFlags = emptySet(),
            flags = flags.map { flag ->
                if (flag.type == FlagType.Boolean && flag.name in names) {
                    flag.copy(value = value, overridden = true)
                } else {
                    flag
                }
            },
        ),
        overrides = overrides,
    )
}

internal fun FlagDetailsState.withoutSelectedOverrides(): FlagSelectionResetMutation {
    val affected = flags.filter {
        SelectedFlag(it.type, it.name) in selectedFlags && it.overridden
    }
    val names = affected.mapTo(mutableSetOf(), PhenotypeFlag::name)

    return FlagSelectionResetMutation(
        state = copy(
            selectedFlags = emptySet(),
            flags = flags.mapNotNull { flag ->
                if (flag.name !in names) {
                    flag
                } else {
                    flag.originalValue?.let { original -> flag.copy(value = original, overridden = false) }
                }
            },
        ),
        names = names,
    )
}

internal fun FlagDetailsState.selectedFlagValues(): List<PhenotypeFlag> = flags.filter {
    SelectedFlag(it.type, it.name) in selectedFlags
}

internal data class FlagSelectionMutation(
    val state: FlagDetailsState,
    val overrides: List<FlagOverride>,
)

internal data class FlagSelectionResetMutation(
    val state: FlagDetailsState,
    val names: Set<String>,
)
