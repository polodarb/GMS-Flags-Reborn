package ua.polodarb.xposed.info.needle

import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.Signature
import java.security.spec.ECGenParameterSpec
import java.security.spec.PKCS8EncodedKeySpec
import java.util.Base64
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NeedleEffectTypeRulesTest {

    private fun assertAllowed(check: NeedleEffectTypeCheck) =
        assertEquals(NeedleEffectTypeCheck.Allowed, check)

    private fun assertRejected(check: NeedleEffectTypeCheck) =
        assertTrue("expected rejection, got $check", check is NeedleEffectTypeCheck.Rejected)

    @Test
    fun `a primitive boolean method accepts BOOLEAN_RESULT at either hook point`() {
        assertAllowed(NeedleEffectTypeRules.booleanResult("boolean", null, HookPoint.BEFORE))
        assertAllowed(NeedleEffectTypeRules.booleanResult("boolean", null, HookPoint.AFTER))
    }

    @Test
    fun `a boxed Boolean method accepts BOOLEAN_RESULT without any semantic declaration`() {
        assertAllowed(NeedleEffectTypeRules.booleanResult(NeedleTypeNames.BOXED_BOOLEAN, null, HookPoint.AFTER))
    }

    @Test
    fun `an Object bridge is only accepted as AFTER plus an explicit Boolean semantic type`() {
        assertAllowed(
            NeedleEffectTypeRules.booleanResult(NeedleTypeNames.OBJECT, NeedleTypeNames.BOXED_BOOLEAN, HookPoint.AFTER),
        )
        assertRejected(NeedleEffectTypeRules.booleanResult(NeedleTypeNames.OBJECT, null, HookPoint.AFTER))
        assertRejected(
            NeedleEffectTypeRules.booleanResult(NeedleTypeNames.OBJECT, NeedleTypeNames.BOXED_BOOLEAN, HookPoint.BEFORE),
        )
        assertRejected(
            NeedleEffectTypeRules.booleanResult(NeedleTypeNames.OBJECT, NeedleTypeNames.STRING, HookPoint.AFTER),
        )
    }

    @Test
    fun `BOOLEAN_RESULT is refused on every unrelated return type`() {
        listOf("int", "long", "void", NeedleTypeNames.STRING, "java.util.Locale").forEach { returnType ->
            assertRejected(
                NeedleEffectTypeRules.booleanResult(returnType, NeedleTypeNames.BOXED_BOOLEAN, HookPoint.AFTER),
            )
        }
    }

    @Test
    fun `only an Object return type needs the runtime boxed-Boolean guard`() {
        assertTrue(NeedleEffectTypeRules.requiresBoxedBooleanGuard(NeedleTypeNames.OBJECT))
        assertTrue(!NeedleEffectTypeRules.requiresBoxedBooleanGuard("boolean"))
        assertTrue(!NeedleEffectTypeRules.requiresBoxedBooleanGuard(NeedleTypeNames.BOXED_BOOLEAN))
    }

    @Test
    fun `NUMERIC_RESULT is refused on boolean and on reference types`() {
        assertAllowed(NeedleEffectTypeRules.numericResult("int"))
        assertAllowed(NeedleEffectTypeRules.numericResult("double"))
        assertRejected(NeedleEffectTypeRules.numericResult("boolean"))
        assertRejected(NeedleEffectTypeRules.numericResult("void"))
        assertRejected(NeedleEffectTypeRules.numericResult(NeedleTypeNames.STRING))
    }

    @Test
    fun `ARGUMENT_REPLACE only accepts a constant that fits the parameter`() {
        assertAllowed(NeedleEffectTypeRules.argumentReplace("boolean", ConstantValueType.BOOL))
        assertAllowed(NeedleEffectTypeRules.argumentReplace(NeedleTypeNames.STRING, ConstantValueType.STRING))
        assertRejected(NeedleEffectTypeRules.argumentReplace("boolean", ConstantValueType.STRING))
        assertRejected(NeedleEffectTypeRules.argumentReplace("int", ConstantValueType.BOOL))
        assertRejected(NeedleEffectTypeRules.argumentReplace("java.util.Locale", ConstantValueType.STRING))
        assertRejected(NeedleEffectTypeRules.argumentReplace("boolean", null))
    }

    @Test
    fun `a signature is bound to the schema version whose domain separator produced it`() {
        val generator = KeyPairGenerator.getInstance("EC")
        generator.initialize(ECGenParameterSpec("secp256r1"))
        val keyPair = generator.generateKeyPair()
        val privateKeyBase64 = Base64.getEncoder().encodeToString(keyPair.private.encoded)
        val publicKeyBase64 = Base64.getEncoder().encodeToString(keyPair.public.encoded)
        val payload = """{"schema_version":2}""".toByteArray(Charsets.UTF_8)

        fun sign(separator: String): String {
            val keySpec = PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyBase64))
            val signer = Signature.getInstance("SHA256withECDSA")
            signer.initSign(KeyFactory.getInstance("EC").generatePrivate(keySpec))
            signer.update(separator.toByteArray(Charsets.UTF_8))
            signer.update(payload)
            return Base64.getEncoder().encodeToString(signer.sign())
        }

        val v1Signature = sign(NeedleProtocol.DOMAIN_SEPARATOR)
        val v2Signature = sign(NeedleProtocol.DOMAIN_SEPARATOR_V2)

        assertEquals(1, NeedleSignature.verifiedSchemaVersion(payload, v1Signature, publicKeyBase64))
        assertEquals(2, NeedleSignature.verifiedSchemaVersion(payload, v2Signature, publicKeyBase64))
        assertNull(NeedleSignature.verifiedSchemaVersion(payload, "not-a-signature", publicKeyBase64))
    }
}
