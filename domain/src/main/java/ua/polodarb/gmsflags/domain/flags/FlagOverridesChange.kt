package ua.polodarb.gmsflags.domain.flags

sealed interface FlagOverridesChange {
    val androidPackageName: String
    val phenotypePackageName: String

    data class Applied(
        override val androidPackageName: String,
        override val phenotypePackageName: String,
        val overrides: List<FlagOverride>,
    ) : FlagOverridesChange

    data class Removed(
        override val androidPackageName: String,
        override val phenotypePackageName: String,
        val flagNames: Set<String>,
    ) : FlagOverridesChange

    data class PackageCleared(
        override val androidPackageName: String,
        override val phenotypePackageName: String,
    ) : FlagOverridesChange
}
