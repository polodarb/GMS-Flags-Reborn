package ua.polodarb.gmsflags.servermode

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import ua.polodarb.gmsflags.domain.servermode.OfflineNotice
import ua.polodarb.gmsflags.domain.servermode.ServerMode

internal object ServerModeJson {
    private val json = Json { ignoreUnknownKeys = true }

    fun parse(raw: String?, onUnreadablePayload: (IllegalArgumentException) -> Unit): ServerMode {
        val trimmed = raw?.takeIf { it.isNotBlank() } ?: return ServerMode.Online
        val payload = try {
            json.decodeFromString<OfflineModePayload>(trimmed)
        } catch (error: IllegalArgumentException) {
            onUnreadablePayload(error)
            return ServerMode.Online
        }
        if (!payload.enabled) return ServerMode.Online
        val config = payload.config
        val notice = config?.let {
            OfflineNotice(
                title = it.title.normalized(),
                subtitle = it.subtitle.normalized(),
                badge = it.badge.normalized(),
            )
        }
        return ServerMode(offline = true, notice = notice)
    }

    private fun String?.normalized(): String? = this?.trim()?.takeIf { it.isNotEmpty() }

    @Serializable
    private data class OfflineModePayload(
        val enabled: Boolean = false,
        val config: OfflineModeConfigPayload? = null,
    )

    @Serializable
    private data class OfflineModeConfigPayload(
        val title: String? = null,
        val subtitle: String? = null,
        val badge: String? = null,
    )
}
