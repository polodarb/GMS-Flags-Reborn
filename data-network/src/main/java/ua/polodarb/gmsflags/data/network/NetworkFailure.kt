package ua.polodarb.gmsflags.data.network

import java.io.IOException

enum class NetworkFailureReason {
    NetworkUnavailable,
    Timeout,
    Http,
    InvalidResponse,
    Unknown,
}

class NetworkFailure(
    val reason: NetworkFailureReason,
    val statusCode: Int? = null,
    cause: Throwable? = null,
) : IOException(null, cause)
