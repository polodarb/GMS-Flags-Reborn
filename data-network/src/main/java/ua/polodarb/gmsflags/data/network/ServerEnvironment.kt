package ua.polodarb.gmsflags.data.network

import java.net.URI

@JvmInline
value class ServerEnvironment(val baseUrl: String) {
    fun requireBaseUrl(): String {
        require(baseUrl.isNotBlank()) { "SERVER_BASE_URL is not configured" }
        return baseUrl.trimEnd('/') + "/"
    }

    fun resolvePublicUrl(value: String?): String? {
        val candidate = value?.trim()?.takeIf(String::isNotEmpty) ?: return null
        return runCatching {
            val uri = URI(candidate)
            if (uri.isAbsolute) uri.toString()
            else URI(requireBaseUrl()).resolve(uri).toString()
        }.getOrNull()
    }
}
