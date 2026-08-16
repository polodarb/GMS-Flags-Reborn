package ua.polodarb.xposed.info.needle

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Base64

class NeedleClientSupportTest {

    private fun b64(json: String): String = Base64.getEncoder().encodeToString(json.toByteArray())

    @Test
    fun `a v1 recipe is supported`() {
        val payload = b64("""{"schema_version":1}""")
        assertTrue(NeedleClientSupport.isSupported(payload))
        assertFalse(NeedleClientSupport.requiresNewerEngine(payload))
    }

    @Test
    fun `an image-overlay recipe within this engine is supported`() {
        val payload = b64(
            """{"schema_version":2,"minimum_engine_version":3,"required_capabilities":["VIEW_IMAGE_OVERLAY"]}""",
        )
        assertTrue(NeedleClientSupport.isSupported(payload))
        assertFalse(NeedleClientSupport.requiresNewerEngine(payload))
    }

    @Test
    fun `a recipe with unknown selector and effect enums but a higher engine version needs a newer engine`() {
        // The gate fields must be read from raw JSON: a typed decode would throw on the unknown enums
        // BEFORE ever seeing minimum_engine_version, collapsing app-update-required into a parse error.
        val payload = b64(
            """{"schema_version":2,"minimum_engine_version":99,"required_capabilities":[],""" +
                """"selector":{"type":"SOME_FUTURE_SELECTOR"},"effect":{"kind":"SOME_FUTURE_EFFECT"}}""",
        )
        assertFalse(NeedleClientSupport.isSupported(payload))
        assertTrue(NeedleClientSupport.requiresNewerEngine(payload))
    }

    @Test
    fun `a recipe requiring an unknown capability needs a newer engine`() {
        val payload = b64(
            """{"schema_version":2,"minimum_engine_version":3,"required_capabilities":["FUTURE_CAP"]}""",
        )
        assertFalse(NeedleClientSupport.isSupported(payload))
        assertTrue(NeedleClientSupport.requiresNewerEngine(payload))
    }

    @Test
    fun `a recipe with an unsupported schema version needs a newer engine`() {
        val payload = b64("""{"schema_version":4,"minimum_engine_version":1}""")
        assertFalse(NeedleClientSupport.isSupported(payload))
        assertTrue(NeedleClientSupport.requiresNewerEngine(payload))
    }
}
