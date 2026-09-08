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
    private val onFetchFailure: (FetchFailure) -> Unit,
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
            onFetchFailure(FetchFailure.Thrown(error))
            return
        }
        when (outcome) {
            is RemoteFetch.Failed -> onFetchFailure(FetchFailure.Reported)
            is RemoteFetch.Fetched -> {
                store.write(key, outcome.raw)
                state.value = parse(outcome.raw)
            }
        }
    }
}
