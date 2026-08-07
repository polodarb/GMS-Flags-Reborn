package ua.polodarb.gmsflags.data.network.impl.core

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import ua.polodarb.gmsflags.data.network.APP_SIGNATURE_SHA256_HEADER
import ua.polodarb.gmsflags.data.network.ServerEnvironment

internal object HttpClientFactory {
    fun create(
        environment: ServerEnvironment,
        engine: HttpClientEngine? = null,
        appSignatureSha256: String? = null,
    ): HttpClient {
        val configure: io.ktor.client.HttpClientConfig<*>.() -> Unit = {
            expectSuccess = false

            install(HttpTimeout) {
                requestTimeoutMillis = REQUEST_TIMEOUT_MILLIS
                connectTimeoutMillis = CONNECT_TIMEOUT_MILLIS
                socketTimeoutMillis = SOCKET_TIMEOUT_MILLIS
            }

            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        explicitNulls = false
                    }
                )
            }

            defaultRequest {
                url(environment.requireBaseUrl())
                contentType(ContentType.Application.Json)
                headers.append(HttpHeaders.Accept, ContentType.Application.Json.toString())
                if (!appSignatureSha256.isNullOrBlank()) {
                    headers.append(APP_SIGNATURE_SHA256_HEADER, appSignatureSha256)
                }
            }
        }

        return if (engine == null) HttpClient(configure) else HttpClient(engine, configure)
    }
}

private const val REQUEST_TIMEOUT_MILLIS = 20_000L
private const val CONNECT_TIMEOUT_MILLIS = 10_000L
private const val SOCKET_TIMEOUT_MILLIS = 20_000L
