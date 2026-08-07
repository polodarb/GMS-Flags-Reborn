package ua.polodarb.gmsflags.data.repository.impl.server.mapper

import kotlinx.serialization.json.JsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import ua.polodarb.gmsflags.data.network.publicapi.model.HookCompatibilityResolveNetModel
import ua.polodarb.gmsflags.data.network.publicapi.model.InfoBlockNetModel
import ua.polodarb.gmsflags.data.network.publicapi.model.ResolveResponseNetModel
import ua.polodarb.gmsflags.data.network.publicapi.model.ResolvedFlagNetModel
import ua.polodarb.gmsflags.domain.server.content.InfoBlockType
import ua.polodarb.gmsflags.domain.server.content.RemoteFlagValueType

class ServerModelMappersTest {

    @Test
    fun `unknown server enum remains representable`() {
        val model = InfoBlockNetModel(
            id = 1,
            type = "FUTURE_TYPE",
            message = "Message",
            isActive = true,
            sortOrder = 0,
        )

        assertEquals(InfoBlockType.Unknown, model.toDomain().type)
    }

    @Test
    fun `resolved primitive value maps to storage friendly text`() {
        val model = ResolveResponseNetModel(
            packageName = "com.example",
            versionCode = 42,
            flags = listOf(
                ResolvedFlagNetModel(
                    flagName = "enabled",
                    valueType = "BOOL",
                    value = JsonPrimitive(true),
                )
            ),
            configHash = "hash",
        )

        val flag = model.toDomain().flags.single()

        assertEquals(RemoteFlagValueType.Boolean, flag.type)
        assertEquals("true", flag.value)
    }

    @Test
    fun `unsupported hook response does not invent a profile`() {
        val result = HookCompatibilityResolveNetModel(
            status = "UNSUPPORTED",
            profile = null,
        ).toDomain()

        assertFalse(result.supported)
        assertEquals(null, result.profile)
    }
}
