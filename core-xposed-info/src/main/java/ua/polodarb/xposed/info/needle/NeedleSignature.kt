package ua.polodarb.xposed.info.needle

import java.security.KeyFactory
import java.security.PublicKey
import java.security.Signature
import java.security.spec.X509EncodedKeySpec
import java.util.Base64
import java.util.concurrent.ConcurrentHashMap

/**
 * Verifies (never creates) Needle recipe signatures on-device. Mirrors
 * backend-gms-insight's NeedleSignature.kt exactly (same algorithm, same domain separator) so a
 * recipe signed offline for the backend verifies identically here.
 */
object NeedleSignature {
    private val publicKeyCache = ConcurrentHashMap<String, PublicKey>()

    private fun publicKey(publicKeyBase64: String): PublicKey =
        publicKeyCache.computeIfAbsent(publicKeyBase64) {
            KeyFactory.getInstance("EC")
                .generatePublic(X509EncodedKeySpec(Base64.getDecoder().decode(it)))
        }

    fun verify(
        payload: ByteArray,
        signatureBase64: String,
        publicKeyBase64: String,
        domainSeparator: String = NeedleProtocol.DOMAIN_SEPARATOR,
    ): Boolean = runCatching {
        val signatureBytes = Base64.getDecoder().decode(signatureBase64)
        val verifier = Signature.getInstance("SHA256withECDSA")
        verifier.initVerify(publicKey(publicKeyBase64))
        verifier.update(domainSeparator.toByteArray(Charsets.UTF_8))
        verifier.update(payload)
        verifier.verify(signatureBytes)
    }.getOrDefault(false)

    fun verifiedSchemaVersion(
        payload: ByteArray,
        signatureBase64: String,
        publicKeyBase64: String,
    ): Int? = NeedleProtocol.SUPPORTED_SCHEMA_VERSIONS.firstOrNull { schemaVersion ->
        val separator = NeedleProtocol.domainSeparatorFor(schemaVersion) ?: return@firstOrNull false
        verify(payload, signatureBase64, publicKeyBase64, separator)
    }
}
