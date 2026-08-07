package ua.polodarb.xposed.hook.strategy.deviceconfig

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class DeviceConfigFlagReflectionTest {
    @Test
    fun `finds the unique raw string reader`() {
        assertNotNull(
            DeviceConfigFlagReflection.findRawReaderMethod(ValidManager::class.java)
        )
    }

    @Test
    fun `rejects ambiguous raw readers`() {
        assertNull(
            DeviceConfigFlagReflection.findRawReaderMethod(AmbiguousReaderManager::class.java)
        )
    }

    @Test
    fun `reads one immutable namespace and rejects several`() {
        assertEquals(
            "device_personalization_services",
            DeviceConfigFlagReflection.readNamespace(
                ValidManager("device_personalization_services")
            ),
        )
        assertNull(
            DeviceConfigFlagReflection.readNamespace(
                AmbiguousNamespaceManager("first", "second")
            )
        )
    }

    @Suppress("UNUSED_PARAMETER")
    private class ValidManager(private val namespace: String) {
        fun read(name: String): String? = null
    }

    @Suppress("UNUSED_PARAMETER")
    private class AmbiguousReaderManager(private val namespace: String) {
        fun first(name: String): String? = null
        fun second(name: String): String? = null
    }

    private class AmbiguousNamespaceManager(
        private val firstNamespace: String,
        private val secondNamespace: String,
    )
}
