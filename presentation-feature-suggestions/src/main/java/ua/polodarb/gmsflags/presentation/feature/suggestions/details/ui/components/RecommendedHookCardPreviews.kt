package ua.polodarb.gmsflags.presentation.feature.suggestions.details.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ua.polodarb.gmsflags.domain.server.content.HookTrustStatus
import ua.polodarb.gmsflags.domain.server.content.RecommendationVariantHook
import ua.polodarb.gmsflags.presentation.core.ui.theme.GMSFlags20Theme
import ua.polodarb.gmsflags.presentation.feature.suggestions.details.ui.model.RecommendationHookUiModel

private fun sampleHook(
    trust: HookTrustStatus,
    required: Boolean = true,
    purpose: String? = "Force the Neural Design master device gate while preserving the original result.",
) = RecommendationHookUiModel(
    hook = RecommendationVariantHook(
        recipeId = trust.ordinal.toLong(),
        required = required,
        codename = "neural-design-master-gate",
        purpose = purpose,
        envelope = null,
    ),
    trustStatus = trust,
    recipeDetails = null,
)

@Composable
private fun HookCardPreview(
    hook: RecommendationHookUiModel,
    dark: Boolean = false,
    appIntegrityBlocked: Boolean = false,
) {
    GMSFlags20Theme(darkTheme = dark, dynamicColor = false) {
        Surface(color = MaterialTheme.colorScheme.background) {
            RecommendedHookCard(
                hook = hook,
                appIntegrityBlocked = appIntegrityBlocked,
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}

@Preview(name = "Verified", showBackground = true, widthDp = 400)
@Composable
private fun HookCardVerifiedPreview() = HookCardPreview(sampleHook(HookTrustStatus.VERIFIED))

@Preview(name = "Not signed", showBackground = true, widthDp = 400)
@Composable
private fun HookCardNotSignedPreview() = HookCardPreview(sampleHook(HookTrustStatus.NOT_SIGNED))

@Preview(name = "Not checked here", showBackground = true, widthDp = 400)
@Composable
private fun HookCardCannotVerifyPreview() =
    HookCardPreview(sampleHook(HookTrustStatus.CANNOT_VERIFY_IN_THIS_BUILD))

@Preview(name = "Failed · required", showBackground = true, widthDp = 400)
@Composable
private fun HookCardFailedRequiredPreview() =
    HookCardPreview(sampleHook(HookTrustStatus.VERIFICATION_FAILED, required = true))

@Preview(name = "Failed · optional", showBackground = true, widthDp = 400)
@Composable
private fun HookCardFailedOptionalPreview() =
    HookCardPreview(sampleHook(HookTrustStatus.VERIFICATION_FAILED, required = false))

@Preview(name = "Verified · no note", showBackground = true, widthDp = 400)
@Composable
private fun HookCardNoPurposePreview() =
    HookCardPreview(sampleHook(HookTrustStatus.VERIFIED, purpose = null))

@Preview(name = "Verified · dark", showBackground = true, widthDp = 400)
@Composable
private fun HookCardVerifiedDarkPreview() =
    HookCardPreview(sampleHook(HookTrustStatus.VERIFIED), dark = true)

@Preview(name = "Failed · dark", showBackground = true, widthDp = 400)
@Composable
private fun HookCardFailedDarkPreview() =
    HookCardPreview(sampleHook(HookTrustStatus.VERIFICATION_FAILED), dark = true)

@Preview(name = "App untrusted · verified recipe", showBackground = true, widthDp = 400)
@Composable
private fun HookCardAppUntrustedPreview() = HookCardPreview(
    sampleHook(HookTrustStatus.VERIFIED),
    appIntegrityBlocked = true,
)
