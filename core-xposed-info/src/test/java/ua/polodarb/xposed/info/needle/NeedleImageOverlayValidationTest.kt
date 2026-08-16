package ua.polodarb.xposed.info.needle

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Base64

class NeedleImageOverlayValidationTest {

    private val appPackage = "com.google.android.googlequicksearchbox"

    private val pngOnePixelBase64 =
        "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=="

    private fun overlay(
        imageBase64: String = pngOnePixelBase64,
        gravity: OverlayGravity = OverlayGravity.TOP_RIGHT,
        offsetXDp: Int = 0,
        offsetYDp: Int = 0,
        widthDp: Int = 24,
        heightDp: Int = 24,
        alpha: Float = 1f,
    ) = NeedleImageOverlay(imageBase64, gravity, offsetXDp, offsetYDp, widthDp, heightDp, alpha)

    private fun overlaySelector(
        name: String = "translate_button",
        pkg: String = appPackage,
    ) = MicroHookSelector(
        type = SelectorKind.VIEW_RESOURCE_ID,
        viewResourceName = name,
        viewResourcePackage = pkg,
    )

    private fun overlayEffect(
        overlay: NeedleImageOverlay = overlay(),
        hookPoint: HookPoint = HookPoint.AFTER,
        argumentIndex: Int? = null,
        whenGate: BooleanExpression? = null,
    ) = MicroHookEffect(
        hookPoint = hookPoint,
        kind = EffectKind.ADD_IMAGE_OVERLAY,
        argumentIndex = argumentIndex,
        expression = NeedleJson.encodeToJsonElement(NeedleImageOverlay.serializer(), overlay),
        `when` = whenGate,
    )

    private fun overlayPayload(
        selector: MicroHookSelector = overlaySelector(),
        effect: MicroHookEffect = overlayEffect(),
        capabilities: List<String> = listOf(NeedleCapabilities.VIEW_IMAGE_OVERLAY),
        minimumEngineVersion: Int = NeedleCapabilities.IMAGE_OVERLAY_ENGINE_VERSION,
        schemaVersion: Int = 2,
    ) = NeedleRecipePayload(
        schemaVersion = schemaVersion,
        recipeId = "20",
        codename = "badge-overlay",
        appPackageName = appPackage,
        revision = "1",
        minimumEngineVersion = minimumEngineVersion,
        requiredCapabilities = capabilities,
        versionConstraint = null,
        selector = selector,
        effect = effect,
    )

    private fun assertValid(payload: NeedleRecipePayload) {
        assertEquals(NeedleRecipeValidationResult.Valid, NeedleRecipeValidation.validate(payload))
    }

    private fun assertInvalid(payload: NeedleRecipePayload) {
        assertTrue(
            "expected Invalid but was ${NeedleRecipeValidation.validate(payload)}",
            NeedleRecipeValidation.validate(payload) is NeedleRecipeValidationResult.Invalid,
        )
    }

    @Test
    fun `a well-formed image overlay recipe is valid`() = assertValid(overlayPayload())

    @Test
    fun `an overlay targeting the android package is valid`() =
        assertValid(overlayPayload(selector = overlaySelector(pkg = "android")))

    @Test
    fun `an overlay without the image capability is rejected`() =
        assertInvalid(overlayPayload(capabilities = emptyList()))

    @Test
    fun `an overlay declaring an engine version below the overlay minimum is rejected`() =
        assertInvalid(overlayPayload(minimumEngineVersion = 2))

    @Test
    fun `an overlay with hook_point BEFORE is rejected`() =
        assertInvalid(overlayPayload(effect = overlayEffect(hookPoint = HookPoint.BEFORE)))

    @Test
    fun `an overlay carrying an argument_index is rejected`() =
        assertInvalid(overlayPayload(effect = overlayEffect(argumentIndex = 0)))

    @Test
    fun `an overlay carrying a when gate is rejected`() = assertInvalid(
        overlayPayload(
            effect = overlayEffect(
                whenGate = BooleanExpression(
                    orGroups = listOf(
                        AndGroup(
                            listOf(
                                Condition(
                                    left = TypedValueSource(source = SourceKind.SDK_INT),
                                    compare = CompareOp.GTE,
                                    right = TypedValueSource(source = SourceKind.CONSTANT, valueType = ConstantValueType.INT, value = "30"),
                                ),
                            ),
                        ),
                    ),
                ),
            ),
        ),
    )

    @Test
    fun `an overlay selector carrying method fields is rejected`() =
        assertInvalid(overlayPayload(selector = overlaySelector().copy(methodReturnType = "boolean")))

    @Test
    fun `an overlay with a blank resource name is rejected`() =
        assertInvalid(overlayPayload(selector = overlaySelector(name = " ")))

    @Test
    fun `an overlay targeting a foreign package is rejected`() =
        assertInvalid(overlayPayload(selector = overlaySelector(pkg = "com.evil.other")))

    @Test
    fun `an overlay whose image is not valid base64 is rejected`() =
        assertInvalid(overlayPayload(effect = overlayEffect(overlay = overlay(imageBase64 = "not base64!!"))))

    @Test
    fun `an overlay whose image is not PNG or WEBP is rejected`() {
        val plain = Base64.getEncoder().encodeToString("this is plain text, not an image".toByteArray())
        assertInvalid(overlayPayload(effect = overlayEffect(overlay = overlay(imageBase64 = plain))))
    }

    @Test
    fun `an overlay whose image exceeds the byte limit is rejected`() {
        val oversized = ByteArray(NeedleImageOverlayLimits.MAX_IMAGE_BYTES + 1) { 0 }
        // stamp a PNG magic so it is the size check, not the format check, that rejects it
        byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A).copyInto(oversized)
        val encoded = Base64.getEncoder().encodeToString(oversized)
        assertInvalid(overlayPayload(effect = overlayEffect(overlay = overlay(imageBase64 = encoded))))
    }

    @Test
    fun `an overlay with a zero render size is rejected`() =
        assertInvalid(overlayPayload(effect = overlayEffect(overlay = overlay(widthDp = 0))))

    @Test
    fun `an overlay with a render size above the limit is rejected`() =
        assertInvalid(overlayPayload(effect = overlayEffect(overlay = overlay(heightDp = NeedleImageOverlayLimits.MAX_RENDER_DP + 1))))

    @Test
    fun `an overlay with an offset above the limit is rejected`() =
        assertInvalid(overlayPayload(effect = overlayEffect(overlay = overlay(offsetXDp = NeedleImageOverlayLimits.MAX_OFFSET_DP + 1))))

    @Test
    fun `an overlay with an out-of-range alpha is rejected`() =
        assertInvalid(overlayPayload(effect = overlayEffect(overlay = overlay(alpha = 1.5f))))

    @Test
    fun `non-finite alpha values are out of range`() {
        assertTrue(!NeedleImageOverlayLimits.isAlphaInRange(Float.NaN))
        assertTrue(!NeedleImageOverlayLimits.isAlphaInRange(Float.POSITIVE_INFINITY))
    }

    @Test
    fun `a schema v1 recipe using a view-resource selector is rejected`() =
        assertInvalid(
            overlayPayload(schemaVersion = 1, capabilities = emptyList(), minimumEngineVersion = 1),
        )
}
