package ua.polodarb.xposed.info.needle

import java.util.Base64

object NeedleClientSupport {
    fun isSupported(payloadBase64: String): Boolean = runCatching {
        val bytes = Base64.getDecoder().decode(payloadBase64)
        val payload = NeedleJson.decodeFromString(
            NeedleRecipePayload.serializer(),
            String(bytes, Charsets.UTF_8),
        )
        payload.schemaVersion in NeedleProtocol.SUPPORTED_SCHEMA_VERSIONS &&
            payload.minimumEngineVersion <= NeedleProtocol.ENGINE_VERSION &&
            payload.requiredCapabilities.all { it in NeedleCapabilities.SUPPORTED }
    }.getOrDefault(false)
}
