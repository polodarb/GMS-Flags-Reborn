package ua.polodarb.gmsflags.domain.flags

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import ua.polodarb.gmsflags.domain.server.content.HookEffectExpression
import ua.polodarb.gmsflags.domain.server.content.HookEffectKind
import ua.polodarb.gmsflags.domain.server.content.HookRuntimePoint
import ua.polodarb.gmsflags.domain.server.content.HookSelectorKind
import ua.polodarb.gmsflags.domain.server.content.HookValueSourceKind
import ua.polodarb.gmsflags.domain.server.content.RecommendationVariantHook
import ua.polodarb.gmsflags.domain.server.content.SignedNeedleEnvelope

class DecodeHookRecipeUseCaseTest {
    private val decode = DecodeHookRecipeUseCase()

    @Test
    fun `decodes a real neural-design-master-gate recipe`() {
        val hook = RecommendationVariantHook(
            recipeId = 1,
            required = true,
            codename = "neural-design-master-gate",
            purpose = "Force the Neural Design master device gate when override 45814173 is " +
                "enabled, while preserving the original true result",
            envelope = SignedNeedleEnvelope(
                mediaType = "application/vnd.gmsflags.needle-recipe+json;v=1",
                payloadBase64 = REAL_PAYLOAD_BASE64,
                payloadSha256 = "4c8762087561a68c997a44d99ceabb11234a95674b1dea8bd0c62573b913c29f",
                signatureAlgorithm = "ECDSA_P256_SHA256",
                signatureBase64 = "unused-in-this-test",
            ),
        )

        val details = decode(hook).getOrThrow()

        assertEquals("com.google.android.googlequicksearchbox", details.appPackageName)
        assertEquals("com.google.android.googlequicksearchbox:search", details.processName)

        assertEquals(HookSelectorKind.DEX_METHOD, details.selector.kind)
        assertEquals("boolean", details.selector.methodReturnType)
        assertTrue(details.selector.methodParameterTypes.isEmpty())
        assertEquals(listOf("final"), details.selector.methodModifiersAll)
        assertEquals(
            listOf("com.google.android.apps.search.assistant.mobile.user", "45814173"),
            details.selector.methodUsingStringsAll,
        )

        assertEquals(HookRuntimePoint.AFTER, details.effect.hookPoint)
        assertEquals(HookEffectKind.BOOLEAN_RESULT, details.effect.kind)
        val expression = details.effect.expression
        assertTrue(expression is HookEffectExpression.Conditional)
        val orGroups = (expression as HookEffectExpression.Conditional).expression.orGroups
        assertEquals(2, orGroups.size)

        val rule1 = orGroups[0].conditions.single()
        assertEquals(HookValueSourceKind.ORIGINAL_RESULT, rule1.left.kind)
        assertEquals(HookValueSourceKind.CONSTANT, rule1.right.kind)
        assertEquals("true", rule1.right.constantValue)

        val rule2 = orGroups[1].conditions.single()
        assertEquals(HookValueSourceKind.FLAG_OVERRIDE, rule2.left.kind)
        assertEquals("45814173", rule2.left.flagName)
        assertEquals(
            "com.google.android.apps.search.assistant.mobile.user#" +
                "com.google.android.googlequicksearchbox",
            rule2.left.packageName,
        )
        assertEquals("1", rule2.right.constantValue)
    }

    private companion object {
        const val REAL_PAYLOAD_BASE64 =
            "eyJzY2hlbWFfdmVyc2lvbiI6MSwicmVjaXBlX2lkIjoiMSIsImNvZGVuYW1lIjoibmV1cmFsLWRlc2ln" +
                "bi1tYXN0ZXItZ2F0ZSIsImFwcF9wYWNrYWdlX25hbWUiOiJjb20uZ29vZ2xlLmFuZHJvaWQuZ29vZ2" +
                "xlcXVpY2tzZWFyY2hib3giLCJwcm9jZXNzX25hbWUiOiJjb20uZ29vZ2xlLmFuZHJvaWQuZ29vZ2xl" +
                "cXVpY2tzZWFyY2hib3g6c2VhcmNoIiwic2VsZWN0b3IiOnsiY2xhc3NfdXNpbmdfc3RyaW5nc19hbG" +
                "wiOltdLCJjbGFzc191c2luZ19zdHJpbmdzX2FueSI6W10sIm1ldGhvZF9yZXR1cm5fdHlwZSI6ImJv" +
                "b2xlYW4iLCJtZXRob2RfcGFyYW1ldGVyX3R5cGVzIjpbXSwibWV0aG9kX21vZGlmaWVyc19hbGwiOl" +
                "siZmluYWwiXSwibWV0aG9kX3VzaW5nX3N0cmluZ3NfYWxsIjpbImNvbS5nb29nbGUuYW5kcm9pZC5h" +
                "cHBzLnNlYXJjaC5hc3Npc3RhbnQubW9iaWxlLnVzZXIiLCI0NTgxNDE3MyJdLCJtZXRob2RfdXNpbm" +
                "dfc3RyaW5nc19hbnkiOltdfSwiZWZmZWN0Ijp7Imhvb2tfcG9pbnQiOiJBRlRFUiIsImtpbmQiOiJC" +
                "T09MRUFOX1JFU1VMVCIsImFyZ3VtZW50X2luZGV4IjpudWxsLCJleHByZXNzaW9uIjp7Im9yX2dyb3" +
                "VwcyI6W3siYW5kX2NvbmRpdGlvbnMiOlt7ImxlZnQiOnsic291cmNlIjoiT1JJR0lOQUxfUkVTVUxU" +
                "In0sImNvbXBhcmUiOiJFUSIsInJpZ2h0Ijp7InNvdXJjZSI6IkNPTlNUQU5UIiwidmFsdWVfdHlwZS" +
                "I6IkJPT0wiLCJ2YWx1ZSI6InRydWUifX1dfSx7ImFuZF9jb25kaXRpb25zIjpbeyJsZWZ0Ijp7InNv" +
                "dXJjZSI6IkZMQUdfT1ZFUlJJREUiLCJwYWNrYWdlX25hbWUiOiJjb20uZ29vZ2xlLmFuZHJvaWQuYX" +
                "Bwcy5zZWFyY2guYXNzaXN0YW50Lm1vYmlsZS51c2VyI2NvbS5nb29nbGUuYW5kcm9pZC5nb29nbGVx" +
                "dWlja3NlYXJjaGJveCIsImZsYWdfbmFtZSI6IjQ1ODE0MTczIiwidmFsdWVfdHlwZSI6IlNUUklORy" +
                "J9LCJjb21wYXJlIjoiRVEiLCJyaWdodCI6eyJzb3VyY2UiOiJDT05TVEFOVCIsInZhbHVlX3R5cGUi" +
                "OiJTVFJJTkciLCJ2YWx1ZSI6IjEifX1dfV19fX0="
    }
}
