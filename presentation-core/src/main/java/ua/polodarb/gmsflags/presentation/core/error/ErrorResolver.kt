package ua.polodarb.gmsflags.presentation.core.error

fun interface ErrorResolver {
    fun resolve(error: Throwable): UiError
}
