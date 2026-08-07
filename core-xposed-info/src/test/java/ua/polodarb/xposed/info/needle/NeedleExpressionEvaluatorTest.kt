package ua.polodarb.xposed.info.needle

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

private class FakeContext(
    private val originalResult: Any? = null,
    private val arguments: Map<Int, Any?> = emptyMap(),
    private val flags: Map<Pair<String?, String?>, Any?> = emptyMap(),
    private val features: Set<String> = emptySet(),
    private val sdk: Int = 34,
    private val resourceIds: Map<Triple<String?, String?, String?>, Int> = emptyMap(),
) : NeedleEvaluationContext {
    override fun originalResult(): Any? = originalResult
    override fun argument(index: Int): Any? = arguments[index]
    override fun flagOverride(packageName: String?, flagName: String?, valueType: ConstantValueType?): Any? =
        flags[packageName to flagName]
    override fun systemFeature(name: String?): Boolean = name in features
    override fun sdkInt(): Int = sdk
    override fun resourceId(packageName: String?, resourceType: String?, resourceName: String?): Int? =
        resourceIds[Triple(packageName, resourceType, resourceName)]
}

private fun case001Expression() = BooleanExpression(
    orGroups = listOf(
        AndGroup(listOf(
            Condition(
                left = TypedValueSource(source = SourceKind.ORIGINAL_RESULT),
                compare = CompareOp.EQ,
                right = TypedValueSource(source = SourceKind.CONSTANT, valueType = ConstantValueType.BOOL, value = "true"),
            ),
        )),
        AndGroup(listOf(
            Condition(
                left = TypedValueSource(source = SourceKind.FLAG_OVERRIDE, packageName = "ns#pkg", flagName = "45814173", valueType = ConstantValueType.BOOL),
                compare = CompareOp.EQ,
                right = TypedValueSource(source = SourceKind.CONSTANT, valueType = ConstantValueType.BOOL, value = "true"),
            ),
            Condition(
                left = TypedValueSource(source = SourceKind.SYSTEM_FEATURE, name = "PIXEL_2026_EXPERIENCE"),
                compare = CompareOp.EQ,
                right = TypedValueSource(source = SourceKind.CONSTANT, valueType = ConstantValueType.BOOL, value = "true"),
            ),
        )),
    ),
)

class NeedleExpressionEvaluatorTest {

    @Test
    fun `first OR-group true short-circuits to true regardless of second group`() {
        val context = FakeContext(originalResult = true)
        assertTrue(NeedleExpressionEvaluator.evaluateBoolean(case001Expression(), context))
    }

    @Test
    fun `second OR-group true when both AND conditions hold`() {
        val context = FakeContext(
            originalResult = false,
            flags = mapOf(("ns#pkg" to "45814173") to true),
            features = setOf("PIXEL_2026_EXPERIENCE"),
        )
        assertTrue(NeedleExpressionEvaluator.evaluateBoolean(case001Expression(), context))
    }

    @Test
    fun `false when neither OR-group is satisfied`() {
        val context = FakeContext(originalResult = false, flags = mapOf(("ns#pkg" to "45814173") to true))
        assertFalse(NeedleExpressionEvaluator.evaluateBoolean(case001Expression(), context))
    }

    @Test
    fun `AND-group with only one satisfied condition is false`() {
        val context = FakeContext(originalResult = false, features = setOf("PIXEL_2026_EXPERIENCE"))
        assertFalse(NeedleExpressionEvaluator.evaluateBoolean(case001Expression(), context))
    }

    @Test
    fun `numeric compare operators evaluate correctly`() {
        val expression = BooleanExpression(listOf(AndGroup(listOf(
            Condition(
                left = TypedValueSource(source = SourceKind.ARGUMENT, index = 0),
                compare = CompareOp.GT,
                right = TypedValueSource(source = SourceKind.CONSTANT, valueType = ConstantValueType.INT, value = "10"),
            ),
        ))))
        assertTrue(NeedleExpressionEvaluator.evaluateBoolean(expression, FakeContext(arguments = mapOf(0 to 15))))
        assertFalse(NeedleExpressionEvaluator.evaluateBoolean(expression, FakeContext(arguments = mapOf(0 to 5))))
    }

    @Test
    fun `evaluateValue resolves a plain typed source for non-boolean effects`() {
        val source = TypedValueSource(source = SourceKind.CONSTANT, valueType = ConstantValueType.INT, value = "42")
        assertEquals(42, NeedleExpressionEvaluator.evaluateValue(source, FakeContext()))
    }

    @Test
    fun `evaluateValue resolves SDK_INT and SYSTEM_FEATURE sources`() {
        assertEquals(34, NeedleExpressionEvaluator.evaluateValue(TypedValueSource(source = SourceKind.SDK_INT), FakeContext(sdk = 34)))
        assertEquals(
            true,
            NeedleExpressionEvaluator.evaluateValue(TypedValueSource(source = SourceKind.SYSTEM_FEATURE, name = "X"), FakeContext(features = setOf("X"))),
        )
    }

    @Test
    fun `evaluateValue resolves RESOURCE_ID via the context`() {
        val source = TypedValueSource(
            source = SourceKind.RESOURCE_ID,
            packageName = "com.google.android.dialer",
            resourceType = "string",
            resourceName = "call_recording_starting_voice",
        )
        val context = FakeContext(
            resourceIds = mapOf(Triple("com.google.android.dialer", "string", "call_recording_starting_voice") to 12345),
        )
        assertEquals(12345, NeedleExpressionEvaluator.evaluateValue(source, context))
    }

    @Test
    fun `RESOURCE_ID condition matches ARGUMENT equal to the resolved id`() {
        val expression = BooleanExpression(listOf(AndGroup(listOf(
            Condition(
                left = TypedValueSource(source = SourceKind.ARGUMENT, index = 0),
                compare = CompareOp.EQ,
                right = TypedValueSource(
                    source = SourceKind.RESOURCE_ID,
                    packageName = "com.google.android.dialer",
                    resourceType = "string",
                    resourceName = "call_recording_starting_voice",
                ),
            ),
        ))))
        val context = FakeContext(
            arguments = mapOf(0 to 12345),
            resourceIds = mapOf(Triple("com.google.android.dialer", "string", "call_recording_starting_voice") to 12345),
        )
        assertTrue(NeedleExpressionEvaluator.evaluateBoolean(expression, context))

        val mismatchedContext = FakeContext(
            arguments = mapOf(0 to 99999),
            resourceIds = mapOf(Triple("com.google.android.dialer", "string", "call_recording_starting_voice") to 12345),
        )
        assertFalse(NeedleExpressionEvaluator.evaluateBoolean(expression, mismatchedContext))
    }
}
