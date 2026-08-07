package ua.polodarb.xposed.info.needle

import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.Signature
import java.security.spec.ECGenParameterSpec
import java.security.spec.PKCS8EncodedKeySpec
import java.util.Base64
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Test-only EC keypair + signer, mirroring the backend's TestNeedleSigner exactly - never the
 * real production key, which never touches this module. */
private object TestNeedleSigner {
    fun generateKeyPairBase64(): Pair<String, String> {
        val generator = KeyPairGenerator.getInstance("EC")
        generator.initialize(ECGenParameterSpec("secp256r1"))
        val keyPair = generator.generateKeyPair()
        return Base64.getEncoder().encodeToString(keyPair.private.encoded) to
            Base64.getEncoder().encodeToString(keyPair.public.encoded)
    }

    fun sign(payload: ByteArray, privateKeyBase64: String): String {
        val keySpec = PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyBase64))
        val privateKey = KeyFactory.getInstance("EC").generatePrivate(keySpec)
        val signer = Signature.getInstance("SHA256withECDSA")
        signer.initSign(privateKey)
        signer.update(NeedleProtocol.DOMAIN_SEPARATOR.toByteArray(Charsets.UTF_8))
        signer.update(payload)
        return Base64.getEncoder().encodeToString(signer.sign())
    }
}

class NeedleSignatureTest {

    @Test
    fun `verify accepts a signature produced over the exact payload and domain separator`() {
        val (privateKey, publicKey) = TestNeedleSigner.generateKeyPairBase64()
        val payload = """{"recipe_id":"1"}""".toByteArray(Charsets.UTF_8)
        val signature = TestNeedleSigner.sign(payload, privateKey)

        assertTrue(NeedleSignature.verify(payload, signature, publicKey))
    }

    @Test
    fun `verify rejects a tampered payload`() {
        val (privateKey, publicKey) = TestNeedleSigner.generateKeyPairBase64()
        val signature = TestNeedleSigner.sign("""{"recipe_id":"1"}""".toByteArray(Charsets.UTF_8), privateKey)

        assertFalse(NeedleSignature.verify("""{"recipe_id":"2"}""".toByteArray(Charsets.UTF_8), signature, publicKey))
    }

    @Test
    fun `verify rejects a signature from a different keypair`() {
        val (privateKeyA, _) = TestNeedleSigner.generateKeyPairBase64()
        val (_, publicKeyB) = TestNeedleSigner.generateKeyPairBase64()
        val payload = """{"recipe_id":"1"}""".toByteArray(Charsets.UTF_8)
        val signature = TestNeedleSigner.sign(payload, privateKeyA)

        assertFalse(NeedleSignature.verify(payload, signature, publicKeyB))
    }

    @Test
    fun `verify returns false instead of throwing on garbage input`() {
        assertFalse(NeedleSignature.verify(ByteArray(0), "not-base64!!", "also-not-base64!!"))
    }
}
