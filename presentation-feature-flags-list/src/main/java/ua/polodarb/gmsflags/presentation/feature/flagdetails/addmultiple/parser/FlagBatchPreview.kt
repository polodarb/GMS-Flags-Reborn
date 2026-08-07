package ua.polodarb.gmsflags.presentation.feature.flagdetails.addmultiple.parser

import ua.polodarb.gmsflags.domain.flags.FlagOverride

internal data class FlagBatchPreview(
    val overrides: List<FlagOverride> = emptyList(),
    val invalidTokenCount: Int = 0,
) {
    val canSave: Boolean
        get() = overrides.isNotEmpty() && invalidTokenCount == 0
}
