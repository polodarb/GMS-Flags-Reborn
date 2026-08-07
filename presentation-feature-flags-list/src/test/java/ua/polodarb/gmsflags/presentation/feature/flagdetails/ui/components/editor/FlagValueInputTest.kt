package ua.polodarb.gmsflags.presentation.feature.flagdetails.ui.components.editor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import ua.polodarb.gmsflags.domain.flags.FlagType

class FlagValueInputTest {
    @Test
    fun `integer input accepts only an optional sign and digits`() {
        assertEquals("-42", sanitizeFlagValueInput(FlagType.Integer, "-42"))
        assertEquals("", sanitizeFlagValueInput(FlagType.Integer, ""))
        assertNull(sanitizeFlagValueInput(FlagType.Integer, "4.2"))
        assertNull(sanitizeFlagValueInput(FlagType.Integer, "42a"))
    }

    @Test
    fun `float input normalizes decimal comma and rejects letters`() {
        assertEquals("-4.2", sanitizeFlagValueInput(FlagType.Float, "-4,2"))
        assertEquals("4.", sanitizeFlagValueInput(FlagType.Float, "4."))
        assertNull(sanitizeFlagValueInput(FlagType.Float, "4.2f"))
    }

    @Test
    fun `string input is preserved`() {
        assertEquals("any value", sanitizeFlagValueInput(FlagType.String, "any value"))
    }
}
