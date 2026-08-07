package ua.polodarb.gmsflags.domain.server.content

import org.junit.Assert.assertEquals
import org.junit.Test
import ua.polodarb.gmsflags.domain.flags.FlagOverride
import ua.polodarb.gmsflags.domain.flags.FlagType
import ua.polodarb.gmsflags.domain.flags.PhenotypeFlag

class RecommendationApplicationStatusTest {
    @Test
    fun `all matching overrides are applied`() {
        assertEquals(
            RecommendationApplicationStatus.Applied,
            resolveRecommendationApplicationStatus(
                expectedOverrides = listOf(
                    FlagOverride("enabled", FlagType.Boolean, "true"),
                ),
                currentFlags = listOf(
                    flag("enabled", FlagType.Boolean, "1", overridden = true),
                ),
            ),
        )
    }

    @Test
    fun `one changed override is partially applied`() {
        assertEquals(
            RecommendationApplicationStatus.PartiallyApplied,
            resolveRecommendationApplicationStatus(
                expectedOverrides = listOf(
                    FlagOverride("first", FlagType.Boolean, "true"),
                    FlagOverride("second", FlagType.Integer, "2"),
                ),
                currentFlags = listOf(
                    flag("first", FlagType.Boolean, "1", overridden = true),
                    flag("second", FlagType.Integer, "3", overridden = true),
                ),
            ),
        )
    }

    @Test
    fun `matching default without override is not applied`() {
        assertEquals(
            RecommendationApplicationStatus.NotApplied,
            resolveRecommendationApplicationStatus(
                expectedOverrides = listOf(
                    FlagOverride("enabled", FlagType.Boolean, "true"),
                ),
                currentFlags = listOf(
                    flag("enabled", FlagType.Boolean, "1", overridden = false),
                ),
            ),
        )
    }

    @Test
    fun `package statuses are applied only when every package is applied`() {
        assertEquals(
            RecommendationApplicationStatus.Applied,
            combineRecommendationApplicationStatuses(
                listOf(
                    RecommendationApplicationStatus.Applied,
                    RecommendationApplicationStatus.Applied,
                )
            ),
        )
        assertEquals(
            RecommendationApplicationStatus.PartiallyApplied,
            combineRecommendationApplicationStatuses(
                listOf(
                    RecommendationApplicationStatus.Applied,
                    RecommendationApplicationStatus.NotApplied,
                )
            ),
        )
    }

    @Test
    fun `unavailable package makes the combined status unavailable`() {
        assertEquals(
            RecommendationApplicationStatus.Unavailable,
            combineRecommendationApplicationStatuses(
                listOf(
                    RecommendationApplicationStatus.Applied,
                    RecommendationApplicationStatus.Unavailable,
                )
            ),
        )
    }

    @Test
    fun `hooks whose stored hash matches the current recipe are applied`() {
        assertEquals(
            RecommendationApplicationStatus.Applied,
            resolveHookApplicationStatus(
                expectedHooks = listOf(hook(recipeId = 1, sha256 = "current-hash")),
                appliedHooks = setOf(AppliedHookRef(recipeId = 1, payloadSha256 = "current-hash", required = true)),
            ),
        )
    }

    @Test
    fun `a republished recipe with a changed payload hash is not applied`() {
        assertEquals(
            RecommendationApplicationStatus.NotApplied,
            resolveHookApplicationStatus(
                expectedHooks = listOf(hook(recipeId = 1, sha256 = "new-hash-after-republish")),
                appliedHooks = setOf(AppliedHookRef(recipeId = 1, payloadSha256 = "stale-hash", required = true)),
            ),
        )
    }

    @Test
    fun `one drifted hook among several is partially applied`() {
        assertEquals(
            RecommendationApplicationStatus.PartiallyApplied,
            resolveHookApplicationStatus(
                expectedHooks = listOf(
                    hook(recipeId = 1, sha256 = "unchanged-hash"),
                    hook(recipeId = 2, sha256 = "new-hash"),
                ),
                appliedHooks = setOf(
                    AppliedHookRef(recipeId = 1, payloadSha256 = "unchanged-hash", required = true),
                    AppliedHookRef(recipeId = 2, payloadSha256 = "stale-hash", required = true),
                ),
            ),
        )
    }

    @Test
    fun `no hooks in the variant is unavailable`() {
        assertEquals(
            RecommendationApplicationStatus.Unavailable,
            resolveHookApplicationStatus(expectedHooks = emptyList(), appliedHooks = emptySet()),
        )
    }

    private fun hook(recipeId: Long, sha256: String) = RecommendationVariantHook(
        recipeId = recipeId,
        required = true,
        envelope = SignedNeedleEnvelope(
            mediaType = "application/vnd.gmsflags.needle-recipe+json",
            payloadBase64 = "payload",
            payloadSha256 = sha256,
            signatureAlgorithm = "ECDSA_P256_SHA256",
            signatureBase64 = "signature",
        ),
    )

    private fun flag(
        name: String,
        type: FlagType,
        value: String,
        overridden: Boolean,
    ) = PhenotypeFlag(
        name = name,
        type = type,
        originalValue = null,
        value = value,
        overridden = overridden,
    )
}
