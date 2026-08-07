package ua.polodarb.xposed.info.needle

import java.security.KeyFactory
import java.security.Signature
import java.security.spec.X509EncodedKeySpec
import java.util.Base64

/**
 * Verifies (never creates) Needle recipe signatures on-device. Mirrors
 * backend-gms-insight's NeedleSignature.kt exactly (same algorithm, same domain separator) so a
 * recipe signed offline for the backend verifies identically here.
 */
object NeedleSignature {
    fun verify(payload: ByteArray, signatureBase64: String, publicKeyBase64: String): Boolean = runCatching {
        val signatureBytes = Base64.getDecoder().decode(signatureBase64)
        val keySpec = X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyBase64))
        val publicKey = KeyFactory.getInstance("EC").generatePublic(keySpec)
        val verifier = Signature.getInstance("SHA256withECDSA")
        verifier.initVerify(publicKey)
        verifier.update(NeedleProtocol.DOMAIN_SEPARATOR.toByteArray(Charsets.UTF_8))
        verifier.update(payload)
        verifier.verify(signatureBytes)
    }.getOrDefault(false)
}
