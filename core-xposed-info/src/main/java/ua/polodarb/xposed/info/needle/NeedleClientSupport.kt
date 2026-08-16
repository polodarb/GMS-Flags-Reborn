package ua.polodarb.xposed.info.needle

import java.util.Base64

object NeedleClientSupport {
    fun isSupported(payloadBase64: String): Boolean = runCatching {
        supports(decode(payloadBase64))
    }.getOrDefault(false)

    fun requiresNewerEngine(payloadBase64: String): Boolean = runCatching {
        !supports(decode(payloadBase64))
    }.getOrDefault(false)

    private fun decode(payloadBase64: String): NeedleRecipePayload = NeedleJson.decodeFromString(
        NeedleRecipePayload.serializer(),
        String(Base64.getDecoder().decode(payloadBase64), Charsets.UTF_8),
    )

    private fun supports(payload: NeedleRecipePayload): Boolean =
        payload.schemaVersion in NeedleProtocol.SUPPORTED_SCHEMA_VERSIONS &&
            payload.minimumEngineVersion <= NeedleProtocol.ENGINE_VERSION &&
            payload.requiredCapabilities.all { it in NeedleCapabilities.SUPPORTED }
}
