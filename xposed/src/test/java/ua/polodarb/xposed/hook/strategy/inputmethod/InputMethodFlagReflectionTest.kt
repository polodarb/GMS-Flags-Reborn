package ua.polodarb.xposed.hook.strategy.inputmethod

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class InputMethodFlagReflectionTest {
    @Test
    fun `finds the unique non-toString name accessor`() {
        val valueGetter = ValidFlag::class.java.getDeclaredMethod("value")

        assertEquals(
            "name",
            InputMethodFlagReflection.findNameAccessor(valueGetter)?.name,
        )
    }

    @Test
    fun `rejects ambiguous string accessors`() {
        val valueGetter = AmbiguousFlag::class.java.getDeclaredMethod("value")

        assertNull(InputMethodFlagReflection.findNameAccessor(valueGetter))
    }

    @Test
    fun `rejects a class without a name accessor`() {
        val valueGetter = MissingNameFlag::class.java.getDeclaredMethod("value")

        assertNull(InputMethodFlagReflection.findNameAccessor(valueGetter))
    }

    private class ValidFlag {
        fun value(): Any = true
        fun name(): String = "flag"
        override fun toString(): String = "ignored"
    }

    private class AmbiguousFlag {
        fun value(): Any = true
        fun name(): String = "flag"
        fun alias(): String = "alias"
    }

    private class MissingNameFlag {
        fun value(): Any = true
    }
}
