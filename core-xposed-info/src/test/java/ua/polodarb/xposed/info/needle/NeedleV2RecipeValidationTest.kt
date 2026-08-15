package ua.polodarb.xposed.info.needle

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NeedleV2RecipeValidationTest {

    private fun constant(valueType: ConstantValueType, value: String?) = NeedleJson.encodeToJsonElement(
        ValueExpression.serializer(),
        ValueExpression(TypedValueSource(source = SourceKind.CONSTANT, valueType = valueType, value = value)),
    )

    private fun alwaysTrue() = NeedleJson.encodeToJsonElement(
        BooleanExpression.serializer(),
        BooleanExpression(
            orGroups = listOf(
                AndGroup(
                    listOf(
                        Condition(
                            left = TypedValueSource(source = SourceKind.CONSTANT, valueType = ConstantValueType.INT, value = "1"),
                            compare = CompareOp.EQ,
                            right = TypedValueSource(source = SourceKind.CONSTANT, valueType = ConstantValueType.INT, value = "1"),
                        ),
                    ),
                ),
            ),
        ),
    )

    private fun v2Payload(
        selector: MicroHookSelector,
        effect: MicroHookEffect,
        versionConstraint: NeedleVersionConstraint? = NeedleVersionConstraint(min = "301784999", max = "301784999"),
        revision: String? = "1",
        capabilities: List<String> = emptyList(),
        minimumEngineVersion: Int = 2,
    ) = NeedleRecipePayload(
        schemaVersion = 2,
        recipeId = "10",
        codename = "cts-live-translate",
        appPackageName = "com.google.android.googlequicksearchbox",
        processName = "com.google.android.googlequicksearchbox:googleapp",
        revision = revision,
        minimumEngineVersion = minimumEngineVersion,
        requiredCapabilities = capabilities,
        versionConstraint = versionConstraint,
        selector = selector,
        effect = effect,
    )

    private fun entrypointSelector() = MicroHookSelector(
        type = SelectorKind.DEX_METHOD,
        methodUsingStringsAll = listOf("android.media.projection.extra.EXTRA_MEDIA_PROJECTION"),
        methodReturnType = NeedleTypeNames.OBJECT,
        methodParameterTypes = listOf(NeedleTypeNames.OBJECT),
        semanticResultType = NeedleTypeNames.BOXED_BOOLEAN,
    )

    private fun entrypointEffect(hookPoint: HookPoint = HookPoint.AFTER) = MicroHookEffect(
        hookPoint = hookPoint,
        kind = EffectKind.BOOLEAN_RESULT,
        expression = alwaysTrue(),
    )

    private fun languageSelector() = MicroHookSelector(
        type = SelectorKind.DEX_METHOD,
        methodUsingStringsAll = listOf("auto"),
        methodReturnType = "java.util.Locale",
        methodParameterTypes = listOf("boolean"),
        methodInvokesAll = listOf(
            InvokedMethodSignature(
                declaringType = "java.util.Locale",
                name = "forLanguageTag",
                returnType = "java.util.Locale",
                parameterTypes = listOf(NeedleTypeNames.STRING),
            ),
        ),
    )

    private fun languageEffect() = MicroHookEffect(
        hookPoint = HookPoint.BEFORE,
        kind = EffectKind.ARGUMENT_REPLACE,
        argumentIndex = 0,
        expression = constant(ConstantValueType.BOOL, "false"),
    )

    private fun assertRejected(payload: NeedleRecipePayload) {
        assertTrue(
            "expected the recipe to be rejected",
            NeedleRecipeValidation.validate(payload) is NeedleRecipeValidationResult.Invalid,
        )
    }

    @Test
    fun `the Live Translate entrypoint recipe is valid as an AFTER Object bridge with a Boolean semantic type`() {
        val payload = v2Payload(
            selector = entrypointSelector(),
            effect = entrypointEffect(),
            capabilities = listOf(NeedleCapabilities.REFERENCE_TYPES, NeedleCapabilities.BOXED_BOOLEAN_RESULT),
        )
        assertEquals(NeedleRecipeValidationResult.Valid, NeedleRecipeValidation.validate(payload))
    }

    @Test
    fun `an Object-returning BOOLEAN_RESULT without a semantic result type is rejected`() {
        assertRejected(
            v2Payload(
                selector = entrypointSelector().copy(semanticResultType = null),
                effect = entrypointEffect(),
            ),
        )
    }

    @Test
    fun `an Object bridge hooked BEFORE is rejected because the original result cannot be type-checked`() {
        assertRejected(v2Payload(selector = entrypointSelector(), effect = entrypointEffect(HookPoint.BEFORE)))
    }

    @Test
    fun `BOOLEAN_RESULT on a String-returning method is rejected`() {
        assertRejected(
            v2Payload(
                selector = entrypointSelector().copy(
                    methodReturnType = NeedleTypeNames.STRING,
                    semanticResultType = null,
                ),
                effect = entrypointEffect(),
            ),
        )
    }

    @Test
    fun `the Live Translate language recipe is valid despite a reference return type`() {
        val payload = v2Payload(
            selector = languageSelector(),
            effect = languageEffect(),
            capabilities = listOf(NeedleCapabilities.REFERENCE_TYPES, NeedleCapabilities.STRUCTURAL_SELECTORS),
        )
        assertEquals(NeedleRecipeValidationResult.Valid, NeedleRecipeValidation.validate(payload))
    }

    @Test
    fun `ARGUMENT_REPLACE writing a String into a boolean parameter is rejected`() {
        assertRejected(
            v2Payload(
                selector = languageSelector(),
                effect = languageEffect().copy(expression = constant(ConstantValueType.STRING, "false")),
            ),
        )
    }

    @Test
    fun `ARGUMENT_REPLACE with an argument_index outside the declared parameters is rejected`() {
        assertRejected(
            v2Payload(selector = languageSelector(), effect = languageEffect().copy(argumentIndex = 3)),
        )
    }

    @Test
    fun `ARGUMENT_REPLACE computed from a hook-time source instead of a constant is rejected`() {
        val computed = NeedleJson.encodeToJsonElement(
            ValueExpression.serializer(),
            ValueExpression(TypedValueSource(source = SourceKind.ORIGINAL_RESULT)),
        )
        assertRejected(
            v2Payload(selector = languageSelector(), effect = languageEffect().copy(expression = computed)),
        )
    }

    @Test
    fun `a write-target return type outside the allowlist is rejected`() {
        assertRejected(
            v2Payload(
                selector = entrypointSelector().copy(methodReturnType = "com.foo.Bar", semanticResultType = null),
                effect = entrypointEffect(),
            ),
        )
    }

    @Test
    fun `a matching-only return type may be any well-formed name and fails closed later, not at validation`() {
        val payload = v2Payload(
            selector = languageSelector().copy(methodReturnType = "java.util.Locate"),
            effect = languageEffect(),
            capabilities = listOf(NeedleCapabilities.REFERENCE_TYPES),
        )
        assertEquals(NeedleRecipeValidationResult.Valid, NeedleRecipeValidation.validate(payload))
    }

    @Test
    fun `an unknown modifier is rejected instead of being ignored`() {
        assertRejected(
            v2Payload(
                selector = languageSelector().copy(methodModifiersAll = listOf("public", "transient")),
                effect = languageEffect(),
            ),
        )
    }

    @Test
    fun `a DEX_METHOD recipe with no string anchor is rejected`() {
        assertRejected(
            v2Payload(
                selector = languageSelector().copy(methodUsingStringsAll = emptyList()),
                effect = languageEffect(),
            ),
        )
    }

    @Test
    fun `a DEX_METHOD recipe without a version_constraint is rejected`() {
        assertRejected(
            v2Payload(selector = languageSelector(), effect = languageEffect(), versionConstraint = null),
        )
    }

    @Test
    fun `an UNBOUNDED version_constraint is rejected for DEX_METHOD`() {
        assertRejected(
            v2Payload(
                selector = languageSelector(),
                effect = languageEffect(),
                versionConstraint = NeedleVersionConstraint(type = VersionConstraintKind.UNBOUNDED),
            ),
        )
    }

    @Test
    fun `a version_constraint with min greater than max is rejected`() {
        assertRejected(
            v2Payload(
                selector = languageSelector(),
                effect = languageEffect(),
                versionConstraint = NeedleVersionConstraint(min = "300", max = "200"),
            ),
        )
    }

    @Test
    fun `a blank revision is rejected`() {
        assertRejected(v2Payload(selector = languageSelector(), effect = languageEffect(), revision = " "))
    }

    @Test
    fun `an unknown required capability is rejected`() {
        assertRejected(
            v2Payload(
                selector = languageSelector(),
                effect = languageEffect(),
                capabilities = listOf("ARBITRARY_OBJECT_RESULT"),
            ),
        )
    }

    @Test
    fun `a recipe requiring a newer engine than this build is rejected`() {
        assertRejected(
            v2Payload(selector = languageSelector(), effect = languageEffect(), minimumEngineVersion = 99),
        )
    }

    private fun dropdownFixSelector() = MicroHookSelector(
        type = SelectorKind.DEX_METHOD,
        methodUsingStringsAll = listOf("ButtonUiModel(buttonText="),
        methodReturnType = "void",
        methodParameterTypes = listOf("bbpo", "bbpj", "ilg", "hrk", "int"),
        methodModifiersAll = listOf("public", "static", "final"),
    )

    private fun argumentNullEffect(index: Int = 0, hookPoint: HookPoint = HookPoint.BEFORE) = MicroHookEffect(
        hookPoint = hookPoint,
        kind = EffectKind.ARGUMENT_NULL,
        argumentIndex = index,
        expression = kotlinx.serialization.json.JsonNull,
    )

    @Test
    fun `an ARGUMENT_NULL recipe nulling an obfuscated reference arg is valid`() {
        val payload = v2Payload(
            selector = dropdownFixSelector(),
            effect = argumentNullEffect(index = 0),
            versionConstraint = NeedleVersionConstraint(min = "85262640", max = "85262640"),
        )
        assertEquals(NeedleRecipeValidationResult.Valid, NeedleRecipeValidation.validate(payload))
    }

    @Test
    fun `ARGUMENT_NULL targeting a primitive parameter is rejected`() {
        assertRejected(
            v2Payload(
                selector = dropdownFixSelector(),
                effect = argumentNullEffect(index = 4),
                versionConstraint = NeedleVersionConstraint(min = "85262640", max = "85262640"),
            ),
        )
    }

    @Test
    fun `ARGUMENT_NULL with AFTER hook point is rejected`() {
        assertRejected(
            v2Payload(
                selector = dropdownFixSelector(),
                effect = argumentNullEffect(index = 0, hookPoint = HookPoint.AFTER),
                versionConstraint = NeedleVersionConstraint(min = "85262640", max = "85262640"),
            ),
        )
    }

    @Test
    fun `ARGUMENT_NULL with an out-of-range index is rejected`() {
        assertRejected(
            v2Payload(
                selector = dropdownFixSelector(),
                effect = argumentNullEffect(index = 9),
                versionConstraint = NeedleVersionConstraint(min = "85262640", max = "85262640"),
            ),
        )
    }

    @Test
    fun `obfuscated selector parameter types are accepted for matching`() {
        val payload = v2Payload(
            selector = dropdownFixSelector(),
            effect = argumentNullEffect(index = 0),
            versionConstraint = NeedleVersionConstraint(min = "85262640", max = "85262640"),
        )
        assertEquals(NeedleRecipeValidationResult.Valid, NeedleRecipeValidation.validate(payload))
    }

    @Test
    fun `a malformed selector type name is still rejected`() {
        assertRejected(
            v2Payload(
                selector = dropdownFixSelector().copy(methodParameterTypes = listOf("bad name", "bbpj", "ilg", "hrk", "int")),
                effect = argumentNullEffect(index = 0),
                versionConstraint = NeedleVersionConstraint(min = "85262640", max = "85262640"),
            ),
        )
    }

    @Test
    fun `a v1 payload using ARGUMENT_NULL is rejected`() {
        val payload = NeedleRecipePayload(
            schemaVersion = 1,
            recipeId = "12",
            codename = "v1-argnull",
            appPackageName = "com.example.app",
            selector = MicroHookSelector(type = SelectorKind.DEX_METHOD, methodReturnType = "void", methodParameterTypes = listOf("java.lang.Object")),
            effect = MicroHookEffect(hookPoint = HookPoint.BEFORE, kind = EffectKind.ARGUMENT_NULL, argumentIndex = 0, expression = kotlinx.serialization.json.JsonNull),
        )
        assertRejected(payload)
    }

    @Test
    fun `a v1 payload carrying v2-only fields is rejected`() {
        val payload = NeedleRecipePayload(
            schemaVersion = 1,
            recipeId = "11",
            codename = "smuggled",
            appPackageName = "com.example.app",
            selector = MicroHookSelector(
                type = SelectorKind.DEX_METHOD,
                methodReturnType = "boolean",
                semanticResultType = NeedleTypeNames.BOXED_BOOLEAN,
            ),
            effect = MicroHookEffect(hookPoint = HookPoint.AFTER, kind = EffectKind.BOOLEAN_RESULT, expression = alwaysTrue()),
        )
        assertRejected(payload)
    }

    @Test
    fun `version constraint bounds are inclusive and fail closed on unparsable bounds`() {
        val constraint = NeedleVersionConstraint(min = "100", max = "200")
        assertTrue(constraint.allows(100L))
        assertTrue(constraint.allows(200L))
        assertTrue(!constraint.allows(99L))
        assertTrue(!constraint.allows(201L))
        assertTrue(!NeedleVersionConstraint(min = "abc", max = "200").allows(150L))
        assertTrue(!NeedleVersionConstraint(min = null, max = null).allows(150L))
    }
}
