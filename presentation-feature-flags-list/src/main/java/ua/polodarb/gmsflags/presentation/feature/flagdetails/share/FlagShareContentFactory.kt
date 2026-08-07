package ua.polodarb.gmsflags.presentation.feature.flagdetails.share

import ua.polodarb.gmsflags.domain.flags.PhenotypeFlag
import ua.polodarb.gmsflags.presentation.feature.flagdetails.export.GmsFlagsXmlFormatter

internal object FlagShareContentFactory {
    fun export(packageName: String, flags: List<PhenotypeFlag>): String =
        GmsFlagsXmlFormatter.format(packageName, flags)

    fun report(flags: List<PhenotypeFlag>): String = flags.joinToString("\n") {
        "${it.name}: ${it.value} (${it.type.name})"
    }
}
