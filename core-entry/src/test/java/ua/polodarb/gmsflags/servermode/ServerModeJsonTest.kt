package ua.polodarb.gmsflags.servermode

import org.junit.Assert.assertEquals
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
            ServerModeJson.parse("""{"enabled":true}"""),
        )
    }

    @Test
    fun `blank notice fields become null`() {
        val mode = ServerModeJson.parse(
            """{"enabled":true,"config":{"title":"  ","subtitle":"","badge":"Offline"}}""",
        )

        assertEquals(OfflineNotice(null, null, "Offline"), mode.notice)
    }

    @Test
    fun `unknown fields are ignored`() {
        val mode = ServerModeJson.parse("""{"enabled":true,"whatever":42}""")

        assertEquals(true, mode.offline)
    }

    @Test
    fun `disabled blank and malformed input stay online`() {
        assertEquals(ServerMode.Online, ServerModeJson.parse("""{"enabled":false}"""))
        assertEquals(ServerMode.Online, ServerModeJson.parse(""))
        assertEquals(ServerMode.Online, ServerModeJson.parse("   "))
        assertEquals(ServerMode.Online, ServerModeJson.parse(null))
        assertEquals(ServerMode.Online, ServerModeJson.parse("not json"))
    }
}
