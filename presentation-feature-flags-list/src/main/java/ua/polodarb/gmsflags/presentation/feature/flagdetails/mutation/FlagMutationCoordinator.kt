package ua.polodarb.gmsflags.presentation.feature.flagdetails.mutation

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class FlagMutationCoordinator {
    private val operationMutex = Mutex()
    private val stateLock = Any()
    private var latestGeneration = 0L
    private var pendingFailure: Throwable? = null

    fun begin(resetFailure: Boolean): Long = synchronized(stateLock) {
        if (resetFailure) {
            pendingFailure = null
        }
        ++latestGeneration
    }

    suspend fun execute(
        generation: Long,
        operation: suspend () -> Result<Unit>,
    ): FlagMutationResult {
        val operationResult = runCatching {
            operationMutex.withLock { operation().getOrThrow() }
        }
        return synchronized(stateLock) {
            operationResult.exceptionOrNull()?.let { pendingFailure = it }
            if (generation != latestGeneration) {
                FlagMutationResult.AwaitingLatest
            } else {
                val failure = pendingFailure
                pendingFailure = null
                if (failure == null) {
                    FlagMutationResult.Success
                } else {
                    FlagMutationResult.Failure(failure)
                }
            }
        }
    }
}

internal sealed interface FlagMutationResult {
    data object AwaitingLatest : FlagMutationResult
    data object Success : FlagMutationResult
    data class Failure(val error: Throwable) : FlagMutationResult
}
