package ua.polodarb.gmsflags.presentation.feature.flagdetails.addmultiple.parser

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import ua.polodarb.gmsflags.domain.flags.FlagType
import ua.polodarb.gmsflags.presentation.feature.flagdetails.addmultiple.mvi.AddMultipleFlagsState

class FlagBatchParserTest {
    private val parser = FlagBatchParser()

    @Test
    fun `parses and deduplicates overrides by type and name`() {
        val result = parser.parse(
            state(
                inputs = mapOf(
                    FlagType.Boolean to "enabled enabled",
                    FlagType.Integer to "count=1 count=42",
                    FlagType.Float to "ratio=0.5",
                    FlagType.String to "mode=modern",
                )
            )
        )

        assertEquals(4, result.size)
        assertEquals("1", result.first { it.type == FlagType.Boolean }.value)
        assertEquals("42", result.first { it.type == FlagType.Integer }.value)
        assertEquals("0.5", result.first { it.type == FlagType.Float }.value)
        assertEquals("modern", result.first { it.type == FlagType.String }.value)
    }

    @Test
    fun `rejects invalid numeric values`() {
        assertThrows(IllegalArgumentException::class.java) {
            parser.parse(state(inputs = mapOf(FlagType.Integer to "count=many")))
        }
    }

    @Test
    fun `preview keeps valid flags and reports invalid tokens`() {
        val result = parser.preview(
            state(inputs = mapOf(FlagType.Integer to "valid=42 broken=nope missingValue"))
        )

        assertEquals(1, result.overrides.size)
        assertEquals("valid", result.overrides.single().name)
        assertEquals(2, result.invalidTokenCount)
        assertEquals(false, result.canSave)
    }

    @Test
    fun `preview reflects selected boolean value`() {
        val result = parser.preview(
            state(inputs = mapOf(FlagType.Boolean to "first second")).copy(
                booleanValue = false,
            )
        )

        assertEquals(listOf("0", "0"), result.overrides.map { it.value })
        assertEquals(true, result.canSave)
    }

    @Test
    fun `inline boolean value overrides the selected default`() {
        val result = parser.preview(
            state(inputs = mapOf(FlagType.Boolean to "keepDefault forceOff=0 forceOn=true"))
                .copy(booleanValue = true)
        )

        assertEquals("1", result.overrides.first { it.name == "keepDefault" }.value)
        assertEquals("0", result.overrides.first { it.name == "forceOff" }.value)
        assertEquals("1", result.overrides.first { it.name == "forceOn" }.value)
        assertEquals(0, result.invalidTokenCount)
    }

    @Test
    fun `rejects invalid inline boolean value`() {
        val result = parser.preview(
            state(inputs = mapOf(FlagType.Boolean to "good bad=maybe"))
        )

        assertEquals(1, result.overrides.size)
        assertEquals("good", result.overrides.single().name)
        assertEquals(1, result.invalidTokenCount)
    }

    private fun state(inputs: Map<FlagType, String>) = AddMultipleFlagsState(
        androidPackageName = "com.google.android.test",
        phenotypePackageName = "com.google.test",
        inputs = inputs,
    )
}
