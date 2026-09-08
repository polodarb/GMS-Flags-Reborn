package ua.polodarb.gmsflags.servermode

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ua.polodarb.gmsflags.data.repository.servermode.ServerModeRepository
import ua.polodarb.gmsflags.domain.servermode.ServerMode

sealed interface ServerModeFetch {
    data class Fetched(val raw: String) : ServerModeFetch

    data object Failed : ServerModeFetch
}

class DefaultServerModeRepository(
    private val store: ServerModeStore,
    private val fetchRawConfig: suspend () -> ServerModeFetch,
) : ServerModeRepository {
    private val state = MutableStateFlow(ServerModeJson.parse(store.read()))

    override val mode: StateFlow<ServerMode> = state.asStateFlow()

    override val hasCachedValue: Boolean
        get() = store.read() != null

    override suspend fun refresh() {
        val outcome = try {
            fetchRawConfig()
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (error: Exception) {
            ServerModeFetch.Failed
        }
        val raw = (outcome as? ServerModeFetch.Fetched)?.raw ?: return
        store.write(raw)
        state.value = ServerModeJson.parse(raw)
    }
}
