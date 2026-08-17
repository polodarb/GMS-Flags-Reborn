package ua.polodarb.xposed.hook.strategy.mendel

import java.lang.reflect.Method
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MendelFlagReflectionTest {
    @Test
    fun `resolves the config map type shared by the typed reader cluster`() {
        assertEquals(
            ConfigMap::class.java,
            MendelFlagReflection.findConfigMapType(ValidReaders::class.java),
        )
    }

    @Test
    fun `rejects a class without the full typed reader cluster`() {
        assertNull(MendelFlagReflection.findConfigMapType(PartialReaders::class.java))
    }

    @Test
    fun `collects typed leaf readers of the resolved config map type`() {
        val readers = MendelFlagReflection.findLeafReaders(
            ValidReaders::class.java,
            ConfigMap::class.java,
        )

        assertEquals(
            setOf("readBoolean", "readLong", "readDouble", "readString", "readStringWithoutDefault"),
            readers.map(Method::getName).toSet(),
        )
    }

    private class ConfigMap

    private class OtherMap

    private class ValidReaders {
        companion object {
            @JvmStatic
            fun readBoolean(map: ConfigMap, id: Long, fallback: Boolean): Boolean = fallback

            @JvmStatic
            fun readLong(map: ConfigMap, id: Long, fallback: Long): Long = fallback

            @JvmStatic
            fun readDouble(map: ConfigMap, id: Long, fallback: Double): Double = fallback

            @JvmStatic
            fun readString(map: ConfigMap, id: Long, fallback: String): String = fallback

            @JvmStatic
            fun readStringWithoutDefault(map: ConfigMap, id: Long): String = ""

            @JvmStatic
            fun readUnrelated(map: OtherMap, id: Long, fallback: Boolean): Boolean = fallback

            @JvmStatic
            fun readUnsupportedType(map: ConfigMap, id: Long, fallback: Int): Int = fallback
        }
    }

    private class PartialReaders {
        companion object {
            @JvmStatic
            fun readBoolean(map: ConfigMap, id: Long, fallback: Boolean): Boolean = fallback

            @JvmStatic
            fun readLong(map: ConfigMap, id: Long, fallback: Long): Long = fallback
        }
    }
}
