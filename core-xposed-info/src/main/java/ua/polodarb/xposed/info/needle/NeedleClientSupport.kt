package ua.polodarb.xposed.info.needle

import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.int
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.util.Base64

object NeedleClientSupport {
    fun isSupported(payloadBase64: String): Boolean =
        runCatching { supports(payloadBase64) }.getOrDefault(false)

    fun requiresNewerEngine(payloadBase64: String): Boolean =
        runCatching { !supports(payloadBase64) }.getOrDefault(false)

    private fun supports(payloadBase64: String): Boolean {
        val json = NeedleJson
            .parseToJsonElement(String(Base64.getDecoder().decode(payloadBase64), Charsets.UTF_8))
            .jsonObject
        val schemaVersion = json["schema_version"]?.jsonPrimitive?.int ?: return false
        val minimumEngineVersion = json["minimum_engine_version"]?.jsonPrimitive?.intOrNull ?: 1
        val requiredCapabilities = json["required_capabilities"]?.jsonArray
            ?.mapNotNull { it.jsonPrimitive.contentOrNull }
            ?: emptyList()
        return schemaVersion in NeedleProtocol.SUPPORTED_SCHEMA_VERSIONS &&
            minimumEngineVersion <= NeedleProtocol.ENGINE_VERSION &&
            requiredCapabilities.all { it in NeedleCapabilities.SUPPORTED }
    }
}
