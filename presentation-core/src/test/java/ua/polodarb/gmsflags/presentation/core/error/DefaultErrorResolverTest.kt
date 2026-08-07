package ua.polodarb.gmsflags.presentation.core.error

import org.junit.Assert.assertSame
import org.junit.Test
import ua.polodarb.gmsflags.domain.error.AppError

class DefaultErrorResolverTest {
    private val resolver = DefaultErrorResolver()

    @Test
    fun `maps domain errors without exposing implementation details`() {
        assertSame(UiError.NetworkUnavailable, resolver.resolve(AppError.NetworkUnavailable))
        assertSame(UiError.Timeout, resolver.resolve(AppError.Timeout))
        assertSame(UiError.NotFound, resolver.resolve(AppError.NotFound))
        assertSame(UiError.Server, resolver.resolve(AppError.Server))
        assertSame(UiError.InvalidResponse, resolver.resolve(AppError.InvalidResponse))
        assertSame(UiError.RootUnavailable, resolver.resolve(AppError.RootUnavailable))
        assertSame(
            UiError.RootServiceUnavailable,
            resolver.resolve(AppError.RootServiceUnavailable()),
        )
        assertSame(
            UiError.SystemDataUnavailable,
            resolver.resolve(AppError.SystemDataUnavailable()),
        )
    }

    @Test
    fun `maps unexpected failures to generic error`() {
        assertSame(UiError.Generic, resolver.resolve(IllegalStateException()))
    }
}
