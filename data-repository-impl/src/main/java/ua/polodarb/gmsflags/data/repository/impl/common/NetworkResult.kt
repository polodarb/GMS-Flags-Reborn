package ua.polodarb.gmsflags.data.repository.impl.common

import kotlin.coroutines.cancellation.CancellationException
import ua.polodarb.gmsflags.data.network.NetworkFailure
import ua.polodarb.gmsflags.data.network.NetworkFailureReason
import ua.polodarb.gmsflags.domain.error.AppError

internal suspend fun <T> networkResult(block: suspend () -> T): Result<T> = try {
    Result.success(block())
} catch (cancellation: CancellationException) {
    throw cancellation
} catch (failure: NetworkFailure) {
    Result.failure(failure.toAppError())
} catch (error: Throwable) {
    Result.failure(AppError.Unknown(error))
}

private fun NetworkFailure.toAppError(): AppError = when (reason) {
    NetworkFailureReason.NetworkUnavailable -> AppError.NetworkUnavailable
    NetworkFailureReason.Timeout -> AppError.Timeout
    NetworkFailureReason.InvalidResponse -> AppError.InvalidResponse
    NetworkFailureReason.Unknown -> AppError.Unknown(cause)
    NetworkFailureReason.Http -> statusCode.toHttpAppError()
}

private fun Int?.toHttpAppError(): AppError = when {
    this == 401 -> AppError.Unauthorized
    this == 403 -> AppError.AccessDenied
    this == 404 -> AppError.NotFound
    this == 408 -> AppError.Timeout
    this == 429 -> AppError.TooManyRequests
    this != null && this in 500..599 -> AppError.Server
    else -> AppError.Unknown()
}
