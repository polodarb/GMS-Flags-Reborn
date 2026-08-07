package ua.polodarb.gmsflags.data.repository.impl.common

import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import ua.polodarb.gmsflags.data.network.NetworkFailure
import ua.polodarb.gmsflags.data.network.NetworkFailureReason
import ua.polodarb.gmsflags.domain.error.AppError

class NetworkResultTest {

    @Test
    fun `network failure is mapped to app error`() = runBlocking {
        val result = networkResult<Unit> {
            throw NetworkFailure(NetworkFailureReason.NetworkUnavailable)
        }

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is AppError.NetworkUnavailable)
    }

    @Test
    fun `http status is mapped to semantic app error`() = runBlocking {
        val result = networkResult<Unit> {
            throw NetworkFailure(
                reason = NetworkFailureReason.Http,
                statusCode = 429,
            )
        }

        assertTrue(result.exceptionOrNull() is AppError.TooManyRequests)
    }

    @Test
    fun `cancellation is never converted to failure`() {
        try {
            runBlocking {
                networkResult<Unit> { throw CancellationException("cancelled") }
            }
            fail("Expected cancellation")
        } catch (_: CancellationException) {
        }
    }
}
