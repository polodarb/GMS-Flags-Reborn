package ua.polodarb.gmsflags.servermode

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import ua.polodarb.gmsflags.domain.servermode.OfflineNotice
import ua.polodarb.gmsflags.domain.servermode.ServerMode

class ServerModeJsonTest {
    @Test
    fun `enabled config turns the app offline and carries the notice`() {
        val mode = ServerModeJson.parse(
            """
            {"enabled":true,"config":{"title":"Offline","subtitle":"Server retired","badge":"Offline"}}
            """.trimIndent(),
            onUnreadablePayload = { fail("must not be called") },
        )

        assertEquals(
            ServerMode(offline = true, notice = OfflineNotice("Offline", "Server retired", "Offline")),
            mode,
        )
    }

    @Test
    fun `config is optional`() {
        assertEquals(
            ServerMode(offline = true, notice = null),
            ServerModeJson.parse(
                """{"enabled":true}""",
                onUnreadablePayload = { fail("must not be called") },
            ),
        )
    }

    @Test
    fun `blank notice fields become null`() {
        val mode = ServerModeJson.parse(
            """{"enabled":true,"config":{"title":"  ","subtitle":"","badge":"Offline"}}""",
            onUnreadablePayload = { fail("must not be called") },
        )

        assertEquals(OfflineNotice(null, null, "Offline"), mode.notice)
    }

    @Test
    fun `unknown fields are ignored`() {
        val mode = ServerModeJson.parse(
            """{"enabled":true,"whatever":42}""",
            onUnreadablePayload = { fail("must not be called") },
        )

        assertEquals(true, mode.offline)
    }

    @Test
    fun `disabled blank and null input stay online without reporting`() {
        assertEquals(
            ServerMode.Online,
            ServerModeJson.parse("""{"enabled":false}""", onUnreadablePayload = { fail("must not be called") }),
        )
        assertEquals(
            ServerMode.Online,
            ServerModeJson.parse("", onUnreadablePayload = { fail("must not be called") }),
        )
        assertEquals(
            ServerMode.Online,
            ServerModeJson.parse("   ", onUnreadablePayload = { fail("must not be called") }),
        )
        assertEquals(
            ServerMode.Online,
            ServerModeJson.parse(null, onUnreadablePayload = { fail("must not be called") }),
        )
    }

    @Test
    fun `unparseable payload stays online and reports the decoding error once`() {
        val errors = mutableListOf<IllegalArgumentException>()

        val mode = ServerModeJson.parse("not json") { error -> errors.add(error) }

        assertEquals(ServerMode.Online, mode)
        assertEquals(1, errors.size)
    }

    @Test
    fun `a value with the wrong type reports the decoding error`() {
        val errors = mutableListOf<IllegalArgumentException>()

        val mode = ServerModeJson.parse("""{"enabled":1}""") { error -> errors.add(error) }

        assertEquals(ServerMode.Online, mode)
        assertEquals(1, errors.size)
        assertTrue(errors.single().message.orEmpty().isNotBlank())
    }

    private fun fail(message: String): Nothing = throw AssertionError(message)
}
