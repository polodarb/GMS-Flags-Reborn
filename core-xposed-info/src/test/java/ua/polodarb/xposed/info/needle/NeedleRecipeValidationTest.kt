package ua.polodarb.xposed.info.needle

import kotlinx.serialization.json.JsonNull
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NeedleRecipeValidationTest {

    private fun resourceIdCondition() = BooleanExpression(
        orGroups = listOf(AndGroup(listOf(
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
        ))),
    )

    private fun stringResultPayload(
        hookPoint: HookPoint = HookPoint.AFTER,
        condition: BooleanExpression? = resourceIdCondition(),
    ) = NeedleRecipePayload(
        schemaVersion = 1,
        recipeId = "1",
        codename = "dialer-call-recording-start-announcement",
        appPackageName = "com.google.android.dialer",
        selector = MicroHookSelector(type = SelectorKind.ANDROID_RESOURCE_STRING),
        effect = MicroHookEffect(
            hookPoint = hookPoint,
            kind = EffectKind.STRING_RESULT,
            expression = NeedleJson.encodeToJsonElement(
                ValueExpression.serializer(),
                ValueExpression(TypedValueSource(source = SourceKind.CONSTANT, valueType = ConstantValueType.STRING, value = "RECORD")),
            ),
            `when` = condition,
        ),
    )

    private fun dexMethodPayload(kind: EffectKind) = NeedleRecipePayload(
        schemaVersion = 1,
        recipeId = "2",
        codename = "some-dex-recipe",
        appPackageName = "com.example.app",
        selector = MicroHookSelector(type = SelectorKind.DEX_METHOD, methodReturnType = "boolean"),
        effect = MicroHookEffect(hookPoint = HookPoint.AFTER, kind = kind, expression = JsonNull),
    )

    @Test
    fun `a well-formed ANDROID_RESOURCE_STRING plus STRING_RESULT recipe is valid`() {
        assertEquals(NeedleRecipeValidationResult.Valid, NeedleRecipeValidation.validate(stringResultPayload()))
    }

    @Test
    fun `unsupported schema_version is rejected`() {
        val payload = stringResultPayload().copy(schemaVersion = 999)
        val result = NeedleRecipeValidation.validate(payload)
        assertTrue(result is NeedleRecipeValidationResult.Invalid)
    }

    @Test
    fun `ANDROID_RESOURCE_STRING selector paired with a non-STRING_RESULT effect is rejected`() {
        val payload = stringResultPayload().copy(
            effect = stringResultPayload().effect.copy(kind = EffectKind.BOOLEAN_RESULT),
        )
        val result = NeedleRecipeValidation.validate(payload)
        assertTrue(result is NeedleRecipeValidationResult.Invalid)
    }

    @Test
    fun `ANDROID_RESOURCE_STRING selector with hook_point BEFORE is rejected`() {
        val result = NeedleRecipeValidation.validate(stringResultPayload(hookPoint = HookPoint.BEFORE))
        assertTrue(result is NeedleRecipeValidationResult.Invalid)
    }

    @Test
    fun `STRING_RESULT without a when condition is rejected`() {
        val result = NeedleRecipeValidation.validate(stringResultPayload(condition = null))
        assertTrue(result is NeedleRecipeValidationResult.Invalid)
    }

    @Test
    fun `STRING_RESULT with a when condition that never references RESOURCE_ID is rejected`() {
        val conditionWithoutResourceId = BooleanExpression(
            orGroups = listOf(AndGroup(listOf(
                Condition(
                    left = TypedValueSource(source = SourceKind.SDK_INT),
                    compare = CompareOp.GTE,
                    right = TypedValueSource(source = SourceKind.CONSTANT, valueType = ConstantValueType.INT, value = "30"),
                ),
            ))),
        )
        val result = NeedleRecipeValidation.validate(stringResultPayload(condition = conditionWithoutResourceId))
        assertTrue(result is NeedleRecipeValidationResult.Invalid)
    }

    @Test
    fun `an or_group without a resource anchor is rejected even though another or_group has one`() {
        val unanchoredSecondBranch = BooleanExpression(
            orGroups = listOf(
                AndGroup(listOf(
                    Condition(
                        left = TypedValueSource(source = SourceKind.ARGUMENT, index = 0),
                        compare = CompareOp.EQ,
                        right = TypedValueSource(source = SourceKind.RESOURCE_ID, packageName = "com.google.android.dialer", resourceType = "string", resourceName = "call_recording_starting_voice"),
                    ),
                )),
                AndGroup(listOf(
                    Condition(
                        left = TypedValueSource(source = SourceKind.FLAG_OVERRIDE, packageName = "ns#pkg", flagName = "flagA", valueType = ConstantValueType.BOOL),
                        compare = CompareOp.EQ,
                        right = TypedValueSource(source = SourceKind.CONSTANT, valueType = ConstantValueType.BOOL, value = "true"),
                    ),
                )),
            ),
        )
        val result = NeedleRecipeValidation.validate(stringResultPayload(condition = unanchoredSecondBranch))
        assertTrue(result is NeedleRecipeValidationResult.Invalid)
    }

    @Test
    fun `two or_groups each independently anchored to a different resource id are both valid`() {
        val twoAnchoredGroups = BooleanExpression(
            orGroups = listOf(
                AndGroup(listOf(
                    Condition(
                        left = TypedValueSource(source = SourceKind.ARGUMENT, index = 0),
                        compare = CompareOp.EQ,
                        right = TypedValueSource(source = SourceKind.RESOURCE_ID, packageName = "com.google.android.dialer", resourceType = "string", resourceName = "call_recording_starting_voice"),
                    ),
                )),
                AndGroup(listOf(
                    Condition(
                        left = TypedValueSource(source = SourceKind.ARGUMENT, index = 0),
                        compare = CompareOp.EQ,
                        right = TypedValueSource(source = SourceKind.RESOURCE_ID, packageName = "com.google.android.dialer", resourceType = "string", resourceName = "call_recording_ending_voice"),
                    ),
                )),
            ),
        )
        assertEquals(NeedleRecipeValidationResult.Valid, NeedleRecipeValidation.validate(stringResultPayload(condition = twoAnchoredGroups)))
    }

    @Test
    fun `a RESOURCE_ID dispatch key with a blank package_name is rejected`() {
        val condition = resourceIdCondition().let {
            it.copy(orGroups = it.orGroups.map { group ->
                group.copy(andConditions = group.andConditions.map { c -> c.copy(right = c.right.copy(packageName = "")) })
            })
        }
        val result = NeedleRecipeValidation.validate(stringResultPayload(condition = condition))
        assertTrue(result is NeedleRecipeValidationResult.Invalid)
    }

    @Test
    fun `a RESOURCE_ID dispatch key with a blank resource_name is rejected`() {
        val condition = resourceIdCondition().let {
            it.copy(orGroups = it.orGroups.map { group ->
                group.copy(andConditions = group.andConditions.map { c -> c.copy(right = c.right.copy(resourceName = "")) })
            })
        }
        val result = NeedleRecipeValidation.validate(stringResultPayload(condition = condition))
        assertTrue(result is NeedleRecipeValidationResult.Invalid)
    }

    @Test
    fun `a RESOURCE_ID dispatch key whose resource_type is not string is rejected`() {
        val condition = resourceIdCondition().let {
            it.copy(orGroups = it.orGroups.map { group ->
                group.copy(andConditions = group.andConditions.map { c -> c.copy(right = c.right.copy(resourceType = "drawable")) })
            })
        }
        val result = NeedleRecipeValidation.validate(stringResultPayload(condition = condition))
        assertTrue(result is NeedleRecipeValidationResult.Invalid)
    }

    @Test
    fun `RESOURCE_ID compared with NEQ is not a valid dispatch key, even though a RESOURCE_ID source is present`() {
        val neqCondition = BooleanExpression(orGroups = listOf(AndGroup(listOf(
            Condition(
                left = TypedValueSource(source = SourceKind.ARGUMENT, index = 0),
                compare = CompareOp.NEQ,
                right = TypedValueSource(source = SourceKind.RESOURCE_ID, packageName = "com.google.android.dialer", resourceType = "string", resourceName = "call_recording_starting_voice"),
            ),
        ))))
        val result = NeedleRecipeValidation.validate(stringResultPayload(condition = neqCondition))
        assertTrue(result is NeedleRecipeValidationResult.Invalid)
    }

    @Test
    fun `RESOURCE_ID compared against something other than ARGUMENT 0 is not a valid dispatch key`() {
        val wrongLeftSide = BooleanExpression(orGroups = listOf(AndGroup(listOf(
            Condition(
                left = TypedValueSource(source = SourceKind.ARGUMENT, index = 1),
                compare = CompareOp.EQ,
                right = TypedValueSource(source = SourceKind.RESOURCE_ID, packageName = "com.google.android.dialer", resourceType = "string", resourceName = "call_recording_starting_voice"),
            ),
        ))))
        val result = NeedleRecipeValidation.validate(stringResultPayload(condition = wrongLeftSide))
        assertTrue(result is NeedleRecipeValidationResult.Invalid)
    }

    @Test
    fun `STRING_RESULT paired with DEX_METHOD selector is rejected`() {
        val result = NeedleRecipeValidation.validate(dexMethodPayload(EffectKind.STRING_RESULT))
        assertTrue(result is NeedleRecipeValidationResult.Invalid)
    }

    @Test
    fun `BOOLEAN_RESULT, NUMERIC_RESULT, and ARGUMENT_REPLACE on DEX_METHOD stay valid`() {
        assertEquals(NeedleRecipeValidationResult.Valid, NeedleRecipeValidation.validate(dexMethodPayload(EffectKind.BOOLEAN_RESULT)))
        assertEquals(NeedleRecipeValidationResult.Valid, NeedleRecipeValidation.validate(dexMethodPayload(EffectKind.NUMERIC_RESULT)))
        assertEquals(NeedleRecipeValidationResult.Valid, NeedleRecipeValidation.validate(dexMethodPayload(EffectKind.ARGUMENT_REPLACE)))
    }

    @Test
    fun `DEX_METHOD with a blank method_return_type is rejected`() {
        val payload = dexMethodPayload(EffectKind.BOOLEAN_RESULT).let {
            it.copy(selector = it.selector.copy(methodReturnType = ""))
        }
        val result = NeedleRecipeValidation.validate(payload)
        assertTrue(result is NeedleRecipeValidationResult.Invalid)
    }

    @Test
    fun `DEX_METHOD with an unsupported method_return_type is rejected`() {
        val payload = dexMethodPayload(EffectKind.BOOLEAN_RESULT).let {
            it.copy(selector = it.selector.copy(methodReturnType = "java.lang.String"))
        }
        val result = NeedleRecipeValidation.validate(payload)
        assertTrue(result is NeedleRecipeValidationResult.Invalid)
    }

    @Test
    fun `STRING_RESULT value must be a non-null CONSTANT of type STRING`() {
        fun withValue(source: TypedValueSource) = stringResultPayload().let {
            it.copy(effect = it.effect.copy(
                expression = NeedleJson.encodeToJsonElement(ValueExpression.serializer(), ValueExpression(source)),
            ))
        }

        val nonConstant = withValue(TypedValueSource(source = SourceKind.ARGUMENT, index = 0))
        assertTrue(NeedleRecipeValidation.validate(nonConstant) is NeedleRecipeValidationResult.Invalid)

        val wrongType = withValue(TypedValueSource(source = SourceKind.CONSTANT, valueType = ConstantValueType.BOOL, value = "true"))
        assertTrue(NeedleRecipeValidation.validate(wrongType) is NeedleRecipeValidationResult.Invalid)

        val nullValue = withValue(TypedValueSource(source = SourceKind.CONSTANT, valueType = ConstantValueType.STRING, value = null))
        assertTrue(NeedleRecipeValidation.validate(nullValue) is NeedleRecipeValidationResult.Invalid)

        val valid = withValue(TypedValueSource(source = SourceKind.CONSTANT, valueType = ConstantValueType.STRING, value = "RECORD"))
        assertEquals(NeedleRecipeValidationResult.Valid, NeedleRecipeValidation.validate(valid))
    }

    @Test
    fun `STRING_RESULT value exceeding the length limit is rejected`() {
        val tooLong = "x".repeat(257)
        val payload = stringResultPayload().let {
            it.copy(effect = it.effect.copy(
                expression = NeedleJson.encodeToJsonElement(
                    ValueExpression.serializer(),
                    ValueExpression(TypedValueSource(source = SourceKind.CONSTANT, valueType = ConstantValueType.STRING, value = tooLong)),
                ),
            ))
        }
        val result = NeedleRecipeValidation.validate(payload)
        assertTrue(result is NeedleRecipeValidationResult.Invalid)
    }

    @Test
    fun `dispatchKeyResourceSources only matches the canonical ARGUMENT 0 EQ RESOURCE_ID shape`() {
        val expression = BooleanExpression(orGroups = listOf(AndGroup(listOf(
            Condition(
                left = TypedValueSource(source = SourceKind.ARGUMENT, index = 0),
                compare = CompareOp.EQ,
                right = TypedValueSource(source = SourceKind.RESOURCE_ID, resourceType = "string", resourceName = "a"),
            ),
            Condition(
                left = TypedValueSource(source = SourceKind.RESOURCE_ID, resourceType = "string", resourceName = "b"),
                compare = CompareOp.EQ,
                right = TypedValueSource(source = SourceKind.ARGUMENT, index = 0),
            ),
            Condition(
                left = TypedValueSource(source = SourceKind.ARGUMENT, index = 0),
                compare = CompareOp.NEQ,
                right = TypedValueSource(source = SourceKind.RESOURCE_ID, resourceType = "string", resourceName = "c"),
            ),
        ))))
        val sources = expression.dispatchKeyResourceSources()
        assertEquals(1, sources.size)
        assertEquals("a", sources[0].resourceName)
    }

    @Test
    fun `dispatchKeyResourceSources returns empty for an expression with no canonical condition`() {
        val expression = BooleanExpression(orGroups = listOf(AndGroup(listOf(
            Condition(
                left = TypedValueSource(source = SourceKind.SDK_INT),
                compare = CompareOp.GTE,
                right = TypedValueSource(source = SourceKind.CONSTANT, valueType = ConstantValueType.INT, value = "30"),
            ),
        ))))
        assertTrue(expression.dispatchKeyResourceSources().isEmpty())
    }
}
