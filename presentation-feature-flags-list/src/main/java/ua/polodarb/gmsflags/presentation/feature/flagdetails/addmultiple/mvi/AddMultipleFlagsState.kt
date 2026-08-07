package ua.polodarb.gmsflags.presentation.feature.flagdetails.addmultiple.mvi

import ua.polodarb.gmsflags.domain.flags.FlagType
import ua.polodarb.gmsflags.domain.flags.FlagOverride
import ua.polodarb.gmsflags.presentation.core.mvi.ViewState

data class AddMultipleFlagsState(
    val androidPackageName: String,
    val phenotypePackageName: String,
    val selectedType: FlagType = FlagType.Boolean,
    val inputs: Map<FlagType, String> = FlagType.entries.associateWith { "" },
    val booleanValue: Boolean = true,
    val previewFlags: List<FlagOverride> = emptyList(),
    val invalidTokenCount: Int = 0,
    val saving: Boolean = false,
) : ViewState {
    val canSave: Boolean
        get() = previewFlags.isNotEmpty() && invalidTokenCount == 0 && !saving
}
