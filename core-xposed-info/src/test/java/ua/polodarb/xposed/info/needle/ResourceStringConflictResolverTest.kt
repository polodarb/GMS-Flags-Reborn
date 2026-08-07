package ua.polodarb.xposed.info.needle

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ResourceStringConflictResolverTest {

    private class FakeContext(
        private val flags: Map<Pair<String?, String?>, Any?> = emptyMap(),
    ) : NeedleEvaluationContext {
        override fun originalResult(): Any? = null
        override fun argument(index: Int): Any? = null
        override fun flagOverride(packageName: String?, flagName: String?, valueType: ConstantValueType?): Any? =
            flags[packageName to flagName]
        override fun systemFeature(name: String?): Boolean = false
        override fun sdkInt(): Int = 34
        override fun resourceId(packageName: String?, resourceType: String?, resourceName: String?): Int? = null
    }

    private fun payloadGatedOn(
        flagName: String,
        replacement: String,
        recipeId: String = flagName,
    ) = NeedleRecipePayload(
        schemaVersion = 1,
        recipeId = recipeId,
        codename = "test-$recipeId",
        appPackageName = "com.google.android.dialer",
        selector = MicroHookSelector(type = SelectorKind.ANDROID_RESOURCE_STRING),
        effect = MicroHookEffect(
            hookPoint = HookPoint.AFTER,
            kind = EffectKind.STRING_RESULT,
            expression = NeedleJson.encodeToJsonElement(
                ValueExpression.serializer(),
                ValueExpression(TypedValueSource(source = SourceKind.CONSTANT, valueType = ConstantValueType.STRING, value = replacement)),
            ),
            `when` = BooleanExpression(orGroups = listOf(AndGroup(listOf(
                Condition(
                    left = TypedValueSource(source = SourceKind.FLAG_OVERRIDE, packageName = "ns#pkg", flagName = flagName, valueType = ConstantValueType.BOOL),
                    compare = CompareOp.EQ,
                    right = TypedValueSource(source = SourceKind.CONSTANT, valueType = ConstantValueType.BOOL, value = "true"),
                ),
            )))),
        ),
    )

    @Test
    fun `no candidate matching returns NoMatch`() {
        val candidates = listOf(payloadGatedOn("flagA", "RECORD"))
        val result = ResourceStringConflictResolver.resolve(candidates, FakeContext())
        assertEquals(ResourceStringDispatchResult.NoMatch, result)
    }

    @Test
    fun `exactly one matching candidate returns Applied with zero redundant matches`() {
        val candidates = listOf(payloadGatedOn("flagA", "RECORD"))
        val context = FakeContext(flags = mapOf(("ns#pkg" to "flagA") to true))
        val result = ResourceStringConflictResolver.resolve(candidates, context)
        assertEquals(ResourceStringDispatchResult.Applied("RECORD", redundantMatchCount = 0), result)
    }

    @Test
    fun `two candidates matching with the identical replacement value is Applied and redundant`() {
        val candidates = listOf(
            payloadGatedOn("flagA", "RECORD", recipeId = "1"),
            payloadGatedOn("flagB", "RECORD", recipeId = "2"),
        )
        val context = FakeContext(flags = mapOf(("ns#pkg" to "flagA") to true, ("ns#pkg" to "flagB") to true))
        val result = ResourceStringConflictResolver.resolve(candidates, context)
        assertEquals(ResourceStringDispatchResult.Applied("RECORD", redundantMatchCount = 1), result)
    }

    @Test
    fun `two candidates matching with different replacement values is Conflict, not applied`() {
        val candidates = listOf(
            payloadGatedOn("flagA", "RECORD", recipeId = "1"),
            payloadGatedOn("flagB", "RECORDING", recipeId = "2"),
        )
        val context = FakeContext(flags = mapOf(("ns#pkg" to "flagA") to true, ("ns#pkg" to "flagB") to true))
        val result = ResourceStringConflictResolver.resolve(candidates, context)
        assertTrue(result is ResourceStringDispatchResult.Conflict)
        val matches = (result as ResourceStringDispatchResult.Conflict).matches
        assertEquals(listOf(MatchedRecipe("1", "RECORD"), MatchedRecipe("2", "RECORDING")), matches)
    }

    @Test
    fun `a candidate whose value expression does not resolve to a String is never returned as Applied`() {
        val nonStringPayload = payloadGatedOn("flagA", "unused-recipeId-only").copy(
            effect = payloadGatedOn("flagA", "unused-recipeId-only").effect.copy(
                expression = NeedleJson.encodeToJsonElement(
                    ValueExpression.serializer(),
                    ValueExpression(TypedValueSource(source = SourceKind.CONSTANT, valueType = ConstantValueType.BOOL, value = "true")),
                ),
            ),
        )
        val context = FakeContext(flags = mapOf(("ns#pkg" to "flagA") to true))
        val result = ResourceStringConflictResolver.resolve(listOf(nonStringPayload), context)
        assertEquals(ResourceStringDispatchResult.NoMatch, result)
    }

    @Test
    fun `mutually exclusive when conditions on the same resource do not conflict`() {
        val candidates = listOf(
            payloadGatedOn("flagA", "RECORD", recipeId = "1"),
            payloadGatedOn("flagB", "RECORDING", recipeId = "2"),
        )
        val context = FakeContext(flags = mapOf(("ns#pkg" to "flagA") to true, ("ns#pkg" to "flagB") to false))
        val result = ResourceStringConflictResolver.resolve(candidates, context)
        assertEquals(ResourceStringDispatchResult.Applied("RECORD", redundantMatchCount = 0), result)
    }
}
