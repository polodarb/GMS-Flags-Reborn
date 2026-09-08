package ua.polodarb.gmsflags.remoteconfig

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class StickyRemoteValue<T>(
    private val key: String,
    private val store: RemoteFlagStore,
    private val fetch: suspend () -> RemoteFetch,
    private val parse: (String?) -> T,
) {
    private val state = MutableStateFlow(parse(store.read(key)))

    val value: StateFlow<T> = state.asStateFlow()

    val hasCachedValue: Boolean
        get() = store.read(key) != null

    suspend fun refresh() {
        val outcome = try {
            fetch()
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (error: Exception) {
            RemoteFetch.Failed
        }
        val raw = (outcome as? RemoteFetch.Fetched)?.raw ?: return
        store.write(key, raw)
        state.value = parse(raw)
    }
}
