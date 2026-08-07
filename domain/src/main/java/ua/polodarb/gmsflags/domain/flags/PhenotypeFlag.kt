package ua.polodarb.gmsflags.domain.flags

data class PhenotypeFlag(
    val name: String,
    val type: FlagType,
    val originalValue: String?,
    val value: String,
    val overridden: Boolean,
)

data class FlagOverride(
    val name: String,
    val type: FlagType,
    val value: String,
)
