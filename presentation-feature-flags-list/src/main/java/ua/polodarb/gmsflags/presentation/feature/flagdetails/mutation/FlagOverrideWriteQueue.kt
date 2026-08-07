package ua.polodarb.gmsflags.presentation.feature.flagdetails.mutation

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ua.polodarb.gmsflags.domain.flags.FlagOverride
import ua.polodarb.gmsflags.domain.flags.FlagType

internal class FlagOverrideWriteQueue(
    private val scope: CoroutineScope,
    private val writer: suspend (List<FlagOverride>) -> Result<Unit>,
    private val onFailure: (Throwable) -> Unit,
    private val onApplied: (List<FlagOverride>) -> Unit = {},
    private val debounceDelayMillis: Long = DEFAULT_DEBOUNCE_DELAY_MILLIS,
) {
    private val pending = linkedMapOf<FlagKey, FlagOverride>()
    private var debounceJob: Job? = null
    private var writerJob: Job? = null
    private var flushRequested = false

    fun submit(overrides: List<FlagOverride>) {
        overrides.forEach { override ->
            pending[FlagKey(override.type, override.name)] = override
        }
        debounceJob?.cancel()
        debounceJob = scope.launch {
            delay(debounceDelayMillis)
            requestFlush()
        }
    }

    suspend fun awaitIdle() {
        while (true) {
            val currentDebounce = debounceJob
            val currentWriter = writerJob
            currentDebounce?.join()
            currentWriter?.join()
            if (
                debounceJob?.isActive != true &&
                writerJob?.isActive != true &&
                pending.isEmpty()
            ) {
                return
            }
        }
    }

    private fun requestFlush() {
        if (writerJob?.isActive == true) {
            flushRequested = true
            return
        }
        if (pending.isEmpty()) return

        val batch = pending.values.toList()
        pending.clear()
        val job = scope.launch(start = CoroutineStart.LAZY) {
            val result = writer(batch)
            writerJob = null
            result.onFailure { error ->
                pending.clear()
                debounceJob?.cancel()
                flushRequested = false
                onFailure(error)
                return@launch
            }
            onApplied(batch)
            if (flushRequested) {
                flushRequested = false
                requestFlush()
            }
        }
        writerJob = job
        job.start()
    }

    private data class FlagKey(
        val type: FlagType,
        val name: String,
    )

    private companion object {
        const val DEFAULT_DEBOUNCE_DELAY_MILLIS = 600L
    }
}
