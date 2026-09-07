package ua.polodarb.gmsflags.servermode

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ua.polodarb.gmsflags.data.repository.servermode.ServerModeRepository
import ua.polodarb.gmsflags.domain.servermode.ServerMode

class DefaultServerModeRepository(
    private val store: ServerModeStore,
    private val fetchRawConfig: suspend () -> String?,
) : ServerModeRepository {
    private val state = MutableStateFlow(ServerModeJson.parse(store.read()))

    override val mode: StateFlow<ServerMode> = state.asStateFlow()

    override val hasCachedValue: Boolean
        get() = store.read() != null

    override suspend fun refresh() {
        val raw = runCatching { fetchRawConfig() }.getOrNull() ?: return
        store.write(raw)
        state.value = ServerModeJson.parse(raw)
    }
}
