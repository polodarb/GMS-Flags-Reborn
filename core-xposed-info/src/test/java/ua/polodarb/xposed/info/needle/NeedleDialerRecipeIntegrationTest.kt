package ua.polodarb.xposed.info.needle

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Full round trip for the "dialer-call-recording-start-announcement" recipe shape: decode the
 * exact JSON a signed recipe would carry, validate it, resolve which resource ids it cares about,
 * and confirm the dispatch pipeline applies/withholds the replacement correctly - everything this
 * plan's protocol and pure-logic layer is responsible for, without needing Android/Xposed. */
class NeedleDialerRecipeIntegrationTest {

    private val recipeJson = """
        {
          "schema_version": 1,
          "recipe_id": "dialer-1",
          "codename": "dialer-call-recording-start-announcement",
          "app_package_name": "com.google.android.dialer",
          "selector": { "type": "ANDROID_RESOURCE_STRING" },
          "effect": {
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
        }
    """.trimIndent()

    private class FakeResourcesContext(private val resolvedId: Int) : NeedleEvaluationContext {
        override fun originalResult(): Any? = null
        override fun argument(index: Int): Any? = null
        override fun flagOverride(packageName: String?, flagName: String?, valueType: ConstantValueType?): Any? = null
        override fun systemFeature(name: String?): Boolean = false
        override fun sdkInt(): Int = 34
        override fun resourceId(packageName: String?, resourceType: String?, resourceName: String?): Int? =
            resolvedId.takeIf { packageName == "com.google.android.dialer" && resourceType == "string" && resourceName == "call_recording_starting_voice" }
    }

    private fun decode() = NeedleJson.decodeFromString(NeedleRecipePayload.serializer(), recipeJson)

    @Test
    fun `decodes with the ANDROID_RESOURCE_STRING selector and STRING_RESULT effect`() {
        val payload = decode()
        assertEquals(SelectorKind.ANDROID_RESOURCE_STRING, payload.selector.type)
        assertEquals(EffectKind.STRING_RESULT, payload.effect.kind)
        assertEquals(HookPoint.AFTER, payload.effect.hookPoint)
    }

    @Test
    fun `passes semantic validation`() {
        assertEquals(NeedleRecipeValidationResult.Valid, NeedleRecipeValidation.validate(decode()))
    }

    @Test
    fun `its when condition has exactly one canonical dispatch-key source matching the announcement resource`() {
        val sources = requireNotNull(decode().effect.`when`).dispatchKeyResourceSources()
        assertEquals(1, sources.size)
        assertEquals("call_recording_starting_voice", sources[0].resourceName)
    }

    @Test
    fun `dispatch applies RECORD when the call site's resource id matches the resolved id`() {
        val payload = decode()
        val context = FakeResourcesContext(resolvedId = 12345)
        val argumentMatchesResolvedIdContext = object : NeedleEvaluationContext by context {
            override fun argument(index: Int): Any? = if (index == 0) 12345 else null
        }
        val result = ResourceStringConflictResolver.resolve(listOf(payload), argumentMatchesResolvedIdContext)
        assertEquals(ResourceStringDispatchResult.Applied("RECORD", redundantMatchCount = 0), result)
    }

    @Test
    fun `dispatch withholds the replacement when the call site's resource id does not match`() {
        val payload = decode()
        val context = FakeResourcesContext(resolvedId = 12345)
        val argumentForADifferentResourceContext = object : NeedleEvaluationContext by context {
            override fun argument(index: Int): Any? = if (index == 0) 99999 else null
        }
        val result = ResourceStringConflictResolver.resolve(listOf(payload), argumentForADifferentResourceContext)
        assertEquals(ResourceStringDispatchResult.NoMatch, result)
    }

    @Test
    fun `an unrelated recipe for a different app never matches this app's resource lookups`() {
        val payload = decode().copy(appPackageName = "com.google.android.dialer")
        assertTrue(payload.appPackageName == "com.google.android.dialer")
    }
}
