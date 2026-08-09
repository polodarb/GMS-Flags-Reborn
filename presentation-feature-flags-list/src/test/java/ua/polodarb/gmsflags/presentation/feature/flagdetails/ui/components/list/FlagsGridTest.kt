package ua.polodarb.gmsflags.presentation.feature.flagdetails.ui.components.list

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import ua.polodarb.gmsflags.domain.flags.FlagType
import ua.polodarb.gmsflags.presentation.feature.flagdetails.mvi.InlineFlagEditor
import ua.polodarb.gmsflags.presentation.feature.flagdetails.mvi.SelectedFlag

class FlagsGridTest {
    @Test
    fun `long press starts drag selection when no inline editor is open`() {
        assertTrue(allowsDragSelection(target = STRING_FLAG, openEditor = null))
    }

    @Test
    fun `long press on the flag being edited is left to the text field`() {
        val editor = InlineFlagEditor(name = STRING_FLAG.name, type = STRING_FLAG.type, value = "")

        assertFalse(allowsDragSelection(target = STRING_FLAG, openEditor = editor))
    }

    @Test
    fun `long press on another flag still starts drag selection while editing`() {
        val editor = InlineFlagEditor(name = STRING_FLAG.name, type = STRING_FLAG.type, value = "")
        val otherFlag = SelectedFlag(FlagType.String, "other_flag")

        assertTrue(allowsDragSelection(target = otherFlag, openEditor = editor))
    }

    @Test
    fun `flags sharing a name across types are told apart`() {
        val editor = InlineFlagEditor(name = STRING_FLAG.name, type = FlagType.Integer, value = "")

        assertTrue(allowsDragSelection(target = STRING_FLAG, openEditor = editor))
    }

    private companion object {
        val STRING_FLAG = SelectedFlag(FlagType.String, "empty_string_flag")
    }
}
