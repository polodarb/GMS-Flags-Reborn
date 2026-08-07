package ua.polodarb.xposed.info

import java.security.KeyFactory
import java.security.MessageDigest
import java.security.spec.X509EncodedKeySpec
import java.util.Base64
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NeedleTrustAnchorTest {

    @Test
    fun `shared Needle trust anchor is a valid EC public key`() {
        val encoded = BuildConfig.NEEDLE_TRUSTED_PUBLIC_KEY_BASE64
        val decoded = Base64.getDecoder().decode(encoded)

        assertTrue("Needle trust anchor must not be blank", encoded.isNotBlank())
        val publicKey = KeyFactory.getInstance("EC").generatePublic(
            X509EncodedKeySpec(decoded),
        )
        assertEquals("EC", publicKey.algorithm)
        assertEquals(
            "9bf64c4f205db13e0a4ace2a061df238fced560e69b59093b540d90eceacd818",
            MessageDigest.getInstance("SHA-256")
                .digest(decoded)
                .joinToString(separator = "") { byte -> "%02x".format(byte) },
        )
    }
}
