package ua.polodarb.xposed.info.needle

import org.junit.Assert.assertEquals
import org.junit.Test

class NeedleModelsTest {

    private val case001PayloadJson = """
        {
          "schema_version": 1,
          "recipe_id": "1",
          "codename": "neural-design-master-gate",
          "app_package_name": "com.google.android.googlequicksearchbox",
          "selector": {
            "class_using_strings_all": ["Ma-Robin-SamsyPlateDrawableFactory"],
            "method_return_type": "boolean",
            "method_parameter_types": []
          },
          "effect": {
            "hook_point": "AFTER",
            "kind": "BOOLEAN_RESULT",
            "argument_index": null,
            "expression": {
              "or_groups": [
                { "and_conditions": [
                  { "left": {"source": "ORIGINAL_RESULT"}, "compare": "EQ", "right": {"source": "CONSTANT", "value_type": "BOOL", "value": "true"} }
                ] },
                { "and_conditions": [
                  { "left": {"source": "FLAG_OVERRIDE", "package_name": "feature.namespace#com.example.app", "flag_name": "45814173", "value_type": "BOOL"}, "compare": "EQ", "right": {"source": "CONSTANT", "value_type": "BOOL", "value": "true"} },
                  { "left": {"source": "SYSTEM_FEATURE", "name": "PIXEL_2026_EXPERIENCE"}, "compare": "EQ", "right": {"source": "CONSTANT", "value_type": "BOOL", "value": "true"} }
                ] }
              ]
            }
          }
        }
    """.trimIndent()

    @Test
    fun `decodes the Case 001 payload shape end to end`() {
        val payload = NeedleJson.decodeFromString(NeedleRecipePayload.serializer(), case001PayloadJson)

        assertEquals("neural-design-master-gate", payload.codename)
        assertEquals("com.google.android.googlequicksearchbox", payload.appPackageName)
        assertEquals(listOf("Ma-Robin-SamsyPlateDrawableFactory"), payload.selector.classUsingStringsAll)
        assertEquals(HookPoint.AFTER, payload.effect.hookPoint)
        assertEquals(EffectKind.BOOLEAN_RESULT, payload.effect.kind)

        val expression = NeedleJson.decodeFromJsonElement(BooleanExpression.serializer(), payload.effect.expression)
        assertEquals(2, expression.orGroups.size)
        assertEquals(1, expression.orGroups[0].andConditions.size)
        assertEquals(2, expression.orGroups[1].andConditions.size)
        assertEquals(SourceKind.SYSTEM_FEATURE, expression.orGroups[1].andConditions[1].left.source)
        assertEquals("PIXEL_2026_EXPERIENCE", expression.orGroups[1].andConditions[1].left.name)
    }

    @Test
    fun `unknown fields are ignored instead of failing decode`() {
        val withExtraField = case001PayloadJson.replaceFirst("\"schema_version\": 1,", "\"schema_version\": 1, \"future_field\": \"ignored\",")
        val payload = NeedleJson.decodeFromString(NeedleRecipePayload.serializer(), withExtraField)
        assertEquals(1, payload.schemaVersion)
    }

    @Test
    fun `decodes a value expression for a non-boolean effect kind`() {
        val json = """{"hook_point":"BEFORE","kind":"ARGUMENT_REPLACE","argument_index":0,"expression":{"value":{"source":"CONSTANT","value_type":"INT","value":"42"}}}"""
        val effect = NeedleJson.decodeFromString(MicroHookEffect.serializer(), json)

        assertEquals(EffectKind.ARGUMENT_REPLACE, effect.kind)
        assertEquals(0, effect.argumentIndex)
        val value = NeedleJson.decodeFromJsonElement(ValueExpression.serializer(), effect.expression)
        assertEquals(SourceKind.CONSTANT, value.value.source)
        assertEquals("42", value.value.value)
    }

    @Test
    fun `selector without a type field decodes as DEX_METHOD for backward compatibility`() {
        val json = """{"class_using_strings_all":[],"method_return_type":"boolean"}"""
        val selector = NeedleJson.decodeFromString(MicroHookSelector.serializer(), json)
        assertEquals(SelectorKind.DEX_METHOD, selector.type)
    }

    @Test
    fun `ANDROID_RESOURCE_STRING selector decodes without a method_return_type field`() {
        val json = """{"type":"ANDROID_RESOURCE_STRING"}"""
        val selector = NeedleJson.decodeFromString(MicroHookSelector.serializer(), json)
        assertEquals(SelectorKind.ANDROID_RESOURCE_STRING, selector.type)
        assertEquals("", selector.methodReturnType)
    }

    @Test
    fun `effect without a when field decodes as unconditional for backward compatibility`() {
        val json = """{"hook_point":"AFTER","kind":"NUMERIC_RESULT","expression":{"value":{"source":"CONSTANT","value_type":"INT","value":"1"}}}"""
        val effect = NeedleJson.decodeFromString(MicroHookEffect.serializer(), json)
        assertEquals(null, effect.`when`)
    }

    @Test
    fun `STRING_RESULT effect with a when condition referencing RESOURCE_ID decodes fully`() {
        val json = """
            {
              "hook_point": "AFTER",
              "kind": "STRING_RESULT",
              "when": {
                "or_groups": [
                  { "and_conditions": [
                    { "left": {"source": "ARGUMENT", "index": 0},
                      "compare": "EQ",
                      "right": {"source": "RESOURCE_ID", "package_name": "com.google.android.dialer", "resource_type": "string", "resource_name": "call_recording_starting_voice"} }
                  ] }
                ]
              },
              "expression": {"value": {"source": "CONSTANT", "value_type": "STRING", "value": "RECORD"}}
            }
        """.trimIndent()
        val effect = NeedleJson.decodeFromString(MicroHookEffect.serializer(), json)

        assertEquals(EffectKind.STRING_RESULT, effect.kind)
        val condition = requireNotNull(effect.`when`)
        val right = condition.orGroups[0].andConditions[0].right
        assertEquals(SourceKind.RESOURCE_ID, right.source)
        assertEquals("com.google.android.dialer", right.packageName)
        assertEquals("string", right.resourceType)
        assertEquals("call_recording_starting_voice", right.resourceName)
    }
}
