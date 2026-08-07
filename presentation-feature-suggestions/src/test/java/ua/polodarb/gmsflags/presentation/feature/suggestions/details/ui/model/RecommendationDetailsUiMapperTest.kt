package ua.polodarb.gmsflags.presentation.feature.suggestions.details.ui.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import ua.polodarb.gmsflags.domain.apps.FlagPackage
import ua.polodarb.gmsflags.domain.apps.FlagPackageCategory
import ua.polodarb.gmsflags.domain.apps.SupportedApplication
import ua.polodarb.gmsflags.domain.server.content.DangerLevel
import ua.polodarb.gmsflags.domain.server.content.DecodeHookRecipe
import ua.polodarb.gmsflags.domain.server.content.RecommendationExperience
import ua.polodarb.gmsflags.domain.server.content.RecommendationFlagVariant
import ua.polodarb.gmsflags.domain.server.content.RecommendationStatus
import ua.polodarb.gmsflags.domain.server.content.RecommendationSupportStatus
import ua.polodarb.gmsflags.domain.server.content.RecommendedFlag
import ua.polodarb.gmsflags.domain.server.content.RemoteFlagValueType
import ua.polodarb.gmsflags.domain.server.content.HookTrustStatus
import ua.polodarb.gmsflags.domain.server.content.ServerApplication
import ua.polodarb.gmsflags.domain.server.content.ServerRecommendationDetails
import ua.polodarb.gmsflags.domain.server.content.ServerRecommendationSummary
import ua.polodarb.gmsflags.domain.server.content.VerifyHookTrust
import ua.polodarb.gmsflags.domain.server.content.VersionConstraint
import ua.polodarb.gmsflags.domain.server.content.VersionConstraintType

class RecommendationDetailsUiMapperTest {
    private val verifyHookTrust = VerifyHookTrust { HookTrustStatus.NOT_SIGNED }
    private val decodeHookRecipe = DecodeHookRecipe {
        Result.failure(IllegalStateException("no envelope in tests"))
    }

    @Test
    fun `selects compatible variant and prepares local overrides`() {
        val result = experience(
            variants = listOf(
                variant(VersionConstraintType.Until, maximum = 99),
                variant(VersionConstraintType.From, minimum = 100),
            )
        ).toUiModel(verifyHookTrust, decodeHookRecipe)

        assertEquals(1, result.initialVariantIndex())
        assertEquals(
            RecommendationApplyAvailability.Available,
            result.applyAvailability(1),
        )
        assertEquals("feature_enabled", result.variants[1].overrides.single().name)
        assertEquals("com.google.flags", result.target?.phenotypePackageName)
    }

    @Test
    fun `normalizes variant description`() {
        val result = experience(
            variants = listOf(
                variant(
                    type = VersionConstraintType.Unbounded,
                    description = "  Cleaner navigation layout  ",
                )
            )
        ).toUiModel(verifyHookTrust, decodeHookRecipe)

        assertEquals("Cleaner navigation layout", result.variants.single().description)
    }

    @Test
    fun `blocks variants containing unsupported bytes`() {
        val unsupported = RecommendedFlag(
            name = "binary_payload",
            type = RemoteFlagValueType.Bytes,
            value = "AA==",
            title = null,
            description = null,
            dangerLevel = DangerLevel.None,
            badges = emptyList(),
        )
        val result = experience(
            variants = listOf(
                variant(VersionConstraintType.Unbounded, extraFlags = listOf(unsupported))
            )
        ).toUiModel(verifyHookTrust, decodeHookRecipe)

        assertEquals(
            RecommendationApplyAvailability.ContainsUnsupportedFlags,
            result.applyAvailability(0),
        )
        assertEquals(1, result.variants.single().unsupportedFlagCount)
    }

    @Test
    fun `does not invent a target for an uninstalled app`() {
        val result = experience(installed = null).toUiModel(verifyHookTrust, decodeHookRecipe)

        assertNull(result.target)
        assertEquals(
            RecommendationApplyAvailability.ApplicationNotInstalled,
            result.applyAvailability(0),
        )
    }

    private fun experience(
        variants: List<RecommendationFlagVariant> = listOf(
            variant(VersionConstraintType.Unbounded)
        ),
        installed: SupportedApplication? = installedApplication,
    ) = RecommendationExperience(
        details = ServerRecommendationDetails(
            summary = ServerRecommendationSummary(
                id = 7,
                status = RecommendationStatus.Published,
                supportStatus = RecommendationSupportStatus.Verified,
                logoUrl = null,
                title = "Feature",
                description = "Description",
                warning = null,
            ),
            applicationId = 4,
            infoBlock = null,
            screenshots = emptyList(),
            externalLink = null,
            source = null,
            variants = variants,
        ),
        application = ServerApplication(
            id = 4,
            packageName = "com.google.app",
            displayName = "Google App",
            iconUrl = null,
        ),
        installedApplication = installed,
    )

    private fun variant(
        type: VersionConstraintType,
        minimum: Long? = null,
        maximum: Long? = null,
        description: String? = null,
        extraFlags: List<RecommendedFlag> = emptyList(),
    ) = RecommendationFlagVariant(
        id = null,
        label = null,
        description = description,
        versionConstraint = VersionConstraint(type, minimum, maximum),
        flags = listOf(
            RecommendedFlag(
                name = "feature_enabled",
                type = RemoteFlagValueType.Boolean,
                value = "true",
                title = null,
                description = null,
                dangerLevel = DangerLevel.None,
                badges = emptyList(),
            )
        ) + extraFlags,
    )

    private companion object {
        val installedApplication = SupportedApplication(
            androidPackageName = "com.google.app",
            flagPackages = listOf(
                FlagPackage("com.google.flags", FlagPackageCategory.Primary)
            ),
            name = "Google App",
            versionName = "1.0",
            versionCode = 100,
            lastUpdateTime = 0,
        )
    }
}
