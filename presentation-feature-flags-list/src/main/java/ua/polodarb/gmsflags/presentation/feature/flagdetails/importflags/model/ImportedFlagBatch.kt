package ua.polodarb.gmsflags.presentation.feature.flagdetails.importflags.model

import ua.polodarb.gmsflags.domain.flags.FlagOverride
import ua.polodarb.gmsflags.domain.flags.FlagType

data class ImportedFlagKey(
    val type: FlagType,
    val name: String,
)

data class ImportedFlag(
    val name: String,
    val type: FlagType,
    val value: String,
    val packageName: String? = null,
) {
    val key: ImportedFlagKey get() = ImportedFlagKey(type, name)

    fun toOverride() = FlagOverride(
        name = name,
        type = type,
        value = value,
    )
}

data class ImportedFlagBatch(
    val phenotypePackageName: String,
    val flags: List<ImportedFlag>,
    val skippedFlags: Int = 0,
)

data class FlagImportDocument(
    val displayName: String,
    val content: String,
)
