package ua.polodarb.gmsflags.domain.backup

import java.io.StringReader
import java.util.Base64
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.xml.sax.InputSource
import ua.polodarb.gmsflags.domain.flags.FlagOverride
import ua.polodarb.gmsflags.domain.flags.FlagOverrideLimits
import ua.polodarb.gmsflags.domain.flags.FlagType

class XmlFlagsBackupCodec : FlagsBackupCodec {
    override fun isBackup(xml: String): Boolean = document(xml).tagName == "gms-flags-backup"

    override fun encode(backup: FlagsBackup): String {
        val needsEncoding = backup.packages.any { pkg ->
            pkg.flags.any { it.type == FlagType.String && !it.value.isXmlText() }
        }
        val version = if (needsEncoding) "2" else "1"
        val xml = buildString {
            appendLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
            appendLine("<gms-flags-backup version=\"$version\">")
            backup.packages
                .sortedWith(
                    compareBy(BackupPackage::androidPackageName)
                        .thenBy(BackupPackage::phenotypePackageName)
                )
                .forEach { pkg ->
                    appendLine(
                        "  <package name=\"${pkg.phenotypePackageName.escaped()}\" androidPackage=\"${pkg.androidPackageName.escaped()}\">"
                    )
                    appendLine("    <flags>")
                    pkg.flags
                        .sortedBy { it.name }
                        .forEach { flag ->
                            val encoded = flag.type == FlagType.String && !flag.value.isXmlText()
                            val value = if (encoded) flag.value.base64CodeUnits() else flag.value.escaped()
                            val encoding = if (encoded) " valueEncoding=\"base64-utf16be\"" else ""
                            appendLine(
                                "      <flag name=\"${flag.name.escaped()}\" type=\"${flag.type.name.lowercase()}\" value=\"$value\"$encoding />"
                            )
                        }
                    appendLine("    </flags>")
                    appendLine("  </package>")
                }
            appendLine("</gms-flags-backup>")
        }
        decode(xml)
        return xml
    }

    override fun decode(xml: String): FlagsBackup {
        val root = document(xml)
        require(root.tagName == "gms-flags-backup") { "This file is not a flags backup" }
        val version = root.getAttribute("version")
        require(version in setOf("1", "2")) { "Unsupported backup version" }
        val identities = mutableSetOf<Triple<String, String, String>>()
        val packages = linkedMapOf<Pair<String, String>, MutableList<FlagOverride>>()
        root.children().forEach { pkg ->
            require(pkg.tagName == "package") { "Unexpected backup element" }
            val app = pkg.required("androidPackage")
            require(ANDROID_PACKAGE.matches(app)) { "Invalid Android package" }
            val name = pkg.required("name")
            val containers = pkg.children()
            require(containers.size == 1 && containers.single().tagName == "flags") {
                "Missing flags element"
            }
            val flags = packages.getOrPut(app to name) { mutableListOf() }
            containers.single().children().forEach { element ->
                require(element.tagName == "flag" && element.children().isEmpty()) {
                    "Unexpected flag element"
                }
                val flagName = element.required("name")
                require(identities.add(Triple(app, name, flagName))) { "Duplicate flag identity" }
                val type = FlagType.entries.firstOrNull { it.name.equals(element.required("type"), true) }
                    ?: error("Unsupported flag type")
                require(element.hasAttribute("value")) { "Missing flag value" }
                val rawValue = element.getAttribute("value")
                val value = if (element.hasAttribute("valueEncoding")) {
                    require(
                        version == "2" &&
                            type == FlagType.String &&
                            element.getAttribute("valueEncoding") == "base64-utf16be"
                    ) {
                        "Unsupported value encoding"
                    }
                    rawValue.decodeCodeUnits()
                } else rawValue
                when (type) {
                    FlagType.Boolean ->
                        require(value in listOf("0", "1", "true", "false")) { "Invalid boolean" }
                    FlagType.Integer ->
                        require(value.toLongOrNull() != null || value.toULongOrNull() != null) {
                            "Invalid integer"
                        }
                    FlagType.Float -> require(value.toDoubleOrNull() != null) { "Invalid float" }
                    FlagType.String -> Unit
                }
                require(
                    FlagOverrideLimits.estimatedParcelBytes(flagName, value) <=
                        FlagOverrideLimits.MAX_PARCEL_BYTES
                ) {
                    "Flag is too large"
                }
                flags +=
                    FlagOverride(
                        flagName,
                        type,
                        if (type == FlagType.Boolean) {
                            if (value == "1" || value == "true") {
                                "1"
                            } else {
                                "0"
                            }
                        } else {
                            value
                        },
                    )
            }
            require(flags.isNotEmpty()) { "Package contains no flags" }
        }
        return FlagsBackup(
            packages.map { (key, flags) -> BackupPackage(key.first, key.second, flags) }
        )
    }

    private fun document(xml: String): Element {
        require(
            xml.isNotBlank() &&
                xml.length <= MAX_DOCUMENT_CHARS &&
                xml.toByteArray(Charsets.UTF_8).size <= MAX_DOCUMENT_CHARS
        ) {
            "Backup is empty or too large"
        }
        require(!Regex("<!\\s*(DOCTYPE|ENTITY)", RegexOption.IGNORE_CASE).containsMatchIn(xml)) {
            "DTD and entities are not supported"
        }
        return DocumentBuilderFactory.newInstance()
            .apply {
                isNamespaceAware = false
                isExpandEntityReferences = false
            }
            .newDocumentBuilder()
            .parse(InputSource(StringReader(xml)))
            .documentElement
    }

    private fun Element.required(name: String): String = getAttribute(name).also {
        require(it.isNotBlank() && it == it.trim()) { "Missing or invalid $name" }
    }

    private fun Element.children(): List<Element> = buildList {
        for (i in 0 until childNodes.length) {
            val child = childNodes.item(i)
            if (child is Element) {
                add(child)
            } else {
                require(child.nodeType != Node.TEXT_NODE || child.textContent.isBlank()) {
                    "Unexpected text"
                }
            }
        }
    }

    private fun String.escaped(): String = buildString {
        this@escaped.codePoints().forEach { c ->
            require(c.isXmlCodePoint()) { "Value contains characters unsupported by XML" }
            when (c) {
                38 -> append("&amp;")
                34 -> append("&quot;")
                60 -> append("&lt;")
                62 -> append("&gt;")
                9,
                10,
                13 -> append("&#$c;")
                else -> append(String(Character.toChars(c)))
            }
        }
    }

    private fun String.isXmlText(): Boolean = codePoints().allMatch { it.isXmlCodePoint() }

    private fun Int.isXmlCodePoint(): Boolean = this == 9 ||
        this == 10 ||
        this == 13 ||
        this in 0x20..0xD7FF ||
        this in 0xE000..0xFFFD ||
        this in 0x10000..0x10FFFF

    private fun String.base64CodeUnits(): String {
        val bytes = ByteArray(length * 2)
        forEachIndexed { index, char ->
            bytes[index * 2] = (char.code ushr 8).toByte()
            bytes[index * 2 + 1] = char.code.toByte()
        }
        return Base64.getEncoder().encodeToString(bytes)
    }

    private fun String.decodeCodeUnits(): String {
        require(length <= FlagOverrideLimits.MAX_PARCEL_BYTES * 4 / 3 + 4) { "Flag is too large" }
        val bytes = Base64.getDecoder().decode(this)
        require(bytes.size % 2 == 0) { "Invalid UTF-16 code units" }
        return String(
            CharArray(bytes.size / 2) { index ->
                (((bytes[index * 2].toInt() and 0xff) shl 8) or
                        (bytes[index * 2 + 1].toInt() and 0xff))
                    .toChar()
            }
        )
    }

    companion object {
        const val MAX_DOCUMENT_CHARS = 64 * 1024 * 1024
        private val ANDROID_PACKAGE = Regex("[A-Za-z0-9_]+(?:\\.[A-Za-z0-9_]+)+")
    }
}
