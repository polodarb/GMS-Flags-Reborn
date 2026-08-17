package ua.polodarb.gmsflags.domain.server.content

import ua.polodarb.gmsflags.domain.flags.FlagOverride
import ua.polodarb.gmsflags.domain.flags.FlagType
import ua.polodarb.gmsflags.domain.flags.PhenotypeFlag

enum class RecommendationApplicationStatus {
    Applied,
    PartiallyApplied,
    NotApplied,
    Unavailable,
    ClientUpdateRequired,
}

fun interface HookEngineSupport {
    operator fun invoke(hook: RecommendationVariantHook): Boolean
}

fun resolveRecommendationApplicationStatus(
    expectedOverrides: List<FlagOverride>,
    currentFlags: List<PhenotypeFlag>,
): RecommendationApplicationStatus {
    if (expectedOverrides.isEmpty()) return RecommendationApplicationStatus.Unavailable

    val currentByIdentity = currentFlags.associateBy { it.type to it.name }
    val appliedCount = expectedOverrides.count { expected ->
        val current = currentByIdentity[expected.type to expected.name]
        current?.overridden == true && valuesMatch(
            type = expected.type,
            expected = expected.value,
            current = current.value,
        )
    }

    return when (appliedCount) {
        expectedOverrides.size -> RecommendationApplicationStatus.Applied
        0 -> RecommendationApplicationStatus.NotApplied
        else -> RecommendationApplicationStatus.PartiallyApplied
    }
}

/**
 * Whether the hooks last actually applied (per the locally stored [AppliedHookRef]s) still match
 * what the server currently specifies for this variant. A recipe can be edited/republished after
 * being applied without its [RecommendationVariantHook.recipeId] changing, so id presence alone
 * can't detect that - the stored [AppliedHookRef.payloadSha256] (recorded at apply time) is
 * compared against the hook's current signed envelope hash. [expectedHooks] should already be
 * filtered to the hooks that were actually eligible to be written (VERIFIED trust) - an
 * unverified hook is never applied by design, and comparing against it would misreport that
 * gap as drift instead of the (already visible, separate) trust state.
 */
fun resolveHookApplicationStatus(
    expectedHooks: List<RecommendationVariantHook>,
    appliedHooks: Set<AppliedHookRef>,
    unsupportedRequiredRecipeIds: Set<Long> = emptySet(),
): RecommendationApplicationStatus {
    if (expectedHooks.isEmpty()) return RecommendationApplicationStatus.Unavailable

    if (expectedHooks.any { it.required && it.recipeId in unsupportedRequiredRecipeIds }) {
        return RecommendationApplicationStatus.ClientUpdateRequired
    }

    val appliedByRecipeId = appliedHooks.associateBy { it.recipeId }
    val matchedCount = expectedHooks.count { expected ->
        val applied = appliedByRecipeId[expected.recipeId]
        applied != null && applied.payloadSha256 == expected.envelope?.payloadSha256
    }

    return when (matchedCount) {
        expectedHooks.size -> RecommendationApplicationStatus.Applied
        0 -> RecommendationApplicationStatus.NotApplied
        else -> RecommendationApplicationStatus.PartiallyApplied
    }
}

fun combineRecommendationApplicationStatuses(
    statuses: List<RecommendationApplicationStatus>,
): RecommendationApplicationStatus = when {
    statuses.isEmpty() -> RecommendationApplicationStatus.Unavailable
    statuses.any { it == RecommendationApplicationStatus.ClientUpdateRequired } ->
        RecommendationApplicationStatus.ClientUpdateRequired
    statuses.all { it == RecommendationApplicationStatus.Applied } ->
        RecommendationApplicationStatus.Applied
    statuses.all { it == RecommendationApplicationStatus.NotApplied } ->
        RecommendationApplicationStatus.NotApplied
    statuses.any { it == RecommendationApplicationStatus.Unavailable } ->
        RecommendationApplicationStatus.Unavailable
    else -> RecommendationApplicationStatus.PartiallyApplied
}

private fun valuesMatch(type: FlagType, expected: String, current: String): Boolean = when (type) {
    FlagType.Boolean -> expected.asBooleanStorageValue() == current.asBooleanStorageValue()
    FlagType.Integer -> expected.trim().toLongOrNull() == current.trim().toLongOrNull()
    FlagType.Float -> expected.trim().toDoubleOrNull() == current.trim().toDoubleOrNull()
    FlagType.String -> expected == current
}

private fun String.asBooleanStorageValue(): String? = when {
    this == "1" || equals("true", ignoreCase = true) -> "1"
    this == "0" || equals("false", ignoreCase = true) -> "0"
    else -> null
}
