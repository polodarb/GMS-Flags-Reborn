package ua.polodarb.gmsflags.domain.error

sealed class AppError private constructor(
    cause: Throwable? = null,
) : Exception(null, cause) {
    data object NetworkUnavailable : AppError()
    data object Timeout : AppError()
    data object Unauthorized : AppError()
    data object AccessDenied : AppError()
    data object NotFound : AppError()
    data object TooManyRequests : AppError()
    data object Server : AppError()
    data object InvalidResponse : AppError()
    data object RootUnavailable : AppError()
    class RootServiceUnavailable(cause: Throwable? = null) : AppError(cause)
    class SystemDataUnavailable(cause: Throwable? = null) : AppError(cause)
    class Unknown(cause: Throwable? = null) : AppError(cause)
}
