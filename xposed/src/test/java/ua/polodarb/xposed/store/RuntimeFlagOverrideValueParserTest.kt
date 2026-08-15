package ua.polodarb.xposed.store

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RuntimeFlagOverrideValueParserTest {
    @Test
    fun `keeps parsing by stored type when no declared type is supplied`() {
        assertEquals(
            true,
            RuntimeFlagOverrideValueParser.parse(null, override(TYPE_BOOLEAN, "1")),
        )
        assertEquals(
            "value",
            RuntimeFlagOverrideValueParser.parse(null, override(TYPE_STRING, "value")),
        )
    }

    @Test
    fun `applies a null string reader result when the stored type is a string`() {
        assertEquals(
            "value",
            RuntimeFlagOverrideValueParser.parse(
                original = null,
                override = override(TYPE_STRING, "value"),
                declaredType = String::class.java,
            ),
        )
    }

    @Test
    fun `rejects a mistyped override for a null string reader result`() {
        assertNull(
            RuntimeFlagOverrideValueParser.parse(
                original = null,
                override = override(TYPE_BOOLEAN, "1"),
                declaredType = String::class.java,
            )
        )
        assertNull(
            RuntimeFlagOverrideValueParser.parse(
                original = null,
                override = override(TYPE_INTEGER, "5"),
                declaredType = String::class.java,
            )
        )
    }

    @Test
    fun `rejects a mistyped override for a primitive reader`() {
        assertNull(
            RuntimeFlagOverrideValueParser.parse(
                original = false,
                override = override(TYPE_STRING, "value"),
                declaredType = Boolean::class.javaPrimitiveType,
            )
        )
    }

    @Test
    fun `applies a matching override for a primitive reader`() {
        assertEquals(
            true,
            RuntimeFlagOverrideValueParser.parse(
                original = false,
                override = override(TYPE_BOOLEAN, "1"),
                declaredType = Boolean::class.javaPrimitiveType,
            ),
        )
        assertEquals(
            7L,
            RuntimeFlagOverrideValueParser.parse(
                original = 1L,
                override = override(TYPE_INTEGER, "7"),
                declaredType = Long::class.javaPrimitiveType,
            ),
        )
    }

    private fun override(flagType: Int, value: String) =
        RuntimeFlagOverrideStore.Override(flagType = flagType, value = value)

    private companion object {
        const val TYPE_BOOLEAN = 0
        const val TYPE_INTEGER = 1
        const val TYPE_STRING = 3
    }
}
