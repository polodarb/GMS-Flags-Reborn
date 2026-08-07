package ua.polodarb.gmsflags.presentation.feature.flagdetails.mvi

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import ua.polodarb.gmsflags.domain.flags.FlagType
import ua.polodarb.gmsflags.domain.flags.PhenotypeFlag

class FlagDetailsReducerTest {
    @Test
    fun `back exits selection before navigating`() {
        val selected = SelectedFlag(FlagType.Boolean, "flag")
        val selectionState = state().copy(selectedFlags = setOf(selected))

        val exitSelection = FlagDetailsReducer.reduce(
            selectionState,
            FlagDetailsEvent.BackClicked,
        )!!
        val navigate = FlagDetailsReducer.reduce(state(), FlagDetailsEvent.BackClicked)!!

        assertTrue(exitSelection.state.selectedFlags.isEmpty())
        assertEquals(null, exitSelection.effect)
        assertEquals(FlagDetailsEffect.NavigateBack, navigate.effect)
    }

    @Test
    fun `switching from boolean resets an incompatible filter`() {
        val reduction = FlagDetailsReducer.reduce(
            state().copy(filter = FlagFilter.Enabled),
            FlagDetailsEvent.TypeSelected(FlagType.String),
        )!!

        assertEquals(FlagType.String, reduction.state.selectedType)
        assertEquals(FlagFilter.All, reduction.state.filter)
    }

    @Test
    fun `search and filters are mutually exclusive`() {
        val search = FlagDetailsReducer.reduce(state(), FlagDetailsEvent.SearchToggled)!!.state
        val filters = FlagDetailsReducer.reduce(
            search.copy(query = "camera"),
            FlagDetailsEvent.FiltersToggled,
        )!!.state

        assertTrue(search.searchVisible)
        assertFalse(filters.searchVisible)
        assertTrue(filters.filtersVisible)
        assertEquals("", filters.query)
    }

    @Test
    fun `launch action targets the android application package`() {
        val initial = state().copy(androidPackageName = "com.google.android.keep")

        val reduction = FlagDetailsReducer.reduce(
            initial,
            FlagDetailsEvent.LaunchApplicationClicked,
        )!!

        assertEquals(
            FlagDetailsEffect.LaunchApplication("com.google.android.keep"),
            reduction.effect,
        )
    }

    @Test
    fun `non boolean flag expands inline editor outside selection mode`() {
        val flag = PhenotypeFlag(
            name = "sample",
            type = FlagType.String,
            originalValue = "old",
            value = "new",
            overridden = true,
        )
        val reduction = FlagDetailsReducer.reduce(
            state().copy(selectedType = FlagType.String, flags = listOf(flag)),
            FlagDetailsEvent.FlagClicked(flag.name),
        )!!

        val editor = requireNotNull(reduction.state.inlineEditor)
        assertEquals(flag.name, editor.name)
        assertEquals(flag.type, editor.type)
        assertEquals(flag.value, editor.value)
        assertEquals(null, reduction.state.dialog)
    }

    @Test
    fun `clicking expanded flag collapses inline editor`() {
        val flag = PhenotypeFlag(
            name = "sample",
            type = FlagType.Integer,
            originalValue = "1",
            value = "2",
            overridden = true,
        )
        val initial = state().copy(
            selectedType = flag.type,
            flags = listOf(flag),
            inlineEditor = InlineFlagEditor(flag.name, flag.type, flag.value),
        )

        val reduction = FlagDetailsReducer.reduce(
            initial,
            FlagDetailsEvent.FlagClicked(flag.name),
        )!!

        assertEquals(null, reduction.state.inlineEditor)
    }

    @Test
    fun `inline editor value is stored in screen state`() {
        val initial = state().copy(
            inlineEditor = InlineFlagEditor("sample", FlagType.Float, "1.0"),
        )

        val reduction = FlagDetailsReducer.reduce(
            initial,
            FlagDetailsEvent.InlineEditorValueChanged("2.5"),
        )!!

        assertEquals("2.5", reduction.state.inlineEditor?.value)
    }

    @Test
    fun `changing editor type normalizes its value`() {
        val editor = FlagDetailsDialog.Editor(
            originalName = null,
            name = "sample",
            type = FlagType.Integer,
            value = "",
        )

        val reduction = FlagDetailsReducer.reduce(
            state().copy(dialog = editor),
            FlagDetailsEvent.EditorTypeChanged(FlagType.Boolean),
        )!!

        val updatedEditor = reduction.state.dialog as FlagDetailsDialog.Editor
        assertEquals(FlagType.Boolean, updatedEditor.type)
        assertEquals("0", updatedEditor.value)
    }

    private fun state() = FlagDetailsState(
        androidPackageName = "com.google.android.test",
        phenotypePackageName = "com.google.test",
        loading = false,
    )
}
