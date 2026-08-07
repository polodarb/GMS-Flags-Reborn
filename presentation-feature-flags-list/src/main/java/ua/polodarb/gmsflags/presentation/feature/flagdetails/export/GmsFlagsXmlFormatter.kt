package ua.polodarb.gmsflags.presentation.feature.flagdetails.export

import ua.polodarb.gmsflags.domain.flags.FlagType
import ua.polodarb.gmsflags.domain.flags.PhenotypeFlag
import ua.polodarb.gmsflags.presentation.feature.flagdetails.mvi.isEnabledBoolean

internal object GmsFlagsXmlFormatter {
    fun format(packageName: String, flags: List<PhenotypeFlag>): String = buildString {
        appendLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
        appendLine("<!-- GMS Flags -->")
        appendLine("<package name=\"${packageName.xmlEscaped()}\">")
        appendLine("  <flags>")
        flags.forEach { flag ->
            appendLine(
                "    <flag name=\"${flag.name.xmlEscaped()}\" " +
                    "type=\"${flag.type.exportName()}\" " +
                    "value=\"${flag.exportValue().xmlEscaped()}\" />"
            )
        }
        appendLine("  </flags>")
        appendLine("</package>")
    }

    private fun PhenotypeFlag.exportValue(): String = if (type == FlagType.Boolean) {
        value.isEnabledBoolean().toString()
    } else {
        value
    }

    private fun FlagType.exportName(): String = when (this) {
        FlagType.Boolean -> "boolean"
        FlagType.Integer -> "integer"
        FlagType.Float -> "float"
        FlagType.String -> "string"
    }

    private fun String.xmlEscaped(): String = replace("&", "&amp;")
        .replace("\"", "&quot;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
}
