package ua.polodarb.gmsflags.data.repository.servermode

import kotlinx.coroutines.flow.StateFlow
import ua.polodarb.gmsflags.domain.servermode.ServerMode

interface ServerModeRepository {
    val mode: StateFlow<ServerMode>

    val hasCachedValue: Boolean

    suspend fun refresh()
}
