package ua.polodarb.gmsflags.data.network.impl.core

import io.ktor.client.HttpClient
import io.ktor.client.call.NoTransformationFoundException
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.JsonConvertException
import io.ktor.util.network.UnresolvedAddressException
import java.io.IOException
import java.net.SocketTimeoutException as JavaSocketTimeoutException
import java.net.UnknownHostException
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.serialization.SerializationException
import ua.polodarb.gmsflags.data.network.NetworkFailure
import ua.polodarb.gmsflags.data.network.NetworkFailureReason

internal suspend inline fun <reified T> HttpClient.safeApiCall(
    noinline request: suspend HttpClient.() -> HttpResponse,
): T = safeApiCall(
    request = request,
    transform = { response -> response.body<T>() },
)

internal suspend fun <T> HttpClient.safeApiCall(
    acceptedStatus: (HttpStatusCode) -> Boolean = { it.value in SUCCESS_STATUS_RANGE },
    request: suspend HttpClient.() -> HttpResponse,
    transform: suspend (HttpResponse) -> T,
): T = try {
    val response = request()
    if (!acceptedStatus(response.status)) {
        throw NetworkFailure(
            reason = NetworkFailureReason.Http,
            statusCode = response.status.value,
        )
    }
    transform(response)
} catch (cancellation: CancellationException) {
    throw cancellation
} catch (failure: NetworkFailure) {
    throw failure
} catch (timeout: HttpRequestTimeoutException) {
    throw timeout.asNetworkFailure(NetworkFailureReason.Timeout)
} catch (timeout: ConnectTimeoutException) {
    throw timeout.asNetworkFailure(NetworkFailureReason.Timeout)
} catch (timeout: JavaSocketTimeoutException) {
    throw timeout.asNetworkFailure(NetworkFailureReason.Timeout)
} catch (unavailable: UnresolvedAddressException) {
    throw unavailable.asNetworkFailure(NetworkFailureReason.NetworkUnavailable)
} catch (unavailable: UnknownHostException) {
    throw unavailable.asNetworkFailure(NetworkFailureReason.NetworkUnavailable)
} catch (invalid: JsonConvertException) {
    throw invalid.asNetworkFailure(NetworkFailureReason.InvalidResponse)
} catch (invalid: SerializationException) {
    throw invalid.asNetworkFailure(NetworkFailureReason.InvalidResponse)
} catch (invalid: NoTransformationFoundException) {
    throw invalid.asNetworkFailure(NetworkFailureReason.InvalidResponse)
} catch (unavailable: IOException) {
    throw unavailable.asNetworkFailure(NetworkFailureReason.NetworkUnavailable)
} catch (unexpected: Throwable) {
    throw unexpected.asNetworkFailure(NetworkFailureReason.Unknown)
}

private fun Throwable.asNetworkFailure(reason: NetworkFailureReason) = NetworkFailure(
    reason = reason,
    cause = this,
)

private val SUCCESS_STATUS_RANGE = 200..299
