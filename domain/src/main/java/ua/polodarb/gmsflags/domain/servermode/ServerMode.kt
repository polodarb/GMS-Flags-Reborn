package ua.polodarb.gmsflags.domain.servermode

data class ServerMode(
    val offline: Boolean,
    val notice: OfflineNotice?,
) {
    companion object {
        val Online = ServerMode(offline = false, notice = null)
    }
}

data class OfflineNotice(
    val title: String?,
    val subtitle: String?,
    val badge: String?,
)
