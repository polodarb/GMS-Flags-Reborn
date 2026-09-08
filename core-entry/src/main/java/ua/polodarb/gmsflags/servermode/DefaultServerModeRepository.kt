package ua.polodarb.gmsflags.servermode

import kotlinx.coroutines.flow.StateFlow
import ua.polodarb.gmsflags.data.repository.servermode.ServerModeRepository
import ua.polodarb.gmsflags.domain.servermode.ServerMode
import ua.polodarb.gmsflags.remoteconfig.StickyRemoteValue

class DefaultServerModeRepository(
    private val sticky: StickyRemoteValue<ServerMode>,
) : ServerModeRepository {
    override val mode: StateFlow<ServerMode> = sticky.value

    override val hasCachedValue: Boolean
        get() = sticky.hasCachedValue

    override suspend fun refresh() = sticky.refresh()
}
