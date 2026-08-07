package ua.polodarb.gmsflags.presentation.feature.flagdetails.importflags.parser

import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Element
import org.xml.sax.InputSource
import ua.polodarb.gmsflags.domain.flags.FlagType
import ua.polodarb.gmsflags.presentation.feature.flagdetails.importflags.model.ImportedFlag
import ua.polodarb.gmsflags.presentation.feature.flagdetails.importflags.model.ImportedFlagBatch

internal class GmsFlagsFileParser {
    fun parse(xml: String): ImportedFlagBatch {
        require(xml.isNotBlank()) { "Import document is empty" }
        require(!FORBIDDEN_XML.containsMatchIn(xml)) { "DTD and entities are not supported" }

        val document = newDocumentBuilderFactory()
            .newDocumentBuilder()
            .parse(InputSource(StringReader(xml)))
        val packageElement = document.documentElement
        require(packageElement.tagName == PACKAGE_TAG) { "Root element must be package" }

        val packageName = packageElement.requiredAttribute(NAME_ATTRIBUTE)
        val flags = linkedMapOf<Pair<FlagType, String>, ImportedFlag>()
        var skippedFlags = 0

        val nodes = packageElement.getElementsByTagName(FLAG_TAG)
        repeat(nodes.length) { index ->
            val element = nodes.item(index) as? Element ?: return@repeat
            val type = element.getAttribute(TYPE_ATTRIBUTE).toFlagType()
            if (type == null) {
                skippedFlags += 1
                return@repeat
            }
            val name = element.requiredAttribute(NAME_ATTRIBUTE)
            val value = element.valueAttribute().normalized(type, name)
            val packageName = element.getAttribute(PACKAGE_ATTRIBUTE).trim().ifEmpty { null }
            flags[type to name] = ImportedFlag(name, type, value, packageName)
        }

        require(flags.isNotEmpty()) { "Import document contains no supported flags" }
        return ImportedFlagBatch(
            phenotypePackageName = packageName,
            flags = flags.values.toList(),
            skippedFlags = skippedFlags,
        )
    }

    private fun newDocumentBuilderFactory() = DocumentBuilderFactory.newInstance().apply {
        isNamespaceAware = false
        isExpandEntityReferences = false
    }

    private fun Element.requiredAttribute(name: String): String = getAttribute(name)
        .trim()
        .also { require(it.isNotEmpty()) { "Missing $name attribute" } }

    private fun Element.valueAttribute(): String {
        require(hasAttribute(VALUE_ATTRIBUTE)) { "Missing $VALUE_ATTRIBUTE attribute" }
        return getAttribute(VALUE_ATTRIBUTE)
    }

    private fun String.toFlagType(): FlagType? = when (trim().lowercase()) {
        "boolean", "bool" -> FlagType.Boolean
        "integer", "int" -> FlagType.Integer
        "float" -> FlagType.Float
        "string" -> FlagType.String
        else -> null
    }

    private fun String.normalized(type: FlagType, flagName: String): String = when (type) {
        FlagType.Boolean -> when {
            equals("true", ignoreCase = true) || this == "1" -> "1"
            equals("false", ignoreCase = true) || this == "0" -> "0"
            else -> error("Invalid boolean value for $flagName")
        }
        FlagType.Integer -> trim().also {
            require(it.toLongOrNull() != null) { "Invalid integer value for $flagName" }
        }
        FlagType.Float -> trim().also {
            require(it.toDoubleOrNull() != null) { "Invalid float value for $flagName" }
        }
        FlagType.String -> also {
            require(length <= MAX_STRING_VALUE_CHARS) { "String value is too large" }
        }
    }

    private companion object {
        const val PACKAGE_TAG = "package"
        const val FLAG_TAG = "flag"
        const val NAME_ATTRIBUTE = "name"
        const val TYPE_ATTRIBUTE = "type"
        const val VALUE_ATTRIBUTE = "value"
        const val PACKAGE_ATTRIBUTE = "package"
        const val MAX_STRING_VALUE_CHARS = 120 * 1024
        val FORBIDDEN_XML = Regex("<!\\s*(DOCTYPE|ENTITY)", RegexOption.IGNORE_CASE)
    }
}
