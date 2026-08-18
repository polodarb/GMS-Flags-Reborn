package ua.polodarb.gmsflags.presentation.feature.flagdetails.importflags.parser

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import ua.polodarb.gmsflags.domain.flags.FlagType

class GmsFlagsFileParserTest {
    private val parser = GmsFlagsFileParser()

    @Test
    fun `parses supported types and normalizes persisted values`() {
        val result = parser.parse(
            """
            <?xml version="1.0" encoding="UTF-8"?>
            <package name="com.google.test">
                <flags>
                    <flag name="enabled" type="boolean" value="true" />
                    <flag name="count" type="integer" value="42" />
                    <flag name="ratio" type="float" value="1.5" />
                    <flag name="label" type="string" value="hello &amp; world" />
                    <flag name="empty" type="string" value="" />
                </flags>
            </package>
            """.trimIndent()
        )

        assertEquals("com.google.test", result.phenotypePackageName)
        assertEquals(
            listOf(
                Triple("enabled", FlagType.Boolean, "1"),
                Triple("count", FlagType.Integer, "42"),
                Triple("ratio", FlagType.Float, "1.5"),
                Triple("label", FlagType.String, "hello & world"),
                Triple("empty", FlagType.String, ""),
            ),
            result.flags.map { Triple(it.name, it.type, it.value) },
        )
    }

    @Test
    fun `keeps latest duplicate and reports unsupported types`() {
        val result = parser.parse(
            """
            <package name="com.google.test">
                <flags>
                    <flag name="enabled" type="boolean" value="false" />
                    <flag name="legacy" type="extVal" value="ignored" />
                    <flag name="enabled" type="boolean" value="true" />
                </flags>
            </package>
            """.trimIndent()
        )

        assertEquals(1, result.flags.size)
        assertEquals("1", result.flags.single().value)
        assertEquals(1, result.skippedFlags)
    }

    @Test
    fun `skips empty or invalid numeric values but keeps empty strings`() {
        val result = parser.parse(
            """
            <package name="com.google.test">
                <flags>
                    <flag name="45820951" type="integer" value="" />
                    <flag name="bad_int" type="integer" value="abc" />
                    <flag name="bad_float" type="float" value="" />
                    <flag name="ok_int" type="integer" value="7" />
                    <flag name="empty_string" type="string" value="" />
                </flags>
            </package>
            """.trimIndent()
        )

        assertEquals(
            listOf(
                Triple("ok_int", FlagType.Integer, "7"),
                Triple("empty_string", FlagType.String, ""),
            ),
            result.flags.map { Triple(it.name, it.type, it.value) },
        )
        assertEquals(3, result.skippedFlags)
    }

    @Test
    fun `rejects document type declarations`() {
        assertThrows(IllegalArgumentException::class.java) {
            parser.parse(
                """
                <!DOCTYPE package [<!ENTITY value "unsafe">]>
                <package name="com.google.test"><flags /></package>
                """.trimIndent()
            )
        }
    }

    @Test
    fun `reads an optional per-flag package attribute`() {
        val result = parser.parse(
            """
            <package name="com.google.test">
                <flags>
                    <flag name="same_package" type="boolean" value="true" />
                    <flag name="other_package" type="boolean" value="false" package="com.google.other" />
                </flags>
            </package>
            """.trimIndent()
        )

        assertEquals(null, result.flags.first { it.name == "same_package" }.packageName)
        assertEquals(
            "com.google.other",
            result.flags.first { it.name == "other_package" }.packageName,
        )
    }
}
