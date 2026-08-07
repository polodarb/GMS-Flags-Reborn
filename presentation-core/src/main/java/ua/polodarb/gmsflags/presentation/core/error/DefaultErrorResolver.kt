package ua.polodarb.gmsflags.presentation.core.error

import ua.polodarb.gmsflags.domain.error.AppError

class DefaultErrorResolver : ErrorResolver {
    override fun resolve(error: Throwable): UiError = when (error) {
        AppError.NetworkUnavailable -> UiError.NetworkUnavailable
        AppError.Timeout -> UiError.Timeout
        AppError.Unauthorized -> UiError.Unauthorized
        AppError.AccessDenied -> UiError.AccessDenied
        AppError.NotFound -> UiError.NotFound
        AppError.TooManyRequests -> UiError.TooManyRequests
        AppError.Server -> UiError.Server
        AppError.InvalidResponse -> UiError.InvalidResponse
        AppError.RootUnavailable -> UiError.RootUnavailable
        is AppError.RootServiceUnavailable -> UiError.RootServiceUnavailable
        is AppError.SystemDataUnavailable -> UiError.SystemDataUnavailable
        is AppError.Unknown -> UiError.Generic
        else -> UiError.Generic
    }
}
